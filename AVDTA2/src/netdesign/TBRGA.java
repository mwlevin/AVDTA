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
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author micha
 */
public class TBRGA extends GeneticAlgorithm<TBROrg>
{
    private Map<Integer, Integer> intersections;
    private DTAProject project;
    
    private int max_tbrs;
    private boolean checkHV;
    
    
    private Map<Integer, Set<Integer>> odpairs;
    
    
    public TBRGA(DTAProject project, int max_tbrs, boolean checkHV)
    {
        this.project = project;
        this.max_tbrs = max_tbrs;
        this.checkHV = checkHV;
        
        if(checkHV)
        {
            DTASimulator sim = project.getSimulator();
            
            odpairs = new HashMap<Integer, Set<Integer>>();
            
            for(Vehicle v : sim.getVehicles())
            {
                if(!v.getDriver().isTransit())
                {
                    int r = v.getOrigin().getId();
                    int s = v.getDest().getId();
                    
                    if(!odpairs.containsKey(r))
                    {
                        odpairs.put(r, new HashSet<Integer>());
                    }
                    odpairs.get(r).add(s);
                }
            }
        }
    }
    
    public TBROrg cross(TBROrg parent1, TBROrg parent2) throws IOException
    {
        TBROrg child;
        
        do
        {
            child = parent1.cross(parent2);
        }
        while(!isFeasible(child));
        
        return child;
    }
    
    public void mutate(TBROrg org) throws IOException
    {
        
    }
    
    public boolean isFeasible(TBROrg org) throws IOException
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
    
    public void evaluate(TBROrg parent1, TBROrg parent2, TBROrg child) throws IOException
    {
        changeNodes(child);
        
        project.loadSimulator();
        DTASimulator sim = project.getSimulator();
        
        // solve DTA
        sim.msa(30);
        
        child.setAssignment(sim.getAssignment());
    }
    
    public void changeNodes(TBROrg org) throws IOException
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
}
