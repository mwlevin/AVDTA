/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.demand.ReadDemandNetwork;
import avdta.network.PathList;
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
 * This also contains the {@link ReadDemandNetwork#prepareDemand(avdta.project.DemandProject, double)} method to generate demand.
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

        vehicles = new ArrayList<Vehicle>();
        readTransit(project);
        readVehicles(project);
        
        sim.setVehicles(vehicles);
        
        sim.initialize();
        
        
        
        return sim;
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
                
                if(origin == null)
                {
                    throw new RuntimeException("Origin is null: "+origin_id);
                }
                
                if(dest == null)
                {
                    throw new RuntimeException("Dest is null: "+dest_id);
                }
                
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
