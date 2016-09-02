/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network; 

import avdta.dta.DTAResults;
import avdta.network.ReadNetwork;
import avdta.cost.TravelCost;
import avdta.gui.util.StatusUpdate;
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
import avdta.network.node.policy.FCFSPolicy;
import avdta.network.node.Intersection;
import avdta.network.node.TBR;
import avdta.network.node.TrafficSignal;
import avdta.project.Project;
import avdta.vehicle.Bus;
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
    public static PrintStream vat;
    
    public static boolean print_status = true;
    
    

    
    public static final int indexTime(double t)
    {
        return (int)Math.ceil(t / Network.dt);
    }
    
    public static Simulator active;
    
    public static final int num_timesteps = (int)Math.ceil(Simulator.duration / Network.dt)+1;
    

    protected List<Vehicle> vehicles;
    

    
    private Project project;
    
  
    
    
    
    
    public static Random rand;
    
    
    public static int centroid_time = 0;
    

    public PrintStream out;
    

    
    protected StatusUpdate statusUpdate;
    
    public Simulator(Project project)
    {
        this.project = project;
        rand = project.getRandom();
        this.out = System.out;
        vehicles = new ArrayList<Vehicle>();
        active = this;
    }
    public Simulator(Project project, Set<Node> nodes, Set<Link> links)
    {
        super(nodes, links);
        
        this.project = project;
        rand = project.getRandom();
        
        active = this;
        
        this.out = System.out;
   
        vehicles = new ArrayList<Vehicle>();
 
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
    

    
    public List<Vehicle> getVehicles()
    {
        return vehicles;
    }
    
    public int getNumVehicles()
    {
        return vehicles.size();
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

    	for(Vehicle v : vehicles)
    	{
            if(v.getExitTime() < Simulator.duration)
            {
                 output += v.getTT();   
            }	
    	}

    	return output;
    }
    
    public double getFFTT()
    {
        double output = 0;
        
        
        int count = 0;
        for(Vehicle v : vehicles)
        {
            statusUpdate.update((double)count++ / vehicles.size(), 0, "Finding paths");
            
            Path p = findPath((PersonalVehicle)v, TravelCost.ffTime);
            output += p.getFFTime();
            
            
        }
        
        statusUpdate.update(0.0, 0, "");
        
        return output;
    }
    
    public Path findPath(PersonalVehicle v, TravelCost costFunc)
    {
        return findPath(v.getOrigin(), v.getDest(), v.getDepTime(), v.getVOT(), v.getDriver(), costFunc);
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
    
    public double getAvgTT(int type)
    {
        double total = 0.0;
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.getDriver().matches(type))
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
    
    public double getAvgBusTT(boolean transit)
    {
        double total = 0.0;
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.isTransit() == transit)
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

    
    
    public void setVehicles(List<Vehicle> vehicles)
    {
        this.vehicles = vehicles;
        Collections.sort(vehicles);
    }
    
    protected int exit_count;
    protected int veh_idx;
    
    
    public void simulate() throws IOException
    {   
        resetSim();
        
        PrintStream sim_vat = null;
        
        vat = new PrintStream(new FileOutputStream(getVatFile()), true);
        
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
         
        vat.close();
    }
    
    public void createSimVat(File outputFile) throws IOException
    {
        Map<Integer, List<Integer>> arrTimes = new HashMap<Integer, List<Integer>>();
        
        for(Vehicle v : vehicles)
        {
            arrTimes.put(v.getId(), new ArrayList<Integer>());
        }
        
        Scanner filein = new Scanner(getVatFile());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int link = filein.nextInt();
            int time = filein.nextInt();
            
            arrTimes.get(id).add(time);
        }
        
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(outputFile), true);
        
        for(Vehicle v : vehicles)
        {
            List<Integer> times = arrTimes.get(v.getId());
            fileout.println(v.getType()+" "+v.getId()+" "+times.get(0)+".00 "+times.get(times.size()-1)+".00");
        
            Path route = v.getPath();
            fileout.print(route.size());

            for(int i = 0; i < route.size(); i++)
            {
                fileout.print(" "+route.get(i).getId()+" "+times.get(i)+".00");
            }
            fileout.println();
        }
        
        fileout.close();
    }
    
    public File getVatFile()
    {
        return new File(project.getResultsFolder()+"/vat.dat");
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
    
    public void addVehicles()
    {
        
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
            fileout.print(l.getId()+"\t"+l.getType());
            
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
            fileout.print(l.getSource()+"\t"+l.getDest()+"\t"+l.getType()+"\t"+l.getFFTime());
            
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
    
    public double calcBusFFTime()
    {
        double output = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.isTransit())
            {
                output += v.getPath().getFFTime();
            }
        }
        
        return output;
    }
    
    public double calcAvgBusTimeRatio()
    {
        double output = 0;
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.isTransit())
            {
                output += v.getTT() / v.getPath().getFFTime();
                count++;
            }
        }
        
        return output / count;
    }
    
    public double calcBusTime()
    {
        double output = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.isTransit())
            {
                output += v.getTT();
                System.out.println(v.getId()+"\t"+((Bus)v).getRouteId()+"\t"+v.getTT()+"\t"+v.getPath().getFFTime());
            }
        }
        
        return output;
    }
    
    public void printBusTime(File file) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        fileout.println("Id\tRoute\tTT\tFFtime");
        for(Vehicle v : vehicles)
        {
            fileout.println(v.getId()+"\t"+((Bus)v).getRouteId()+"\t"+v.getTT()+"\t"+v.getPath().getFFTime());
        }
        fileout.close();
    }
}
