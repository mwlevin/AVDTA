/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.demand;

import avdta.demand.AST;
import avdta.demand.DemandProfile;
import avdta.dta.VehicleRecord;
import avdta.network.ReadNetwork;
import avdta.project.DemandProject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * This includes methods to create and read demand
 * @author Michael
 */
public class ReadDemandNetwork extends ReadNetwork
{
    /**
     * Creates a new ReadDemandNetwork
     */
    public ReadDemandNetwork()
    {
        
    }
    
    /**
     * This generates the demand file using the demand_profile and dynamic_od files.
     * The demand file will be overwritten.
     * Vehicles are generated somewhat randomly. 
     * The integer number of vehicles is created, and fractional vehicles are discretized via random number generator.
     * @param project the project
     * @param prop the proportion of total demand
     * @return the number of vehicles generated
     * @throws IOException if a file cannot be accessed
     */
    public int prepareDemand(DemandProject project, double prop) throws IOException
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
        
        fileout.println(getDemandFileHeader());
        
        for(VehicleRecord v : vehicles)
        {
            double vot = avdta.vehicle.VOT.dagum_rand(rand);
            fileout.println(v+"\t"+vot);
        }
        fileout.close();
        

        return total;
        
    }
    
    /**
     * This generates the dynamic_od file from the static_od and demand_profile files.
     * Note that the dynamic_od file will be overwritten.
     * The static_od is separated by assignment intervals according to the demand_profile.
     * @param project the project
     * @throws IOException if a file cannot be accessed.
     */
    public void createDynamicOD(DemandProject project) throws IOException
    {
        DemandProfile profile = readDemandProfile(project);
        
        Scanner filein = new Scanner(project.getStaticODFile());
        filein.nextLine();
        
        if(!filein.hasNextInt())
        {
            filein.close();
            
            return;
        }
        
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDynamicODFile()), true);
        
        fileout.println("id\ttype\torigin\tdest\tast\tdemand");
        
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
    
    /**
     * This reads the demand profile associated with the project.
     * @param project the project
     * @return the demand profile
     * @throws IOException if the file cannot be accessed
     * @see DemandProfile
     */
    public DemandProfile readDemandProfile(DemandProject project) throws IOException
    {
        return new DemandProfile(project);
        
    }
    
    /**
     * This returns the header for the demand file.
     * @return the header for the demand file
     */
    public static String getDemandFileHeader()
    {
        return "id\ttype\torigin\tdest\tdtime\tvot";
    }
    
    /**
     * This returns the header for the demand_profile file.
     * @return the header for the demand_profile file
     */
    public static String getDemandProfileFileHeader()
    {
        return "id\tweight\tstart\tduration";
    }
    
    /**
     * This returns the header for the dynamic_od file.
     * @return the header for the dynamic_od file
     */
    public static String getDynamicODFileHeader()
    {
        return "id\ttype\torigin\tdestination\tast\tdemand";
    }
    
    /**
     * This returns the header for the static_od file.
     * @return the header for the static_od file
     */
    public static String getStaticODFileHeader()
    {
        return "id\ttype\torigin\tdestination\tdemand";
    }
}
