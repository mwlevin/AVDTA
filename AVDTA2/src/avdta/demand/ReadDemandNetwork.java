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
import static avdta.network.ReadNetwork.DATA_MISMATCH;
import avdta.project.DemandProject;
import avdta.project.Project;
import avdta.vehicle.VOT;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
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
            
            if(ast == null)
            {
                throw new RuntimeException("Cannot find AST for id "+t);
            }
            
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
                
                vehicles.add(new VehicleRecord(new_id++, type, origin, dest, dtime, avdta.vehicle.VOT.dagum_rand(rand)));
            }
            
            total += num_vehicles;
           
        }
        
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDemandFile()), true);
        
        fileout.println(getDemandFileHeader());
        
        for(VehicleRecord v : vehicles)
        {
            fileout.println(v);
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
    
    /**
     * This changes the type of vehicles in the dynamic_od file to match the specified proportions.
     * The proportions should sum to 1.
     * This rewrites the dynamic_od file.
     * @param project the project 
     * @param proportionMap a mapping of type codes to proportions
     * @throws IOException if a file cannot be accessed
     */
    public void changeDynamicType(DemandProject project, Map<Integer, Double> proportionMap) throws IOException
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
     * This changes the type of vehicles in the static_od file to match the specified proportions.
     * The proportions should sum to 1.
     * This rewrites the static_od file.
     * @param project the project 
     * @param proportionMap a mapping of type codes to proportions
     * @throws IOException if a file cannot be accessed
     */
    public void changeStaticType(DemandProject project, Map<Integer, Double> proportionMap) throws IOException
    {
        Map<Integer, Map<Integer, Double>> demands = new TreeMap<Integer, Map<Integer, Double>>();
        
        Scanner filein = new Scanner(project.getStaticODFile());
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            filein.nextInt();
            int type = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            double demand = filein.nextDouble();
            filein.nextLine();
            
            Map<Integer, Double> temp;
            
            if(demands.containsKey(origin))
            {
                temp = demands.get(origin);
            }
            else
            {
                demands.put(origin, temp = new TreeMap<Integer, Double>());
            }

            
            if(temp.containsKey(dest))
            {
                temp.put(dest, temp.get(dest) + demand);
            }
            else
            {
                temp.put(dest, demand);
            }
        }
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getStaticODFile()), true);
        fileout.println(getStaticODFileHeader());
        
        int new_id = 1;
        for(int o : demands.keySet())
        {
            Map<Integer, Double> temp = demands.get(o);
            
            for(int d : temp.keySet())
            {
                double total = temp.get(d);

                for(int type : proportionMap.keySet())
                {
                    double dem = total * proportionMap.get(type);

                    if(dem > 0)
                    {
                        fileout.println((new_id++)+"\t"+type+"\t"+o+"\t"+d + "\t" + dem);     
                    }

                }
            }
        }
        fileout.close();
    }
    
    /**
     * Performs a sanity check on the network data contained within the {@link Project}.
     * @param project the {@link Project}
     * @param fileout the {@link PrintStream} to print errors to
     * @return the number of errors found
     */
    public int sanityCheck(DemandProject project, PrintStream fileout)
    {
        int output = super.sanityCheck(project, fileout);
        
        int lineno = 0;
        int count = 0;
        
        Scanner filein = null;
        
        fileout.println("<h2>Demand</h2>");
        
        
        fileout.println("<h3>"+project.getStaticODFile().getName()+"</h3>");
        
        Set<Integer> ids = new HashSet<Integer>();
        
        Set<Integer> possible_types = new HashSet<Integer>();
        
        int[] fuels = new int[]{ICV, BEV};
        int[] drivers = new int[]{HV, AV, CV};
        int[] vehicles = new int[]{DA_VEHICLE, BUS};
        
        for(int a : fuels)
        {
            for(int b : drivers)
            {
                for(int c : vehicles)
                {
                    possible_types.add(a+b+c);
                }
            }
        }
        
        try
        {
            filein = new Scanner(project.getStaticODFile());
            
            if(!filein.hasNextLine())
            {
                output++;
                print(fileout, BAD_FILE, project.getStaticODFile().getName()+" file is empty.");
            }
            else
            {
                filein.nextLine();
                lineno = 1;
                count = 0;
                
                
                while(filein.hasNextInt())
                {
                    try
                    {
                        lineno++;
                        StaticODRecord od = new StaticODRecord(filein.nextLine());
                        count++;
                        
                        if(od.getId() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "OD "+od.getId()+" has non-positive id on line "+lineno+".");
                        }
                        
                        if(ids.contains(od.getId()))
                        {
                           output++;
                           print(fileout, DATA_MISMATCH, "Duplicate OD id of "+od.getId()+" on line "+lineno+".");
                        }
                        else
                        {
                            ids.add(od.getId());
                        }
                        
                        if(!nodesmap.containsKey(od.getOrigin()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Origin "+od.getOrigin()+" does not appear in "+project.getNodesFile()+" on line "+lineno+".");
                        }
                        else if(!nodesmap.get(od.getOrigin()).isZone())
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Origin "+od.getOrigin()+" is not a zone on line "+lineno+".");
                        }
                        
                        if(!nodesmap.containsKey(od.getDest()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Destination "+od.getDest()+" does not appear in "+project.getNodesFile()+" on line "+lineno+".");
                        }
                        else if(!nodesmap.get(od.getDest()).isZone())
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Destination "+od.getDest()+" is not a zone on line "+lineno+".");
                        }
                        
                        if(!possible_types.contains(od.getType()))
                        {
                            output++;
                            print(fileout, ODD_DATA, "OD "+od.getId()+" type of "+od.getType()+" not recognized on line "+lineno+".");
                        }
                        
                        if(od.getDemand() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "OD "+od.getId()+" has demand of "+od.getDemand()+" on line "+lineno+".");
                        }
                    }
                    catch(Exception ex)
                    {
                        print(fileout, MALFORMED_DATA, "Malformed entry at line "+lineno+".");
                    }
                    
                    
                }
                
                print(fileout, NORMAL, "Scanned "+count+" entries.");
            }
        }
        catch(IOException ex)
        {
            output++;
            print(fileout, BAD_FILE, project.getStaticODFile().getName()+" file not found.");
        }
        
        
        fileout.println("<h3>"+project.getDemandFile().getName()+"</h3>");
        
        
        ids.clear();
        
        try
        {
            filein = new Scanner(project.getDemandFile());
            
            if(!filein.hasNextLine())
            {
                output++;
                print(fileout, BAD_FILE, project.getDemandFile().getName()+" file is empty.");
            }
            else
            {
                filein.nextLine();
                lineno = 1;
                count = 0;
                
                while(filein.hasNextInt())
                {
                    try
                    {
                        lineno++;
                        DemandRecord demand = new DemandRecord(filein.nextLine());
                        count++;
                        
                        if(demand.getId() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "Demand "+demand.getId()+" has non-positive id on line "+lineno+".");
                        }

                        if(ids.contains(demand.getId()))
                        {
                           output++;
                           print(fileout, DATA_MISMATCH, "Duplicate demand id of "+demand.getId()+" on line "+lineno+".");
                        }
                        else
                        {
                            ids.add(demand.getId());
                        }

                        if(!nodesmap.containsKey(demand.getOrigin()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Origin "+demand.getOrigin()+" does not appear in "+project.getNodesFile()+" on line "+lineno+".");
                        }
                        else if(!nodesmap.get(demand.getOrigin()).isZone())
                        {
                            output++;
                            print(fileout, ODD_DATA, "Origin "+demand.getOrigin()+" is not a zone on line "+lineno+".");
                        }

                        if(!nodesmap.containsKey(demand.getDest()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Destination "+demand.getDest()+" does not appear in "+project.getNodesFile()+" on line "+lineno+".");
                        }
                        else if(!nodesmap.get(demand.getDest()).isZone())
                        {
                            output++;
                            print(fileout, ODD_DATA, "Destination "+demand.getDest()+" is not a zone on line "+lineno+".");
                        }

                        if(!possible_types.contains(demand.getType()))
                        {
                            output++;
                            print(fileout, ODD_DATA, "Demand "+demand.getId()+" type of "+demand.getType()+" not recognized on line "+lineno+".");
                        }

                        
                        if(demand.getDepTime() < 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "Demand "+demand.getId()+" has departure time of "+demand.getDepTime()+" s on line "+lineno+".");
                        }
                        
                        if(demand.getVOT() < 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "Demand "+demand.getId()+" has value-of-time of "+demand.getVOT()+" $/hr on line "+lineno+".");
                        }
                    }
                    catch(Exception ex)
                    {
                        output++;
                        print(fileout, MALFORMED_DATA, "Malformed data on line "+lineno+".");
                    }
                }
                print(fileout, NORMAL, "Scanned "+count+" entries.");
            }
        }
        catch(IOException ex)
        {
            output++;
            print(fileout, BAD_FILE, project.getDemandFile().getName()+" file not found.");
        }
        
        fileout.println("<h3>"+project.getDemandProfileFile().getName()+"</h3>");
        
        
        ids.clear();
        try
        {
            filein = new Scanner(project.getDemandProfileFile());
            
            if(!filein.hasNextLine())
            {
                output++;
                print(fileout, BAD_FILE, project.getDemandProfileFile().getName()+" file is empty.");
            }
            else
            {
                filein.nextLine();
                lineno = 1;
                count = 0;
                
                while(filein.hasNextInt())
                {
                    try
                    {
                        lineno++;
                        DemandProfileRecord ast = new DemandProfileRecord(filein.nextLine());
                        count++;
                        
                        if(ast.getId() < 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "AST "+ast.getId()+" has negative id on line "+lineno+".");
                        }
                        
                        if(ids.contains(ast.getId()))
                        {
                           output++;
                           print(fileout, DATA_MISMATCH, "Duplicate AST id of "+ast.getId()+" on line "+lineno+".");
                        }
                        else
                        {
                            ids.add(ast.getId());
                        }
                        
                        if(ast.getWeight() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "AST "+ast.getId()+" has weight of "+ast.getWeight()+" on line "+lineno+".");
                        }
                        
                        if(ast.getStartTime() < 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "AST "+ast.getId()+" has start time of "+ast.getStartTime()+" s on line "+lineno+".");
                        }
                        
                        if(ast.getDuration() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "AST "+ast.getId()+" has duration of "+ast.getDuration()+" on line "+lineno+".");
                        }
                    }
                    catch(Exception ex)
                    {
                        print(fileout, MALFORMED_DATA, "Malformed data on line "+lineno+".");
                    }
                }
                
                print(fileout, NORMAL, "Scanned "+count+" entries.");
            }
        }
        catch(IOException ex)
        {
            output++;
            print(fileout, BAD_FILE, project.getDemandProfileFile().getName()+" file not found.");
            
        }
        
        DemandProfile profile = null;
        
        try
        {
            profile = readDemandProfile(project);
        }
        catch(Exception ex)
        {
            print(fileout, NORMAL, "Scanning ended due to errors in "+project.getDemandProfileFile());
            return output;
        }
        
        fileout.println("<h3>"+project.getDynamicODFile().getName()+"</h3>");
        
        ids.clear();
        
        try
        {
            filein = new Scanner(project.getDynamicODFile());
            
            if(!filein.hasNextLine())
            {
                output++;
                print(fileout, BAD_FILE, project.getDynamicODFile().getName()+" file is empty.");
            }
            else
            {
                filein.nextLine();
                lineno = 1;
                count = 0;
                
                while(filein.hasNextInt())
                {
                    try
                    {
                        lineno++;
                        DynamicODRecord od = new DynamicODRecord(filein.nextLine());
                        count++;

                        if(od.getId() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "OD "+od.getId()+" has non-positive id on line "+lineno+".");
                        }

                        if(ids.contains(od.getId()))
                        {
                           output++;
                           print(fileout, DATA_MISMATCH, "Duplicate OD id of "+od.getId()+" on line "+lineno+".");
                        }
                        else
                        {
                            ids.add(od.getId());
                        }

                        if(!nodesmap.containsKey(od.getOrigin()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Origin "+od.getOrigin()+" does not appear in "+project.getNodesFile()+" on line "+lineno+".");
                        }
                        else if(!nodesmap.get(od.getOrigin()).isZone())
                        {
                            output++;
                            print(fileout, ODD_DATA, "Origin "+od.getOrigin()+" is not a zone on line "+lineno+".");
                        }

                        if(!nodesmap.containsKey(od.getDest()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Destination "+od.getDest()+" does not appear in "+project.getNodesFile()+" on line "+lineno+".");
                        }
                        else if(!nodesmap.get(od.getDest()).isZone())
                        {
                            output++;
                            print(fileout, ODD_DATA, "Destination "+od.getDest()+" is not a zone on line "+lineno+".");
                        }

                        if(!possible_types.contains(od.getType()))
                        {
                            output++;
                            print(fileout, ODD_DATA, "OD "+od.getId()+" type of "+od.getType()+" not recognized on line "+lineno+".");
                        }

                        if(!profile.containsKey(od.getAST()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "OD "+od.getId()+" AST of "+od.getAST()+" not found in file "+project.getDemandProfileFile()+" on line "+lineno+".");
                        }
                        if(od.getDemand() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "OD "+od.getId()+" has demand of "+od.getDemand()+" on line "+lineno+".");
                        }
                    }
                    catch(Exception ex)
                    {
                        output++;
                        print(fileout, MALFORMED_DATA, "Malformed data on line "+lineno+".");
                    }
                }
                
                print(fileout, NORMAL, "Scanned "+count+" entries.");
            }
        }
        catch(IOException ex)
        {
            output++;
            print(fileout, BAD_FILE, project.getDynamicODFile().getName()+" file not found.");
        }
        
        
        
        return output;
    }
}
