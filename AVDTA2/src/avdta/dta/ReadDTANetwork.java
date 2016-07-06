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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author micha
 */
public class ReadDTANetwork extends ReadNetwork
{
    public static final int HV = 10;
    public static final int AV = 20;
    
    
    public static final int ICV = 1;
    public static final int BEV = 2;
    
    public static final int DA_VEHICLE = 100;
    public static final int BUS = 500;
    public static final int SAV = 200;
    
    public ReadDTANetwork()
    {
        super();
    }
    
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
    
    public void changeType(DTAProject project, Map<Integer, Double> proportionMap) throws IOException
    {
        Map<Integer, Map<Integer, Map<Integer, Double>>> demands = new TreeMap<Integer, Map<Integer, Map<Integer, Double>>>();
        
        Scanner filein = new Scanner(project.getDynamicODFile());
        
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
                Map<Integer, Double> temp2 = temp.get(o);
                
                for(int t : temp2.keySet())
                {
                    double total = temp2.get(t);
                    
                    for(int type : proportionMap.keySet())
                    {
                        double dem = total * proportionMap.get(type);
                        
                        fileout.println((new_id++)+"\t"+type+"\t"+o+"\t"+d+"\t"+t + "\t" + dem);            
                    }
                }
            }
        }
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
            double vot = avdta.vehicle.VOT.dagum_rand(rand);
            fileout.println(v+"\t"+vot);
        }
        fileout.close();
        

        return total;
        
    }
    
    public List<Vehicle> readVehicles(DTAProject project) throws IOException
    {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        
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
    
    public static String getDemandFileHeader()
    {
        return "id\ttype\torigin\tdest\tdtime\tvot";
    }
    
    public static String getDemandProfileFileHeader()
    {
        return "id\tweight\tstart\tduration";
    }
    
    public static String getDynamicODFileHeader()
    {
        return "id\ttype\torigin\tdestination\tast\tdemand";
    }
    
    public static String getStaticODFileHeader()
    {
        return "id\ttype\torigin\tdestination\tdemand";
    }
}
