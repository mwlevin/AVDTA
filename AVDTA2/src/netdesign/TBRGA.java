/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import avdta.dta.DTASimulator;
import avdta.network.ReadNetwork;
import avdta.network.cost.TravelCost;
import avdta.network.node.Node;
import avdta.network.node.NodeRecord;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author micha
 */
public class TBRGA extends GeneticAlgorithm<TBRIndividual>
{
    private int type;
    
    private Map<Integer, Integer> intersections;
    private DTAProject project;
    
    private int max_tbrs;
    private boolean checkHV;
    private boolean isSO;
    
    private Map<Integer, Set<Integer>> odpairs;
    
    
    public TBRGA(DTAProject project, int max_tbrs, boolean checkHV, boolean isSO, int population_size, double proportion_kept, double mutate_percent)
    {
    		super(population_size, proportion_kept, mutate_percent);
    		
        this.project = project;
        this.max_tbrs = max_tbrs;
        this.checkHV = checkHV;
        this.isSO = isSO;
        
        intersections = new HashMap<Integer, Integer>();
        int counter = 0;
        DTASimulator sim = project.getSimulator();
        for(Node n : sim.getNodes()) {
        		if(!n.isZone()) {
        			intersections.put(n.getId(), counter);
            		counter++;
        		}
        }
        
        type =  ReadNetwork.RESERVATION + ReadNetwork.FCFS;//ReadNetwork.MCKS + ReadNetwork.PRESSURE;
        
        if(checkHV)
        {            
            odpairs = new HashMap<Integer, Set<Integer>>();
            
            for(Vehicle v : sim.getVehicles())
            {
                PersonalVehicle veh = (PersonalVehicle)v;
                if(!v.getDriver().isTransit())
                {
                    int r = veh.getOrigin().getId();
                    int s = veh.getDest().getId();
                    
                    if(!odpairs.containsKey(r))
                    {
                        odpairs.put(r, new HashSet<Integer>());
                    }
                    odpairs.get(r).add(s);
                }
            }
        }
    }
    
    public TBRIndividual createRandom() throws IOException
    {
        TBRIndividual org;
        int[] controls = new int[intersections.size()];

        if(!isSO) {
        do
        {
            for(int i = 0; i < controls.length; i++)
            {
                controls[i] = ReadNetwork.SIGNAL;
            }
            
            for(int i = 0; i < max_tbrs; i++)
            {
                controls[(int)(Math.random() * controls.length)] = type;
            }
            
            org = new TBRIndividual(controls);
        }
        while(!isFeasible(org));
        }
        else {
        		for(int i = 0; i < controls.length; i++) {
        			controls[i] = Math.random() < 0.5 ? type : ReadNetwork.SIGNAL;
        		}
        		org = new TBRIndividual(controls);
        }
        return org;
    }
    
    
    public TBRIndividual cross(TBRIndividual parent1, TBRIndividual parent2) throws IOException
    {
		TBRIndividual child;
		
		if (!isSO) {
			do {
				child = parent1.cross(parent2);
			} while (!isFeasible(child));
		}
		else {
			child = parent1.cross(parent2);
		}
		return child;
    }
    
    public void mutate(TBRIndividual org) throws IOException
    {
    		int[] newcontrols = org.getControls();
    		
        for (int i = 0; i < newcontrols.length; i++) {
        		if(Math.random() < 0.07) {
        			
        			if (newcontrols[i] == ReadNetwork.SIGNAL) {
        				newcontrols[i] = type;
        			} else newcontrols[i] = ReadNetwork.SIGNAL;
        		}
        }
    }
    
    public boolean isFeasible(TBRIndividual org) throws IOException
    {
        int tbrs = 0;
        
        for(int i : org.getControls())
        {
            if(i % 100 == ReadNetwork.RESERVATION)
            {
                tbrs++;
            }
        }
        
        if(tbrs > max_tbrs)
        {
            return false;
        }
        
        if(checkHV)
        {
            changeNodes(org);

            project.loadSimulator();
            DTASimulator sim = project.getSimulator();

            Map<Integer, Node> nodesmap = sim.createNodeIdsMap();
            
            for(int r : odpairs.keySet())
            {
                sim.dijkstras(nodesmap.get(r), 0, 1.0, DriverType.HV, TravelCost.ffTime);
                
                for(int s : odpairs.get(r))
                {
                    Node d = nodesmap.get(s);
                    
                    if(d.prev == null)
                    {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    public void evaluate(TBRIndividual child) throws IOException
    {
        changeNodes(child);
        
        project.loadSimulator();
        DTASimulator sim = project.getSimulator();
        
        // solve DTA
        sim.msa(30, 2.0);
        
        child.setAssignment(sim.getAssignment());
    }
    
    public void changeNodes(TBRIndividual org) throws IOException
    {
        Scanner filein = new Scanner(project.getNodesFile());
        File newFile = new File(project.getProjectDirectory()+"/new_nodes.txt");
        PrintStream fileout = new PrintStream(new FileOutputStream(newFile), true);
        
        fileout.println(filein.nextLine());
        
        while(filein.hasNextLine())
        {
            NodeRecord node = new NodeRecord(filein.nextLine());
            if(!node.isZone())
            {
                node.setType(org.getControl(intersections.get(node.getId())));
            }
            fileout.println(node);
        }
        filein.close();
        fileout.close();
        
        project.getNodesFile().delete();
        newFile.renameTo(project.getNodesFile());
    }

	@Override
	public void print(TBRIndividual best, int iteration, int nummutations) throws FileNotFoundException {

		int counttbr = 0;
		int countsig = 0;
		PrintStream fileout = new PrintStream(new FileOutputStream(new File("GA_RESULTS_1")), true);
		fileout.println("Iteration " + iteration);
		fileout.println("TSTT\t" + best.getObj() + "\tNumber of mutations\t" + nummutations);
		for(int node : intersections.keySet()) {
			if(best.getControl(intersections.get(node)) == 100)	countsig++;
			else		counttbr++;
		}
		fileout.println("Num of Signals: " + countsig + "/tNum of Reservations: " + counttbr);
		fileout.println("Node\tControl");
		for(int node : intersections.keySet()) {
			fileout.println(node + "\t" + best.getControl(intersections.get(node)));
		}
		fileout.println();
		fileout.close();
	}
}
