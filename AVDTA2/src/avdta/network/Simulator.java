/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network; 

import avdta.demand.StaticODTable;
import avdta.dta.DTAResults;
import avdta.duer.Incident;
import avdta.duer.IncidentEffect;
import avdta.network.ReadNetwork;
import avdta.network.cost.TravelCost;
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
import avdta.project.DemandProject;
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
import avdta.network.cost.FFTime;
import avdta.network.link.AbstractSplitLink;
import avdta.network.link.DLR2CTMLink;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.TransitLane;
import avdta.network.node.obj.MaxPressureObj;
import avdta.util.RunningAvg;
import avdta.vehicle.EmergencyVehicle;
import avdta.dta.Assignment;
import avdta.network.node.IntersectionControl;
import avdta.network.node.MPTurn;
import avdta.network.node.MaxPressure;

/**
 * A {@link Simulator} extends {@link Network} in adding {@link Vehicle}s and dynamic network loading. 
 * Where the {@link Network} works with {@link Node}s and {@link Link}s, {@link Simulator} methods are focused on {@link Vehicle}s.
 * @author Michael
 */
public class Simulator extends Network 
{

    
    public static int time;
    
    public static int duration = 3600*10;
    
    public static int ast_duration = 15*60;
    public static final int num_asts = duration / ast_duration ;

    
    /**
     * Calculates the assignment interval for the current time, based on the assignment duration.
     * @return {@link Simulator#time}/{@link Simulator#ast_duration}
     */
    public static int ast()
    {
        return time / ast_duration;
    }
    
/**
     * Calculates the assignment interval for the specified time, based on the assignment duration.
     * @param time the time (s)
     * @return {@code time}/{@link Simulator#ast_duration}
     */
    public static int ast(int time)
    {
        return (int)Math.min(time / ast_duration, num_asts-1);
    }
    
    
    public static PrintStream fileout;
    public static PrintStream vat;
    
    public static boolean print_status = true;
    public static boolean debug = true;
    
    

    /**
     * Indexes time into a time step
     * @param t the time to be indexed (s)
     * @return the time step index
     */
    public static final int indexTime(double t)
    {
        return (int)Math.ceil(t / Network.dt);
    }
    
    public static Simulator active;
    
    public static final int num_timesteps = (int)Math.ceil(Simulator.duration / Network.dt)+1;
    

    protected List<Vehicle> vehicles;
    

    
    private Project project;
    
  
    private int lastExit;
    
    
    
    public static Random rand;
    
    
    public static int centroid_time = 0;
    

    public PrintStream out;
    
    private boolean postProcessed;
    

    private boolean printQueueLength;
    
    protected StatusUpdate statusUpdate;
    
    private Set<EmergencyVehicle> emergency;
    
    
    /**
     * Constructs the Simulator for the given {@link Project}. 
     * This also constructs an empty {@link Network} (see {@link Network#Network()}).
     * @param project the {@link Project}
     */
    public Simulator(Project project)
    {
        this.project = project;
        rand = project.getRandom();
        this.out = System.out;
        vehicles = new ArrayList<Vehicle>();
        active = this;
        lastExit = duration;
        
        emergency = new HashSet<>();
    }
    
    
    /**
     * Constructs the Simulator for the given {@link Project} with the given {@link Node}s and {@link Link}s.
     * @param project the {@link Project}
     * @param nodes the set of {@link Node}s
     * @param links the set of {@link Link}s
     */
    public Simulator(Project project, Set<Node> nodes, Set<Link> links)
    {
        super(nodes, links);
        
        this.project = project;
        rand = project.getRandom();
        
        active = this;
        
        this.out = System.out;
   
        vehicles = new ArrayList<Vehicle>();
 
        time = 0;
        lastExit = duration;
        
        emergency = new HashSet<>();
    }
    
    
    private int queue_time_delay;
    private RunningAvg queue_length;
    
    public void recordQueueLengths(int time_delay)
    {
        printQueueLength = true;
        queue_time_delay = time_delay;
        queue_length = new RunningAvg();
    }
    
    public Set<EmergencyVehicle> getEmergencyVehicles()
    {
        return emergency;
    }
    
    public Incident getIncident()
    {
        return Incident.UNKNOWN;
    }
    
    
    /**
     * This method calculates turning proportions for nodes when max-pressure is used.
     */
    public void calculateTurningProportionsMP(Assignment assign) throws IOException
    {
        PathList paths = new PathList(this, assign.getPathsFile());
        
        StaticODTable table = new StaticODTable((DemandProject)getProject());
        
        for(Path p : paths)
        {
            p.flow = p.proportion * table.getTrips(p.getOrigin(), p.getDest());
            
        }
        
        table = null;
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(assign.getResultsDirectory()+"/turning_proportions.txt")), true);
        
        for(Node n : nodes)
        {
            if(n instanceof Intersection)
            {
                IntersectionControl c = ((Intersection)n).getControl();
                
                if(!(c instanceof MaxPressure))
                {
                    continue;
                }
                
                MaxPressure control = (MaxPressure)c;
                
                for(MPTurn turn : control.getTurns())
                {
                    for(Path p : paths)
                    {
                        int idx = p.indexOf(turn.i);
                        if(idx >= 0)
                        {
                            turn.denom += p.flow;
                            
                            if(p.get(idx+1) == turn.j)
                            {
                                turn.num += p.flow;
                            }
                        }
                    }
                    
                    if(turn.denom > 0)
                    {
                        turn.setTurningProportion(turn.num / turn.denom);
                    }
                    else
                    {
                        turn.setTurningProportion(0);
                    }
                    
                    fileout.println(turn.i.getId()+"\t"+turn.j.getId()+"\t"+turn.getTurningProportion());
                }
            }
        }
        fileout.close();
    }
    
    public void loadTurningProportionsMP(Assignment assign) throws IOException
    {
        Scanner filein = new Scanner(new File(assign.getResultsDirectory()+"/turning_proportions.txt"));
        
        Map<Integer, Link> linksmap = createLinkIdsMap();
        
        while(filein.hasNextInt())
        {
            int i_id = filein.nextInt();
            int j_id = filein.nextInt();
            double p_ij = filein.nextDouble();
            
            Link i = linksmap.get(i_id);
            Link j = linksmap.get(j_id);
            
            Node node = i.getDest();
            MaxPressure control = (MaxPressure)((Intersection)node).getControl();
            for(MPTurn t : control.getTurns())
            {
                if(t.i == i && t.j == j)
                {
                    t.setTurningProportion(p_ij);
                    break;
                }
            }
        }
        filein.close();
    }
    
    
    
    public boolean isObservable(Link l, Incident i)
    {
        return false;
    }
    
    /**
     * Sets the {@link StatusUpdate}, which is used for visualizing progress
     * @param s the new {@link StatusUpdate}
     * @see StatusUpdate
     */
    public void setStatusUpdate(StatusUpdate s)
    {
        statusUpdate = s;
    }
    
    /**
     * Returns the {@link Project} associated with this {@link Simulator}
     * @return the {@link Project} associated with this {@link Simulator}
     */
    public Project getProject()
    {
        return project;
    }
    

    
    
    /**
     * Sets the output stream to write the log file.
     * @param out the output stream to write the log file
     */
    public void setOutStream(PrintStream out)
    {
        this.out = out;
    }
    
    /**
     * Returns the output stream for writing the log file
     * @return the output stream for writing the log file
     */
    public PrintStream getOutStream()
    {
        return out;
    }
    
    
    /**
     * Iterates through all vehicles to find one matching the given id.
     * @param id the id to search for
     * @return a vehicle with the specified id, if one exists, or null otherwise.
     */
    public Vehicle findVehicle(int id)
    {
        for(Vehicle v : vehicles)
        {
            if(v.getId() == id)
            {
                return v;
            }
        }
        return null;
    }
    
    
    
    
    
    
    /**
     * Calculates the average link density as the average over all links.
     * @return the average link density (veh/mi)
     */
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
    

    /**
     * Returns a list of all vehicles in the network.
     * @return a list of all vehicles in the network
     */
    public List<Vehicle> getVehicles()
    {
        return vehicles;
    }
    
    /**
     * Returns the number of vehicles in the network.
     * @return the number of vehicles in the network
     */
    public int getNumVehicles()
    {
        return vehicles.size();
    }
    
    public int getNumVehiclesInSystem()
    {
        int output = 0;
        for(Link l : links)
        {
            output += l.getOccupancy();
        }
        
        return output;
    }
    
    /**
     * Returns the number of transit vehicles.
     * This method iterates through all vehicles and counts which vehicles are transit (see {@link Vehicle#isTransit()}).
     * @return the number of buses
     */
    public int getNumBuses()
    {
        int output = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.isTransit())
            {
                output++;
            }
        }
        
        return output;
    }
    
    
    /**
     * This method iterates through all vehicles and counts which ones have exited (see {@link Vehicle#isExited()}).
     * @return the number of exited vehicles
     */
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
    
    /**
     * Resets the simulator to restart the simulation.
     * This calls {@link Link#reset()} for all {@link Link}s, {@link Node#reset()} for all {@link Node}s, and {@link Vehicle#reset()} for all {@link Vehicle}s.
     */
    public void resetSim()
    {
        centroid_time = 0;
        
        lastExit = duration;
        postProcessed = false;

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
        
        emergency.clear();
    }

    /**
     * Returns the time the last vehicle exited, or the duration if a vehicle did not exit.
     * @return the time the last vehicle exited (s)
     */
    public int getLastExitTime()
    {
        return lastExit;
    }
    
    
    /**
     * Returns the total system travel time.
     * This method iterates over all vehicles, and sums their travel time (see {@link Vehicle#getTT()}).
     * @return the total system travel time(s)
     */
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
    
    /**
     * Returns the total free flow travel time.
     * This method iterates over all vehicles and finds the free flow path (see {@link FFTime}).
     * @return the total free flow travel time (s)
     */
    public double getFFTT()
    {
        double output = 0;
        
        
        int count = 0;
        for(Vehicle v : vehicles)
        {
            if(statusUpdate != null)
            {
                statusUpdate.update((double)count++ / vehicles.size(), 0, "Finding paths");
            }
            
            Path p = findPath((PersonalVehicle)v, TravelCost.ffTime);
            output += p.getFFTime();
            
            
        }
        
        if(statusUpdate != null)
        {
            statusUpdate.update(0.0, 0, "");
        }
        
        return output;
    }
    
    /**
     * Finds a path for the specified vehicle using the specified cost function.
     * This runs Dijkstra's and traces the shortest path for the relevant origin-destination pair.
     * Calls {@link Network#findPath(avdta.network.node.Node, avdta.network.node.Node, int, double, avdta.vehicle.DriverType, avdta.network.cost.TravelCost)} using the vehicle's parameters.
     * @param v the {@link Vehicle}
     * @param costFunc the cost function
     * @return the shortest path
     * @see TravelCost
     */
    public Path findPath(Vehicle v, TravelCost costFunc)
    {
        return findPath(v.getOrigin(), v.getDest(), v.getDepTime(), v.getVOT(), v.getDriver(), costFunc);
    }
    
    /**
     * Returns the total energy consumed.
     * This method iterates over all vehicles and sums their energy consumption (see {@link Vehicle#getTotalEnergy()}).
     * @return the total energy consumption
     */
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
    
    /**
     * Returns the total vehicle miles traveled.
     * This method iterates over all vehicles and sums their travel distance (see {@link Vehicle#getPath()} and {@link Path#getLength()}).
     * @return the total vehicle miles traveled
     */
    public double getTotalVMT()
    {
    	double output = 0;

    	for(Vehicle v : vehicles)
    	{
            if(v.getExitTime() < Simulator.duration)
            {
                    output += v.getVMT();
            }
    	}

    	return output;
    }
    
    /**
     * This returns the midpoint of the departure time for the specified assignment interval index.
     * The assignment interval index counts from 0 upwards.
     * @param ast the assignment interval index
     * @return ({@code ast})+0.5) * {@link Simulator#ast_duration}
     */
    public int getAvgDepTime(int ast)
    {
        return (int)Math.round((ast + 0.5) * ast_duration);
    }
    
    
    
    /**
     * Returns the average miles per gallon over all vehicles.
     * This method iterates over all vehicles and averages their miles per gallon (see {@link Vehicle#getMPG()}).
     * @return the average miles per gallon
     */
    public double getAvgMPG()
    {
        double total = 0;
        double count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.getExitTime() < Simulator.duration && v.getTotalEnergy() > 0)
            {
                total += v.getMPG() * v.getVMT();
                count += v.getVMT();
            }
        }
        
        return total / count;
    }


    
    /**
     * Returns the number of vehicles using the specified {@link Node}.
     * This method sums over all vehicles and checks whether their path contains the specified {@link Node} (see {@link Vehicle#getPath()} and {@link Path#containsNode(avdta.network.node.Node)}).
     * @param n the {@link Node} being checked
     * @return the number of vehicles using the specified {@link Node}
     */
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
    
    /**
     * This prints the {@link Node} demands for all {@link Node}s to the file {@link Project#getResultsFolder()}{@code /intersection_demand.txt}.
     * This calls {@link Simulator#getNodeDemand(avdta.network.node.Node)} for all {@link Node}s.
     * @throws IOException if the file cannot be accessed
     */
    public void printIntersectionDemands() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/intersection_demand.txt")), true);
        
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
    
    /*
     * This prints the {@link Link} volume/capacity ratios for all {@link Link}s to the file {@link Project#getResultsFolder()}{@code /link_vc.txt}.
     * This iterates over each {@link Link} and checks which vehicles use the {@link Link} for the volume counts.
     * It then divides by the {@link Link} capacity (see {@link Link#getCapacity()}).
     * @throws IOException if the file cannot be accessed
     */
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
    
    /**
     * Returns the average travel time for the given driver type. 
     * Note that all vehicles of the same type (i.e. all autonomous vehicles) should share the same {@link DriverType} instance.
     * @param driver the driver
     * @return the average travel time for the given driver type (s)
     */
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
    
    /**
     * Returns the average travel time for vehicles matching the specified type code
     * @param type the type code
     * @return the average travel time for vehicles matching the specified type code
     */
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
    
    /**
     * Returns average travel times for transit or non-transit vehicles
     * @param transit checks transit vehicles if true, checks non-transit vehicles if false
     * @return average travel times for transit or non-transit vehicles (s)
     */
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
    
    
     /**
     * Returns total travel times for transit  vehicles
     * @return total travel times for transit vehicles (s)
     */
    public double getBusTT()
    {
        return getBusTT(true);
        
    }
    /**
     * Returns total travel times for transit or non-transit vehicles
     * @param transit checks transit vehicles if true, checks non-transit vehicles if false
     * @return total travel times for transit or non-transit vehicles (s)
     */
    public double getBusTT(boolean transit)
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
    
    /**
     * Prints {@link Node} volume/capacity ratios to the file {@link Project#getResultsFolder()}{@code /node_vc.txt}.
     * This iterates over all vehicles and nodes, checking whether the {@link Vehicle} uses the {@link Node}.
     * @throws IOException if the file is not found
     */
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

    
    /**
     * Sets the vehicles to the specified list, and sorts them by departure time.
     * @param vehicles the new list of vehicles.
     */
    public void setVehicles(List<Vehicle> vehicles)
    {
        this.vehicles = vehicles;
        Collections.sort(vehicles);
    }
    
    protected int exit_count;
    protected int veh_idx;
    
    /**
     * Simulates (dynamic network loading) the network. 
     * The simulation runs until the duration is reached or all vehicles have exited.
     * This simultaneously writes the VAT (vehicle arrival time) file (see {@link Simulator#getVatFile()}).
     * For each time step, this calls {@link Simulator#addVehicles()} and {@link Simulator#propagateFlow()}.
     * @throws IOException if a file cannot be accessed
     */
    public void simulate() throws IOException
    {   
 
        resetSim();

        
        PrintStream sim_vat = null;
        
        vat = new PrintStream(new FileOutputStream(getVatFile()), true);
        
        veh_idx = 0;

        exit_count = 0;
        
        
        
        PrintStream queueLengthOut = null;
        
        if(printQueueLength)
        {
            queueLengthOut = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/queue_lengths.txt")), true);
            queueLengthOut.println("Time (s)\tTotal queue");
        }
        
        
        
        for(time = 0; time < duration; time += dt)
        {
            //System.out.println(time+"\t"+getNumVehiclesInSystem()+"\t"+occupancy_900);
            // push vehicles onto centroid connectors at departure time
            addVehicles();
            

            
            if(printQueueLength)
            {
                int queue = getNumVehiclesInSystem();
                
                queueLengthOut.println(time+"\t"+queue);
                
                if(time >= queue_time_delay)
                {
                    queue_length.add(queue);
                }
            }
            
            
            propagateFlow();

            if(isSimulationFinished())
            {
                break;
            }
            
            
            
            
        }
        

        lastExit = time;
        
        for(Vehicle v : vehicles)
        {
            if(!v.isExited())
            {
                Link l = v.getCurrLink();
                if(l != null)
                {
                    l.updateTT(v.enter_time, Simulator.time);
                }
            }
        }
        

        
        simulationFinished();
         
        vat.close();
        
        if(printQueueLength)
        {
            queueLengthOut.close();
            System.out.println("Average queue length: "+queue_length.getAverage());
        }
        
        
    }
    
    public double calcAvgBusSpeed()
    {
        double output = 0.0;
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.isTransit())
            {
                output +=  v.getPath().getLength() / (v.getTT() / 3600.0);
                count++;
            }
        }
        
        return output/count;
    }
    
    public double calcAvgBusStDev()
    {
        double output = 0.0;
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.isTransit())
            {
                output += v.getPath().getStDevTT(v.getDepTime()) / v.getTT();
                count++;
            }
        }
        
        return output/count;
    }
    
    /**
     * Post-process simulation data to prepare for printing results, such as average link flows.
     * This contains an internal check to avoid post-processing multiple times.
     */
    public void postProcess()
    {
        if(postProcessed)
        {
            return;
        }
        
        postProcessed = true;
        
        for(Link l : links)
        {
            l.postProcessFlowin();
        }
    }
    
    /**
     * This file creates the {@code sim.vat} file, which is a VISTA output, from the vehicle arrival times file.
     * @param outputFile the output file
     * @throws IOException if a file cannot be accessed.
     */
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
            
            fileout.println();

            for(int i = 0; i < route.size(); i++)
            {
                fileout.print(" "+route.get(i).getId()+" "+
                        times.get(i)+".00");
            }
            fileout.println();
        }
        
        fileout.close();
    }
    
    /**
     * Returns the vehicle arrival times file.
     * @return {@link Project#getResultsFolder()}{@code /vat.dat}
     */
    public File getVatFile()
    {
        return new File(project.getResultsFolder()+"/vat.dat");
    }
    
    /**
     * This method is called when the simulation has finished.
     */
    public void simulationFinished()
    {
        
    }
    
    /**
     * This method prints the flowin.csv and linktt.csv files, which are VISTA outputs used for visualization.
     * This also calls {@link Simulator#postProcess()}.
     * @throws IOException if a file cannot be accessed
     */
    public void printLinkTdd() throws IOException
    {
        postProcess();
        

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
    
    /**
     * Returns whether all vehicles have exited.
     * @return whether all vehicles have exited
     */
    protected boolean isSimulationFinished()
    {
        return exit_count == vehicles.size();
    }
    
    /**
     * This iterates through all vehicles. 
     * If the simulation time ({@link Simulator#time}) has passed the vehicle's departure time, the vehicle is added to its starting link.
     */
    public void addVehicles()
    {
        List<Vehicle> vehicles = getVehicles();
        
        while(veh_idx < vehicles.size())
        {
            Vehicle v = vehicles.get(veh_idx);

            if(v.getPath() == null)
            {
                veh_idx++;
            }
            else if(v.getDepTime() <= Simulator.time)
            {
                v.entered();
                v.getNextLink().addVehicle(v);
                
                if(v instanceof EmergencyVehicle)
                {
                    emergency.add((EmergencyVehicle)v);
                }
                veh_idx++;
            }
            else
            {
                break;
            }
        }
    }
    
    /**
     * This executes one time step of simulation.
     * This calls {@link Link#prepare()}, {@link Link#step()}, {@link Node#step}, then {@link Link#update()} for all {@link Link}s and {@link Node}s.
     */
    public void propagateFlow()
    {
        if(isDLR())
        {
            MaxPressureObj pressureCalc = new MaxPressureObj();

            for(Link l : links)
            {
                l.pressure_terms = null;

                pressureCalc.calculatePressure(l, l.getDest());
            }
        }
        
        for(Node n : nodes)
        {
            n.prepare();
        }
        
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
    
    
    /**
     * This calls {@link Simulator#printLinkFlow(int, int, File)} with the file {@link Project#getResultsFolder()}{@code /linkq.txt}.
     * @param start the start time (s)
     * @param end the end time (s)
     * @throws IOException if a file cannot be accessed
     */
    public void printLinkFlow(int start, int end) throws IOException
    {
        printLinkFlow(start, end, new File(project.getResultsFolder()+"/linkq.txt"));
    }
    
    /**
     * This prints the average link flows for each assignment interval index within the start and end times.
     * @param start the start time (s)
     * @param end the end time (s)
     * @param file the output file
     * @throws IOException if a file cannot be accessed
     */
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
    
    /**
     * This calculates the number of active vehicles by iterating over all non-centroid connector {@link Link}s.
     * @return the number of active vehicles
     */
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
    
    /**
     * This calls {@link Simulator#printLinkTT(int, int, java.io.File)} with the file {@link Project#getResultsFolder()}{@code /linkq.txt}.
     * @param start the start time (s)
     * @param end the end time (s)
     * @throws IOException if a file cannot be accessed
     */
    public void printLinkTT(int start, int end) throws IOException
    {
        printLinkTT(start, end, new File(project.getResultsFolder()+"/linktt.txt"));
    }
    
    /**
     * This prints average link travel times for all assignment interval indexes between the start and end times.
     * @param start the start time (s)
     * @param end the end time (s)
     * @param file the output file
     * @throws IOException if a file cannot be accessed
     */
    public void printLinkTT(int start, int end, File file) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        
        fileout.print("Link id\tType\tFFtime");
        
        for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
        {
            fileout.print("\t"+t);
        }
        
        fileout.println();
        
        for(Link l : links)
        {
            fileout.print(l.getId()+"\t"+l.getType()+"\t"+l.getFFTime());
            
            for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
            {
                fileout.print("\t"+l.getAvgTT(t));
            }
            
            fileout.println();
        }
        
        fileout.close();
    }
    
    /**
     * This prints average link travel times for all assignment interval indexes between the start and end times.
     * @param start the start time (s)
     * @param end the end time (s)
     * @param file the output file
     * @param linkids the set of links to include
     * @throws IOException if a file cannot be accessed
     */
    public void printLinkTT(int start, int end, File file, Set<Integer> linkids) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        
        fileout.print("Link id\tFFtime");
        
        for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
        {
            fileout.print("\t"+t);
        }
        
        fileout.println();
        
        for(Link l : links)
        {
            if(linkids.contains(l.getId()))
            {
                fileout.print(l.getId()+"\t"+l.getFFTime());

                for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
                {
                    fileout.print("\t"+l.getAvgTT(t));
                }

                fileout.println();
            }
        }
        
        fileout.print("Total\t");
        
        double total = 0.0;
        
        for(Link l : links)
        {
            if(linkids.contains(l.getId()))
            {
                total += l.getFFTime();
            }
        }
        
        fileout.print(total);
        
        for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
        {
            total = 0;
            
            for(Link l : links)
            {
                if(linkids.contains(l.getId()))
                {
                    total += l.getAvgTT(t);
                }
            }
            fileout.print("\t"+total);
        }
        
        fileout.close();
    }
    
    
    /**
     * This prints average link flows for all assignment interval indexes between the start and end times.
     * Note that this only works for LTM links.
     * @param start the start time (s)
     * @param end the end time (s)
     * @param file the output file
     * @param linkids the set of links to include
     * @throws IOException if a file cannot be accessed
     */
    public void printLinkFlow(int start, int end, File file, Set<Integer> linkids) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        
        fileout.print("Link id");
        
        for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
        {
            fileout.print("\t"+t);
        }
        
        fileout.println();
        
        for(Link l : links)
        {
            if((l instanceof LTMLink) && linkids.contains(l.getId()))
            {
                fileout.print(l.getId());

                for(int t = (start / ast_duration) * ast_duration; t <= end; t += ast_duration)
                {
                    fileout.print("\t"+l.getAvgFlow(t));
                }

                fileout.println();
            }
        }
        

        fileout.close();
    }
    
    
    
    /**
     * This calculates the number of vehicles waiting on centroid connectors.
     * @return the number of vehicles waiting on centroid connectors
     */
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
    
    /**
     * This calculates the total travel time of buses at free flow.
     * This method iterates all vehicles and sums {@link Vehicle#getPath()} and {@link Path#getFFTime()} for transit vehicles.
     * @return the total travel time of buses at free flow
     */
    public double getBusFFTime()
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
    
    /**
     * This calculates the average bus travel time ratio.
     * This iterates over all vehicles and averages the travel time ratio for transit vehicles.
     * The travel time ratio is {@link Vehicle#getTT()}/the vehicle free flow travel time.
     * @return the average bus travel time ratio
     */
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
    
    /**
     * This calculates the average bus travel time ratio.
     * This iterates over all vehicles and averages the travel time ratio for transit vehicles.
     * The travel time ratio is {@link Vehicle#getTT()}/the vehicle free flow travel time.
     * @return the average bus travel time ratio
     */
    public double calcAvgEmergencyPercentDelay()
    {
        double output = 0;
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v instanceof EmergencyVehicle)
            {
                output += v.getTT() / v.getPath().getFFTime();
                count++;
            }
        }
        
        return output / count - 1;
    }
    
    /**
     * This calculates the average transit delay.
     * The delay is the travel time - the free flow travel time.
     * This iterates over all vehicles and sums the delay for transit vehicles.
     * @return the average transit delay
     */
    public double calcAvgBusDelay()
    {
        double output = 0;
        int count = 0;
        
        for(Vehicle v : vehicles)
        {
            if(v.isTransit())
            {
                output += v.getTT() - v.getPath().getFFTime();
                count++;
            }
        }
        
        return output / count;
    }
    
    public double calcAvgBusStDev(int time)
    {
        double output = 0;
        int count = 0;
        
        
        for(Link l : links)
        {
            if(l instanceof AbstractSplitLink)
            {
                TransitLane tl = ((AbstractSplitLink)l).getTransitLane();
                
                double stdev1 = tl.getStDevTT(time);
                double stdev2 = l.getStDevTT(time);
                
                if(stdev2 > 0)
                {
                    output += (stdev2 - stdev1) / stdev2;
                }
                
                count++;
            }
        }
        
        
        return output / count;
    }
    
    public double calcAvgTLSpeedChange(int time)
    {
        double output = 0;
        int count = 0;
        
        for(Link l : links)
        {
            if(l instanceof AbstractSplitLink)
            {
                TransitLane tl = ((AbstractSplitLink)l).getTransitLane();
                
                double tt1 = tl.getAvgTT(time);
                double tt2 = l.getAvgTT(time);
                
                output += (tt2 - tt1) / tt2;
                count++;
            }
        }
        
        return output / count;
    }
    
    
    /**
     * This prints bus travel times and free flow travel times to the specified file.
     * @param file the output file
     * @throws IOException if the file cannot be accessed
     */
    public void printBusTime(File file) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        fileout.println("Id\tRoute\tTT\tFFtime");
        for(Vehicle v : vehicles)
        {
            if(v instanceof Bus)
            {
                fileout.println(v.getId()+"\t"+((Bus)v).getRouteId()+"\t"+v.getTT()+"\t"+v.getPath().getFFTime());
            }
        }
        fileout.close();
    }
    
    
    
    
    /**
     * Returns a mapping of ids to vehicles.
     * @return a mapping of ids to vehicles
     */
    public Map<Integer, Vehicle> createVehicleIdsMap()
    {
        Map<Integer, Vehicle> output = new HashMap<Integer, Vehicle>();
        
        for(Vehicle v : vehicles)
        {
            output.put(v.getId(), v);
        }
        
        return output;
    }
    
    public boolean connectivityTest() throws Exception
    {
        
        Set<String> unconnected = new HashSet<String>();
        for(Vehicle v : getVehicles())
        {
            if(!(v instanceof PersonalVehicle))
            {
                continue;
            }
            
            PersonalVehicle veh = (PersonalVehicle)v;
            String od = veh.getOrigin()+"\t"+(int)Math.abs(veh.getDest().getId());
            
            if(unconnected.contains(od))
            {
                continue;
            }
            Path p = null;
            try
            {
                p = findPath((PersonalVehicle)v, TravelCost.ffTime);
            }
            catch(Exception ex){}
            
            if(p == null)
            {
                unconnected.add(od);
            }
        }
        
        PrintStream fileout =new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/unconnected.txt")), true);
        for(String x : unconnected)
        {
            fileout.println(x);
        }
        fileout.close();
     
        return unconnected.size() == 0;
    }
}
