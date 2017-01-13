/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.fourstep;

import avdta.demand.DemandProfile;
import avdta.demand.DynamicODRecord;
import avdta.demand.ReadDemandNetwork;
import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.Network;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.network.ReadNetwork;
import avdta.project.FourStepProject;
import avdta.project.Project;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author micha
 */
public class ReadFourStepNetwork extends ReadDTANetwork
{
    /**
     * Constructs the {@link ReadFourStepNetwork}
     */
    public ReadFourStepNetwork()
    {
        
    }
    
    
    /**
     * Constructs a {@link FourStepSimulator} for the given {@link FourStepProject}. 
     * This calls {@link ReadNetwork#readNodes(avdta.project.Project)} and {@link ReadNetwork#readLinks(avdta.project.Project)} and initializes the simulator ({@link Simulator#initialize()}).
     * This also reads transit ({@link ReadDTANetwork#readTransit(avdta.project.TransitProject)} and personal vehicles ({@link ReadDTANetwork#readVehicles(avdta.project.DTAProject)}).
     * @param project the {@link FourStepProject}
     * @return the created {@link FourStepSimulator}
     * @throws IOException if a file cannot be accessed
     */
    public FourStepSimulator readNetwork(FourStepProject project) throws IOException
    {
        readOptions(project);
        readFourStepOptions(project);
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        
        
        
        FourStepSimulator sim = new FourStepSimulator(project, nodes, links);

        readTransit(project);
        
        sim.initialize();
        
        
        readZones(project);

        
        return sim;
    }
    
    public void readZones(FourStepProject project) throws IOException
    {
        Scanner filein = new Scanner(project.getZonesFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            double p = filein.nextDouble();
            double a = filein.nextDouble();
            double pat = filein.nextDouble();
            double parkFee = filein.nextDouble();
            
            if(nodesmap.containsKey(id))
            {
                Node node = nodesmap.get(id);
                
                if(node instanceof Zone)
                {
                    Zone origin = (Zone)node;
                    origin.setProductions(p);
                    
                    Zone dest = origin.getLinkedZone();
                    dest.setAttractions(a);
                    dest.setPreferredArrivalTime(pat);
                    dest.setParkingFee(parkFee);

                }
                else
                {
                    throw new RuntimeException("Node "+id+" is not a zone.");
                }
            }
            else
            {
                throw new RuntimeException("Zone "+id+" not found from productions.");
            }
        }
        
        filein.close();
    }
    
    
    
    /**
     * Reads the project options from the options file.
     * @param project the project
     */
    public void readFourStepOptions(FourStepProject project)
    {
        try
        {
            Scanner filein = new Scanner(project.getFourStepOptionsFile());
            
            filein.nextLine();
            
            while(filein.hasNext())
            {
                
                String key = filein.next().toLowerCase();
                String val = filein.next().toLowerCase();
                project.setFourStepOption(key, val);
                
                
            }
            filein.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    
    
    public static String getZonesFileHeader()
    {
        return "node\tproductions\tattractions\tarrival_time\tparking_fee";
    }
    
    public void createZonesFile(FourStepProject project) throws IOException
    {
        createZonesFile(project, 1.0);
    }
    
    public void createZonesFile(FourStepProject project, double scale) throws IOException
    {
        ReadDemandNetwork read = new ReadDemandNetwork();
        
        DemandProfile profile = read.readDemandProfile(project);
        
        
        Map<Integer, Double[]> productions = new HashMap<Integer, Double[]>();
        
        int max_time = 0;
        int ast_duration = Integer.parseInt(project.getOption("ast-duration"));
        
        Scanner filein = new Scanner(project.getDynamicODFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            DynamicODRecord od = new DynamicODRecord(filein.nextLine());
            

            max_time = (int)Math.max(max_time, profile.get(od.getAST()).getEnd());
            
            if(productions.containsKey(od.getOrigin()))
            {
                Double[] temp = productions.get(od.getOrigin());
                temp[0] += od.getDemand();
            }
            else
            {
                productions.put(od.getOrigin(), new Double[]{od.getDemand(), 0.0});
            }
            
            if(productions.containsKey(od.getDest()))
            {
                Double[] temp = productions.get(od.getDest());
                temp[1] += od.getDemand();
            }
            else
            {
                productions.put(od.getDest(), new Double[]{0.0, od.getDemand()});
            }
        }
        filein.close();
        
        int dem_asts = (int)Math.ceil(max_time / ast_duration);
        project.setOption("demand-asts", ""+dem_asts);
        project.writeOptions();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getZonesFile()), true);
        fileout.println(ReadFourStepNetwork.getZonesFileHeader());
        
        Random rand = new Random();
        
        double dem_duration = dem_asts * ast_duration;
        
        for(int o : productions.keySet())
        {
            Double[] temp = productions.get(o);
            double pat = rand.nextGaussian() * dem_duration;
            fileout.println(o+"\t"+(temp[0]*scale)+"\t"+(temp[1]*scale)+"\t"+pat+"\t"+5);
        }
        fileout.close();
    }
    
}
