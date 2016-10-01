/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.demand.ReadDemandNetwork;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.vehicle.VOT;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.wallet.StaticWallet;
import avdta.vehicle.Vehicle;
import avdta.vehicle.wallet.Wallet;
import avdta.vehicle.fuel.VehicleClass;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This adds methods to read the trip table for DTA. 
 * This also contains the {@link ReadDTANetwork#prepareDemand(avdta.project.DTAProject, double)} method to generate demand.
 * @author Michael
 */
public class ReadDTANetwork extends ReadDemandNetwork
{
   
    
    /**
     * Constructs the {@link ReadDTANetwork}
     */
    public ReadDTANetwork()
    {
        super();
    }
    
    /**
     * Constructs a {@link DTASimulator} for the given {@link DTAProject}. 
     * This calls {@link ReadNetwork#readNodes(avdta.project.Project)} and {@link ReadNetwork#readLinks(avdta.project.Project)} and initializes the simulator ({@link Simulator#initialize()}).
     * This also reads transit ({@link ReadDTANetwork#readTransit(avdta.project.TransitProject)} and personal vehicles ({@link ReadDTANetwork#readVehicles(avdta.project.DTAProject)}).
     * @param project the {@link DTAProject}
     * @return the created {@link DTASimulator}
     * @throws IOException if a file cannot be accessed
     */
    public DTASimulator readNetwork(DTAProject project) throws IOException
    {
        readOptions(project);
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        
        DTASimulator sim = new DTASimulator(project, nodes, links);
        
        sim.initialize();
        
        vehicles = new ArrayList<Vehicle>();
        readTransit(project);
        readVehicles(project);
        
        sim.setVehicles(vehicles);
        
        return sim;
    }
    
    
    
    /**
     * This changes the type of vehicles in the dynamic_od file to match the specified proportions.
     * The proportions should sum to 1.
     * This rewrites the dynamic_od file.
     * @param project the project 
     * @param proportionMap a mapping of type codes to proportions
     * @throws IOException if a file cannot be accessed
     */
    public void changeType(DTAProject project, Map<Integer, Double> proportionMap) throws IOException
    {
        Map<Integer, Map<Integer, Map<Integer, Double>>> demands = new TreeMap<Integer, Map<Integer, Map<Integer, Double>>>();
        
        Scanner filein = new Scanner(project.getDynamicODFile());
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            filein.nextInt();
            int type = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            int t = filein.nextInt();
            double demand = filein.nextDouble();
            filein.nextLine();
            
            Map<Integer, Map<Integer, Double>> temp;
            
            if(demands.containsKey(origin))
            {
                temp = demands.get(origin);
            }
            else
            {
                demands.put(origin, temp = new TreeMap<Integer, Map<Integer, Double>>());
            }
            
            Map<Integer, Double> temp2;
            
            if(temp.containsKey(dest))
            {
                temp2 = temp.get(dest);
            }
            else
            {
                temp.put(dest, temp2 = new TreeMap<Integer, Double>());
            }
            
            if(temp2.containsKey(t))
            {
                temp2.put(t, temp2.get(t) + demand);
            }
            else
            {
                temp2.put(t, demand);
            }
        }
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDynamicODFile()), true);
        fileout.println(getDynamicODFileHeader());
        
        int new_id = 1;
        for(int o : demands.keySet())
        {
            Map<Integer, Map<Integer, Double>> temp = demands.get(o);
            
            for(int d : temp.keySet())
            {
                Map<Integer, Double> temp2 = temp.get(d);
                
                for(int t : temp2.keySet())
                {
                    double total = temp2.get(t);
                    
                    for(int type : proportionMap.keySet())
                    {
                        double dem = total * proportionMap.get(type);
                        
                        if(dem > 0)
                        {
                            fileout.println((new_id++)+"\t"+type+"\t"+o+"\t"+d+"\t"+t + "\t" + dem);     
                        }
                    }
                }
            }
        }
        fileout.close();
    }
    
    
    
    /**
     * This reads the demand file into a list of {@link Vehicle}s.
     * @param project the project
     * @return the list of {@link Vehicle}s.
     * @throws IOException if a file cannot be accessed
     */
    public List<Vehicle> readVehicles(DTAProject project) throws IOException
    {
        
        
        Scanner filein = new Scanner(project.getDemandFile());
        
        filein.nextLine();
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int origin_id = filein.nextInt();
            int dest_id = filein.nextInt();
            int dtime = filein.nextInt();
            double vot = filein.nextDouble();
            filein.nextLine();
  
            if(type / 100 == DA_VEHICLE/100)
            {
                
            
                Wallet wallet = new StaticWallet(vot);
            
                Zone origin = (Zone)nodesmap.get(origin_id);
                Zone dest = (Zone)nodesmap.get(dest_id);
                
                origin.addProductions(1);
                dest.addAttractions(1);
                
                VehicleClass vehClass = null;
                DriverType driver = null;
                
                switch(type % 10)
                {
                    case ICV:
                        vehClass = VehicleClass.icv;
                        break;
                    case BEV:
                        vehClass = VehicleClass.bev;
                        break;
                    default:
                        throw new RuntimeException("Vehicle class not recognized - "+type);
                }
                
                switch((type / 10 % 10)*10)
                {
                    case HV:
                        driver = DriverType.HV;
                        break;
                    case AV:
                        driver = DriverType.AV;
                        break;
                    default:
                        throw new RuntimeException("Vehicle class not recognized - "+type);
                }
                vehicles.add(new PersonalVehicle(id, origin, dest, dtime, vot, vehClass, driver));
            }
        }
        

        filein.close();

        return vehicles;
    }
    
    
    
    
}
