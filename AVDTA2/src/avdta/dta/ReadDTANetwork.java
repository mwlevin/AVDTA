/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.network.AST;
import avdta.network.DemandProfile;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.vehicle.VOT;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.StaticWallet;
import avdta.vehicle.Vehicle;
import avdta.vehicle.Wallet;
import avdta.vehicle.fuel.VehicleClass;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author micha
 */
public class ReadDTANetwork extends ReadNetwork
{
    public DTASimulator readNetwork(DTAProject project) throws IOException
    {
        readOptions(project);
        List<Node> nodes = readNodes(project);
        List<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        
        DTASimulator sim = new DTASimulator(project, nodes, links);
        
        sim.initialize();
        
        sim.setVehicles(readVehicles(project));
        
        return sim;
    }
    
    public void createDynamicOD(DTAProject project) throws IOException
    {
        DemandProfile profile = readDemandProfile(project);
        
        Scanner filein = new Scanner(project.getStaticODFile());
        
        if(!filein.hasNextInt())
        {
            filein.close();
            
            return;
        }
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDynamicODFile()), true);
        
        fileout.println("id\ttype\torigin\tdest\tast\tdemand");
        filein.nextLine();
        
        int new_id = 1;
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            double demand = filein.nextDouble();
            
            filein.nextLine();
            
            for(int t : profile.keySet())
            {
                AST ast = profile.get(t);
                
                fileout.println((new_id++)+"\t"+type+"\t"+origin+"\t"+dest+"\t"+ast.getId() + "\t" + demand * ast.getWeight());
            }
        }
        
        filein.close();
        fileout.close();
    }
    
    public int prepareDemand(DTAProject project, double prop) throws IOException
    {
        Set<VehicleRecord> vehicles = new TreeSet<VehicleRecord>();
        
        DemandProfile profile = readDemandProfile(project);
        Scanner filein = new Scanner(project.getDynamicODFile());
        
        
        filein.nextLine();
        
        Random rand = project.getRandom();
        
        int total = 0;

        int new_id = 1;
        while(filein.hasNextInt())
        {
            filein.nextInt();
            int type = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            int t = filein.nextInt();
            double demand = filein.nextDouble() * prop;
            
            filein.nextLine();

           
            AST ast = profile.get(t);
            
            
            
            int num_vehicles = (int)Math.floor(demand);
            double rem = demand - Math.floor(demand);
            
            
            if(rand.nextDouble() < rem)
            {
                num_vehicles ++;
            }
            
            int dtime_interval = ast.getDuration() / (num_vehicles+1);
            
            
            for(int i = 0; i < num_vehicles; i++)
            {
                int dtime = ast.getStart() + (i+1) * dtime_interval;
                
                vehicles.add(new VehicleRecord(new_id++, type, origin, dest, dtime));
            }
            
            total += num_vehicles;
           
        }
        
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDemandFile()), true);
        
        fileout.println("id\ttype\torigin\tdest\tdtime\tvot");
        
        for(VehicleRecord v : vehicles)
        {
            double vot = VOT.dagum_rand(rand);
            fileout.println(v+"\t"+vot);
        }
        fileout.close();
        

        return total;
        
    }
    
    public List<PersonalVehicle> readVehicles(DTAProject project) throws IOException
    {
        List<PersonalVehicle> vehicles = new ArrayList<PersonalVehicle>();
        
        Scanner filein = new Scanner(project.getDemandFile());
        
        Random rand = project.getRandom();
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
  
            if(type != Vehicle.BUS)
            {
                
            
                Wallet wallet = new StaticWallet(vot);
            
                Zone origin = (Zone)nodesmap.get(origin_id);
                Zone dest = (Zone)nodesmap.get(dest_id);
                
                origin.addProductions(1);
                dest.addAttractions(1);
                
                vehicles.add(new PersonalVehicle(id, origin, dest, dtime, vot, VehicleClass.getVehClass(type), DriverType.getDriver(type)));
            }
        }
        

        filein.close();

        return vehicles;
    }
    
    public DemandProfile readDemandProfile(DTAProject project) throws IOException
    {
        DemandProfile output = new DemandProfile();
        
        Scanner filein = new Scanner(project.getDemandProfileFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            double weight = filein.nextDouble();
            int start = filein.nextInt();
            int duration = filein.nextInt();
            
            filein.nextLine();
            
            output.put(id, new AST(id, start, duration, weight));
        }
        
        filein.close();

        output.normalizeWeights();
        
        return output;
    }
}
