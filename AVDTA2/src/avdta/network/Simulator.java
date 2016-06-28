/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network; 

import avdta.dta.DTAResults;
import avdta.network.ReadNetwork;
import avdta.cost.TravelCost;
import avdta.gui.StatusUpdate;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.link.CTMLink;
import avdta.network.node.Node;
import avdta.network.Path;
import avdta.network.node.Zone;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.fuel.VehicleClass;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import avdta.network.node.FCFSPolicy;
import avdta.network.node.Intersection;
import avdta.network.node.TBR;
import avdta.project.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter; 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author ut
 */
public class Simulator extends Network 
{

    public static int time;
    
    public static int duration = 3600*10;
    
    public static int ast_duration = 15*60;
    public static final int num_asts = duration / ast_duration ;
    
    
    public static int ast()
    {
        return time / ast_duration;
    }
    

    public static int ast(int time)
    {
        return (int)Math.min(time / ast_duration, num_asts-1);
    }
    
    
    public static PrintStream fileout;
    
    
    public static boolean print_status = true;
    
    

    
    public static final int indexTime(double t)
    {
        return (int)Math.ceil(t / Network.dt);
    }
    
    public static Simulator active;
    
    public static final int num_timesteps = (int)Math.ceil(Simulator.duration / Network.dt)+1;
    

    protected List<PersonalVehicle> vehicles;
    

    
    private Project project;
    
  
    
    
    
    
    public static Random rand;
    
    
    public static int centroid_time = 0;
    

    public PrintStream out;
    

    
    protected StatusUpdate statusUpdate;
    
    public Simulator(Project project)
    {
        this.project = project;
    }
    public Simulator(Project project, List<Node> nodes, List<Link> links)
    {
        super(nodes, links);
        
        this.project = project;
        
        active = this;
        rand = project.getRandom();
        
        this.out = System.out;
   
        vehicles = new ArrayList<PersonalVehicle>();
 
        time = 0;
    }
    

    public void setStatusUpdate(StatusUpdate s)
    {
        statusUpdate = s;
    }
    
    public Project getProject()
    {
        return project;
    }
    

    public void importResults() throws IOException
    {
        PrintStream path_out = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/vehicle_path.txt")), true);
        PrintStream veh_out = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/vehicle_path_time.txt")), true);
        
        Set<Integer> printed_paths = new HashSet<Integer>();
        
        // vehicle_path: id, origin, dest, hash, size, freeflowtt, length, issimpath, links
        // vehicle_path_time: id, type, dta_departure, sim_departure, sim_exittime, dta_path, sim_path, arrivaltime
        
        for(PersonalVehicle v : vehicles)
        {
            Path p = v.getPath();
            
            if(printed_paths.add(p.getId()))
            {
                path_out.print(p.getId()+"\t"+p.getOrigin()+"\t"+p.getDest()+"\t"+p.hashCode()+"\t"+p.size()+"\t"+p.getFFTime()+"\t"+p.getLength()+"\t2\t{");
                
                path_out.print(p.get(0));
                for(int i = 1; i < p.size(); i++)
                {
                    path_out.print(","+p.get(i));
                }
                path_out.println("}");
            }
            
            veh_out.print(v.getId()+"\t"+v.getType()+"\t"+v.getDepTime()+"\t"+v.getDepTime()+"\t"+v.getExitTime()+"\t"+p.getId()+"\t"+p.getId()+"\t{");
            int[] arr_times = v.getArrivalTimes();
            
            veh_out.print(arr_times[0]);
            for(int i = 1; i < arr_times.length; i++)
            {
                veh_out.print(","+arr_times[i]);
            }
            
            veh_out.print("}");
            veh_out.println();
        }
        
        path_out.close();
        veh_out.close();
        
        printLinkTdd();
    }
    
    
    
    
    
    public void setOutStream(PrintStream out)
    {
        this.out = out;
    }
    
    public PrintStream getOutStream()
    {
        return out;
    }
    
    public void save(File file)
    {
        try
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
            
            out.writeObject(this);
            out.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    

    
    public void writeVehicleResults() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/vehicles.txt")), true);
        
        fileout.println("ID\tOrigin\tDest\tDtime\tTT\tMPG\tTime waiting");
        for(PersonalVehicle v : vehicles)
        {
            fileout.println(v.getId()+"\t"+v.getOrigin()+"\t"+v.getDest()+"\t"+v.getDepTime()+"\t"+v.getTT()+"\t"+v.getMPG()+"\t"+v.getTimeWaiting());
        }
        
        fileout.close();
    }
    
    
    
    
    
    public double getAvgLinkDensity()
    {
        double output = 0;
        int count = 0;
        
        for(Link l : links)
        {
            if(!l.isCentroidConnector())
            {
                output += l.getDensity();
                count ++;
            }
        }
        
        return output / count;
    }
    

    
    public List<PersonalVehicle> getVehicles()
    {
        return vehicles;
    }
    
    public int getNumVehicles()
    {
        return vehicles.size();
    }
    
    public List<Link> getLinks()
    {
        return links;
    }
    
    
    
    
    public int getNumExited()
    {
        int count = 0;
        for(Vehicle v : vehicles)
        {
            if(v.isExited())
            {
                count++;
            }
        }
        
        return count;
    }
    
    public void resetSim()
    {
        centroid_time = 0;

        for(Link l : links)
        {
            l.reset();
        }

        for(Node n : nodes)
        {
            n.reset();
        }

        for(Vehicle v : vehicles)
        {
            v.reset();
        }
    }

    
    
    
    
    public double getTSTT()
    {
    	double output = 0;

    	for(PersonalVehicle v : vehicles)
    	{
            if(v.getExitTime() < Simulator.duration)
            {
                 output += v.getTT();   
            }	
    	}

    	return output;
    }
    
    public double getTotalEnergy()
    {
    	double output = 0;

    	for(Vehicle v : vehicles)
    	{
    		if(v.getExitTime() < Simulator.duration)
    			output += v.getTotalEnergy();
    	}

    	return output;
    }
    
    public double getTotalVMT()
    {
    	double output = 0;

    	for(Vehicle v : vehicles)
    	{
    		if(v.getExitTime() < Simulator.duration)
    			output += v.getPath().getLength();
    	}

    	return output;
    }
    
    public int getAvgDepTime(int ast)
    {
        return (int)Math.round((ast + 0.5) * ast_duration);
    }
    
    
    
    
    public double getAvgMPG()
    {
        double total = 0;
        double count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.getExitTime() < Simulator.duration && v.getTotalEnergy() > 0)
            {
                total += v.getMPG();
                count++;
            }
        }
        
        return total / count;
    }


    
    
    public int getNodeDemand(Node n)
    {
        int output = 0;
        for(Vehicle v : vehicles)
        {
            if(v.getPath() != null && v.getPath().containsNode(n))
            {
                output ++;
            }
        }
        
        return output;
    }
    
    public void printIntersectionDemands() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("intersection_demand.txt")), true);
        
        fileout.println("Intersection\tDemand");
        for(Node n : nodes)
        {
            if(n instanceof Intersection)
            {
                fileout.println(n.getId()+"\t"+getNodeDemand(n));
            }
        }
        
        fileout.close();
    }
    
    public void printLinkVC() throws IOException
    {
        for(Link l : links)
        {
            l.dem = 0;
        }
        
        for(Vehicle v : vehicles)
        {
            if(v.getPath() == null)
            {
                continue;
            }
            
            for(Link l : v.getPath())
            {
                l.dem += 1.0;
            }
        }
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/link_vc.txt")), true);
        
        fileout.println("Id\tSource\tDest\tDemand\tV/C");
        
        
        for(Link l : links)
        {
            if(!l.isCentroidConnector())
            {
                fileout.println(l.getId()+"\t"+l.getSource()+"\t"+l.getDest()+"\t"+l.dem+"\t"+(l.dem/l.getCapacity()));
            }
        }
        
        fileout.close();
    }
    
    public double getAvgTT(DriverType driver)
    {
        double total = 0.0;
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.getDriver() == driver)
            {
                count++;
                total += v.getTT();
            }
        }
        
        if(count > 0)
        {
            return total/count;
        }
        else
        {
            return 0;
        }
    }
    
    public void printNodeVC() throws IOException
    {
        for(Node n : nodes)
        {
            n.vc = 0;
        }
        
        for(Vehicle v : vehicles)
        {
            if(v.getPath() == null)
            {
                continue;
            }
            
            for(Link l : v.getPath())
            {
                l.getDest().vc += 1.0/l.getCapacity();
            }
        }
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/node_vc.txt")), true);
        
        fileout.println("Node\tV/C");
        for(Node n : nodes)
        {
            if(n instanceof Intersection)
            {
                fileout.println(n.getId()+"\t"+n.vc);
            }
        }
        
        fileout.close();
    }

    
    
    public void setVehicles(List<PersonalVehicle> vehicles)
    {
        this.vehicles = vehicles;
    }
    
    protected int exit_count;
    protected int veh_idx;
    
    public void simulate() throws IOException
    {
        simulate(true);
    }
    public void simulate(boolean recording) throws IOException
    {
        
        
        
        
        
        resetSim();
        
        PrintStream sim_vat = null;
        
        if(recording)
        {
            File file = new File(project.getResultsFolder()+"/sim.vat");

            sim_vat = new PrintStream(new FileOutputStream(file), true);
            
            fileout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/cell_enter.txt")), true);
            fileout.println("Vehicle\tLink\tTime");
        }
        
        veh_idx = 0;

        exit_count = 0;
        
        for(time = 0; time < duration; time += dt)
        {
            // push vehicles onto centroid connectors at departure time

            addVehicles();
            
            
            propagateFlow();

            if(isSimulationFinished())
            {
                break;
            }
        }
        

        
        int last_exit = time;
        
        simulationFinished();
         
        if(recording)
        {
            for(Vehicle v : vehicles)
            {
                int[] arr_times = v.getArrivalTimes();

                sim_vat.println(v.getType()+" "+v.getId()+" "+arr_times[0]+".00 "+arr_times[arr_times.length-1]+".00");

                Path route = v.getPath();
                sim_vat.print(route.size());

                for(int i = 0; i < arr_times.length; i++)
                {
                    sim_vat.print(" "+route.get(i).getId()+" "+arr_times[i]+".00");
                }
                sim_vat.println();
            }


            sim_vat.close();
        
            printLinkTdd();
        }
    }
    
    public void simulationFinished()
    {
        
    }
    

    public void printLinkTdd() throws IOException
    {
        for(Link l : links)
        {
            l.postProcessFlowin();
        }
        

        PrintStream flowout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/flowin.csv")), true);
        PrintStream ttout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/linktt.csv")), true);
        
        flowout.println(ast_duration);
        ttout.println(ast_duration);
        
        flowout.print(",");
        ttout.print(",");
        
        for(Link l : links)
        {
            flowout.print(","+l.getId());
            ttout.print(","+l.getId());
        }
        
        flowout.println();
        ttout.println();
        
        for(int t = 0; t < duration; t += ast_duration)
        {
            flowout.print(t);
            ttout.print(t);
            
            for(Link l : links)
            {
                flowout.print(","+l.getFlowin(t));
                ttout.print(","+l.getAvgTT(t));
            }
            
            flowout.println();
            ttout.println();
        }
        
        flowout.close();
        ttout.close();
    }
    
    
    protected boolean isSimulationFinished()
    {
        return exit_count == vehicles.size();
    }
    
    protected void addVehicles()
    {
        while(veh_idx < vehicles.size())
        {
            PersonalVehicle v = vehicles.get(veh_idx);

            if(v.getPath() == null)
            {
                veh_idx++;
            }
            else if(v.getDepTime() <= Simulator.time)
            {
                v.entered();
                v.getNextLink().addVehicle(v);
                veh_idx++;
            }
            else
            {
                break;
            }
        }
    }
    
    protected void propagateFlow()
    {
        for(Link l : links)
        {
            l.prepare();
        }
        

        for(Link l : links)
        {
            l.step();
        }

        for(Node n : nodes)
        {
            exit_count += n.step();
        }

        for(Link l : links)
        {
            l.update();
        }
        
    }
    
    
    
    public void printLinkFlow(int start, int end) throws IOException
    {
        printLinkFlow(start, end, new File(project.getResultsFolder()+"/linkq.txt"));
    }
    
    public void printLinkFlow(int start, int end, File file) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        
        fileout.print("Link\tType");
        
        for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
        {
            fileout.print("\t"+t);
        }
        
        fileout.println();
        
        for(Link l : links)
        {
            fileout.print(l.getId()+"\t"+l.strType());
            
            for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
            {
                fileout.print("\t"+(l.getAvgFlow(t)));
            }
            
            fileout.println();
        }
        
        fileout.close();
    }
    
    public int getActiveVehicles()
    {
        int output = 0;
        
        for(Link l : links)
        {
            if(!l.isCentroidConnector())
            {
                output += l.getOccupancy();

            }
            
            
        }
        
        return output;
    }
    
    public void printLinkTT(int start, int end) throws IOException
    {
        printLinkTT(start, end, new File(project.getResultsFolder()+"/linktt.txt"));
    }
    public void printLinkTT(int start, int end, File file) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        
        fileout.print("Link source\tLink dest\tType\tFFtime");
        
        for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
        {
            fileout.print("\t"+t);
        }
        
        fileout.println();
        
        for(Link l : links)
        {
            fileout.print(l.getSource()+"\t"+l.getDest()+"\t"+l.strType()+"\t"+l.getFFTime());
            
            for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
            {
                fileout.print("\t"+l.getAvgTT(t));
            }
            
            fileout.println();
        }
        
        fileout.close();
    }
    
    public int getWaitingVehicles()
    {
        int output = 0;
        
        for(Link l : links)
        {
            if(l.isCentroidConnector())
            {
                output += l.getOccupancy();
            }
        }
        
        return output;
    }
    


    
}
