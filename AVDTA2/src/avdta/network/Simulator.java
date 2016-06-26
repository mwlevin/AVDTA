/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network; 

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
import avdta.vehicle.VehicleClass;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import avdta.network.node.FCFSPolicy;
import avdta.network.node.Intersection;
import avdta.network.node.TBR;
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
public class Simulator implements Serializable 
{

    public static String type = "";
    public static int time;
    public static int dt = 6;
    public static int duration = 3600*10;
    public static int ast_duration = 15*60;
    public static final int num_asts = duration / ast_duration ;
    public static final int demand_duration = 3600*2;
    public static final int demand_asts = demand_duration / ast_duration;
    
    public static PrintStream fileout;
    
    
    public static boolean print_status = true;
    
    public static int ast()
    {
        return time / ast_duration;
    }
    
    public static void setTimestep(int linktype)
    {
        if(linktype == Link.CTM)
        {
        	dt = 6;
        }
        else if(linktype == Link.LTM)
        {
        	dt = 10;
        }
    }
    
    public static int ast(int time)
    {
        return (int)Math.min(time / ast_duration, num_asts-1);
    }
    
    
    public static Simulator readTBRNetwork(String network, int linktype) throws IOException
    {
        return readTBRNetwork(network, new FCFSPolicy(), Node.CR, linktype);
    }
    public static Simulator readTBRNetwork(String network, int linktype, String demandfile) throws IOException
    {
        return readTBRNetwork(network, new FCFSPolicy(), Node.CR, linktype, demandfile);
    }
    public static Simulator readTBRNetwork(String network, Object policy, int nodetype, int linktype) throws IOException
    {
        ReadNetwork input = new ReadNetwork();
        Simulator output = new Simulator(network);
        
        input.readNetwork(output, linktype);
        input.readTBR(output, policy, nodetype);
        input.readVehicles(output);
        
        output.initialize();
        
        return output;
    }
    public static Simulator readTBRNetwork(String network, Object policy, int nodetype, int linktype, String demandfile) throws IOException
    {
        ReadNetwork input = new ReadNetwork();
        Simulator output = new Simulator(network);
        
        input.readNetwork(output, linktype);
        input.readTBR(output, policy, nodetype);
        input.readVehicles(output, new File("data/"+network+"/"+demandfile));
        
        output.initialize();
        
        return output;
    }
    
    public static Simulator readSignalsNetwork(String network, int linktype) throws IOException
    {
        ReadNetwork input = new ReadNetwork();
        Simulator output = new Simulator(network);
        
        input.readNetwork(output, linktype);
        input.readSignals(output);
        input.readVehicles(output);
        
        output.initialize();
        
        return output;
    }
    
    public static Simulator readSignalsNetwork(String network, int linktype, String demandfile) throws IOException
    {
        ReadNetwork input = new ReadNetwork();
        Simulator output = new Simulator(network);
        
        input.readNetwork(output, linktype);
        input.readSignals(output);
        input.readVehicles(output, new File("data/"+network+"/"+demandfile));
        
        output.initialize();
        
        return output;
    }
    
    public static final int indexTime(double t)
    {
        return (int)Math.ceil( t / Simulator.dt);
    }
    
    public static Simulator active;
    
    public static final int num_timesteps = (int)Math.ceil(Simulator.duration / Simulator.dt)+1;
    
    protected List<Node> nodes;
    protected List<Link> links;
    protected List<PersonalVehicle> vehicles;
    
    private Map<Integer, List<Path>> allPaths;
    
    private String name, scenario;
    
    private static boolean dlr;
    
    
    public static double a = 22020.6;
    public static double b = 2.7926;
    public static double c = 0.2977;
    
    protected static boolean HVs_use_reservations = false;

    public static Random rand = new Random(5);
    
    public static double dagum_rand()
    {
        double y = rand.nextDouble();
        return Math.pow( a / (Math.pow(1/y, 1/c) - 1), 1/b);
    }
    
    
    
    private boolean link_dijkstras; // dijkstras
    
    public static int centroid_time = 0;
    
    private int linktype;
    
    private PrintStream out;
    
    private TravelCost costFunc;
    
    protected StatusUpdate statusUpdate;
    
    public Simulator(String name, List<Node> nodes, List<Link> links, int linktype)
    {
        this(name, nodes, links, false, linktype);
    }
    
    public Simulator(String name, List<Node> nodes, List<Link> links, boolean link_dijkstras, int linktype)
    { 
        active = this;
        
        this.out = System.out;
        this.name = name;
        scenario = "default";
        this.scenario = name;
        this.nodes = nodes;
        this.links = links;    
        this.linktype = linktype;
        vehicles = new ArrayList<PersonalVehicle>();
        
        this.link_dijkstras = link_dijkstras;
        
        this.costFunc = TravelCost.ttCost;
        
        setTimestep(linktype);
        
        for(Node n : nodes)
        {
            n.initialize();
        }
        
        allPaths = new HashMap<Integer, List<Path>>();
    }
    
    public Simulator(String name)
    {
        active = this;
        
        this.name = name;
        scenario = "default";
        this.out = System.out;
        this.scenario = name;
        vehicles = new ArrayList<PersonalVehicle>();
        
        allPaths = new HashMap<Integer, List<Path>>();
        this.link_dijkstras = false;
        this.costFunc = TravelCost.ttCost;
    }
    
    public void setStatusUpdate(StatusUpdate s)
    {
        statusUpdate = s;
    }
    
    public void setNodes(List<Node> nodes)
    {
        this.nodes = nodes;
    }
    
    public void setHVsUseReservations(boolean h)
    {
        HVs_use_reservations = h;
    }
    
    public void setLinks(List<Link> links, int linktype)
    {
        this.links = links;
        this.linktype = linktype;
        setTimestep(linktype);
    }
    
    public void setNetwork(List<Node> nodes, List<Link> links, int linktype)
    {
        setNodes(nodes);
        setLinks(links, linktype);
    }

    public void setScenario(String scenario)
    {
        this.scenario = scenario;
    }
    
    public String getScenario()
    {
        return scenario;
    }
    
    public String getName()
    {
        return name;
    }
    
    
    public void importResults() throws IOException
    {
        PrintStream path_out = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/vehicle_path.txt")), true);
        PrintStream veh_out = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/vehicle_path_time.txt")), true);
        
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
    
    public static void setDLR(boolean d)
    {
        dlr = d;
        
        
    }
    
    public void tieLinks()
    {  
        int count = 0;
        
        for(Node n : nodes)
        {
            for(Link i : n.getIncoming())
            {
                if(i.isCentroidConnector())
                {
                    continue;
                }
                
                for(Link j : n.getOutgoing())
                {
                    if(!j.isCentroidConnector() && i.getSource() == j.getDest())
                    {
                        if (((CTMLink)i).tieCells((CTMLink)j))
                        {
                            count++;
                        }
                    }
                    
                }
            }
        }
        
        System.out.println("Tied "+count+" links");
    }
    
    public static boolean isDLR()
    {
        return dlr;
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
    
    public static Simulator load(File file)
    {
        try
        {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
            
            Simulator output = (Simulator)in.readObject();
            in.close();
            
            return output;
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
            
            return null;
        }
    }
    
    public void writeVehicleResults() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/vehicles.txt")), true);
        
        fileout.println("ID\tOrigin\tDest\tDtime\tTT\tMPG\tTime waiting");
        for(PersonalVehicle v : vehicles)
        {
            fileout.println(v.getId()+"\t"+v.getOrigin()+"\t"+v.getDest()+"\t"+v.getDepTime()+"\t"+v.getTT()+"\t"+v.getMPG()+"\t"+v.getTimeWaiting());
        }
        
        fileout.close();
    }
    
    public void writeVehicles()
    {
        try
        {
            File dir = new File("save/"+name+"/"+scenario);
            dir.mkdirs();

            File path_file = new File("save/"+name+"/"+scenario+"/path.dat");
            File veh_file = new File("save/"+name+"/"+scenario+"/vehicles.dat");
            
            
            PrintStream path_out = new PrintStream(new FileOutputStream(path_file), true);
            PrintStream veh_out = new PrintStream(new FileOutputStream(veh_file), true);
            
            path_out.println(""+Path.next_id);
            
            Set<Integer> printed_paths = new HashSet<Integer>();
            
            for(PersonalVehicle v : vehicles)
            {
                Path p = v.getPath();
                veh_out.println(v.getId()+"\t"+v.getDepTime()+"\t"+p.getId()+"\t"+v.getDriver()+"\t"+v.getVehClass()+"\t"+v.getEfficiency());
                
                if(printed_paths.add(p.getId()))
                {
                    path_out.print(p.getId()+"\t"+p.size());
                    
                    for(Link l : p)
                    {
                        path_out.print("\t"+l);
                    }
                    
                    path_out.println();
                }
                
            }
            
            path_out.close();
            veh_out.close();
            
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    public void readVehicles()
    {
        try
        {
            
            File path_file = new File("save/"+name+"/"+scenario+"/path.dat");
            File veh_file = new File("save/"+name+"/"+scenario+"/vehicles.dat");
            
                Scanner path_in = new Scanner(path_file);
                Scanner veh_in = new Scanner(veh_file);
            
            vehicles.clear();
            
            // create nodes, links lookup maps
            
            Map<Integer, Link> link_lookup = new HashMap<Integer, Link>();
            
            for(Link l : links)
            {
                link_lookup.put(l.getId(), l);
            }

            Map<Integer, Path> path_lookup = new HashMap<Integer, Path>();

            Path.next_id = path_in.nextInt();
            
            while(path_in.hasNext())
            {
                int id = path_in.nextInt();
                int size = path_in.nextInt();
                
                Path p = new Path(id);
                
                for(int i = 0; i < size; i++)
                {
                    p.add(link_lookup.get(path_in.nextInt()));
                }
                
                path_lookup.put(id, p);
            }
            
            path_in.close();
            
            link_lookup = null;
            
            
            
            
            Map<Integer, Node> node_lookup = new HashMap<Integer, Node>();
            
            for(Node n : nodes)
            {
                node_lookup.put(n.getId(), n);
            }
            
            while(veh_in.hasNext())
            {
                int id = veh_in.nextInt();
                int dtime = veh_in.nextInt();
                
                Path p = path_lookup.get(veh_in.nextInt());
                
                Node origin = p.getOrigin();
                Node dest = p.getDest();
                
                DriverType driver = DriverType.getType(veh_in.next());
                VehicleClass vehClass = VehicleClass.getType(veh_in.next());
                double eff = veh_in.nextDouble();
                
                PersonalVehicle v = new PersonalVehicle(id, origin, dest, dtime, vehClass, driver);
                v.setPath(p);
                v.setEfficiency(eff);
                vehicles.add(v);
            }
            
            veh_in.close();
            
            Collections.sort(vehicles);
            
        }
        catch(IOException ex)
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
    
    public void setUseLinkDijkstras(boolean l)
    {
    	link_dijkstras = l;
    }
    
    public List<Node> getNodes()
    {
        return nodes;
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
    
    
    public Results pathgen(double stepsize) throws IOException
    {
        int error_count = 0;

        Map<Node, Map<Node, Path[][]>> newpaths = new HashMap<Node, Map<Node, Path[][]>>();

        double tstt = 0;
        double min = 0;
        int exiting = 0;

        int count = 0;
        
        for(PersonalVehicle v : vehicles)
        {
            Node o = v.getOrigin();
            Node d = v.getDest();
            int ast = v.getAST();
            int dep_time = v.getDepTime();

            if(v.getExitTime() < Simulator.duration)
            {
                exiting++;
            }

            Map<Node, Path[][]> temp1;
            Path[][] temp2;

            if(newpaths.containsKey(o))
            {
                temp1 = newpaths.get(o);
            }
            else
            {
                newpaths.put(o, temp1 = new HashMap<Node, Path[][]>());
            }

            if(temp1.containsKey(d))
            {
                temp2 = temp1.get(d);
            }
            else
            {
                temp1.put(d, temp2 = new Path[Simulator.num_asts][2]);
            }

            if(temp2[ast][v.getDriver().typeIndex()] == null)
            {
                temp2[ast][v.getDriver().typeIndex()] = findPath(o, d, getAvgDepTime(ast), v.getVOT(), v.getDriver(), costFunc);
            }
            
            

            if(v.getPath() != null)
            {
                tstt += v.getPath().getAvgCost(dep_time, v.getVOT(), costFunc);
                //tt += v.getTT();


                if(v.getTT() < 0)
                {
                	out.println("TT < 0 "+v.getDepTime()+" "+v.getExitTime());
                }
            }
            min += temp2[ast][v.getDriver().typeIndex()].getAvgCost(dep_time, v.getVOT(), costFunc);


            // move vehicle random chance
            if(v.getPath() == null || rand.nextDouble() < stepsize)
            {
                try
                {
                    v.setPath(temp2[ast][v.getDriver().typeIndex()]);
                }
                catch(Exception ex)
                {
                    out.println("Path unable: "+o+" "+d);
                    for(Link l : d.getIncoming())
                    {
                        out.println((l instanceof CentroidConnector)+" "+l.getSource());
                    }
                    error_count++;
                }
            }
            

            count ++;

        }
        
        if(error_count > 0)
        {
        	System.err.println("Unable: "+error_count);
        }

        simulate(false);
        

        if(tstt == 0)
        {
            tstt = min;
        }

        writeVehicles();
        
        return new Results(min, tstt, vehicles.size(), exiting);

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

    public Results partial_demand(int iter) throws IOException
    {
        List<PersonalVehicle> temp = new ArrayList<PersonalVehicle>();
        
        for(PersonalVehicle v : vehicles)
        {
            temp.add(v);
        }
        
        vehicles.clear();
        
        int count = 0;
        
        Results output = null;
        
        if(print_status)
        {
            out.println("Iter\tStep\tGap %\tAEC\tTTT\tTrips\tNon-exiting\ttime");
        }
        
        
        for(int i = 0; i < iter; i++)
        {
            long time = System.nanoTime();
            
            Iterator<PersonalVehicle> iterator = temp.iterator();
            
            while(iterator.hasNext())
            {
                if(Math.random() < 1.0 / (iter - i))
                {
                    vehicles.add(iterator.next());
                    iterator.remove();
                    count++;
                }
                else
                {
                    iterator.next();
                }
            }
            
            Collections.sort(vehicles);
            output = pathgen(0);
            
            time = System.nanoTime() - time;
            
            if(print_status)
            {
                out.println((i+1)+"\t"+String.format("%.4f", 0.0)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));
            }
        }
        
        return output;
    }
    
    private int iteration = 1;
    
    public int getIteration()
    {
        return iteration;
    }
    
    public Results msa(int max_iter) throws IOException
    {
        return msa(max_iter, -1);
    }
    
    public Results msa(int max_iter, double min_gap) throws IOException
    {
        return msa_cont(1, max_iter, min_gap);
    }
    
    public Results msa_cont(int max_iter, double min_gap) throws IOException
    {
        return msa_cont(iteration, max_iter, min_gap);
    }
    
    public Results msa_cont(int start_iter, int max_iter) throws IOException
    {
        return msa_cont(start_iter, max_iter, -1);
    }
    
    public Results msa_cont(int start_iter, int max_iter, double min_gap) throws IOException
    {
        if(statusUpdate != null)
        {
            statusUpdate.update(0);
        }
        
        File dir = new File("results/"+name+"/"+scenario);
        dir.mkdirs();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/log.txt")), true);
        
        iteration = start_iter;
        Results output = null;

        if(print_status)
        {
            out.println("Iter\tStep\tGap %\tAEC\tTTT\tTrips\tNon-exit\ttime");
        }
        
        fileout.println("Iter\tStep\tGap %\tAEC\tTTT\tTrips\tNon-exit\ttime");

        do
        {
            long time = System.nanoTime();

            double stepsize = 1.0/iteration;
            output = pathgen(stepsize);


            time = System.nanoTime() - time;

            if(print_status)
            {
                out.println(iteration+"\t"+String.format("%.4f", stepsize)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));
            }
            fileout.println(iteration+"\t"+String.format("%.4f", stepsize)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));

            if(statusUpdate != null)
            {
                statusUpdate.update((double)iteration / max_iter);
            }
        }
        while(iteration++ < max_iter && (iteration == 2 || min_gap < output.getGapPercent()));
        
        if(statusUpdate != null)
        {
            statusUpdate.update(1);
        }

        out.println(String.format("TSTT\t%.1f", getTSTT()/3600.0)+"\thr\nAvg. TT\t"+String.format("%.2f", getTSTT() / 60 / vehicles.size())+"\tmin/veh");
        
        fileout.println(String.format("TSTT\t%.1f", getTSTT()/3600.0)+"\thr\nAvg. TT\t"+String.format("%.2f", getTSTT() / 60 / vehicles.size())+"\tmin/veh");
        fileout.println("Energy:\t"+getTotalEnergy());
        fileout.println("VMT:\t"+getTotalVMT());
        fileout.println("MPG:\t"+(getTotalVMT() / (getTotalEnergy() / VehicleClass.E_PER_GALLON)));
        fileout.println("HV TT:\t"+(getAvgTT(DriverType.HV)/60)+"\tmin");
        fileout.println("AV TT:\t"+(getAvgTT(DriverType.AV)/60)+"\tmin");
        
        fileout.close();
        
        //simulate(true);
        writeVehicles();
        //importResults();

        return output;
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
    
    public int getLinkType()
    {
        return linktype;
    }
    
    
    public TravelCost getCostFunction()
    {
        return costFunc;
    }
    
    public void setCostFunction(TravelCost cost)
    {
        costFunc = cost;
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

    public Node getNode(int id)
    {
        for(Node n : nodes)
        {
            if(n.getId() == id)
            {
                return n;
            }
        }
        
        return null;
    }
    
    public Path findPath(Node o, Node d, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        node_dijkstras(o, dep_time, vot, driver, costFunc);
        Path output = node_trace(o, d);
        
        if(output.size() == 0)
        {
            link_dijkstras(o, dep_time, vot, driver, costFunc);
            output = link_trace(o, d);
        }
        
        int hash = output.hashCode();
        
        if(allPaths.containsKey(hash))
        {
            List<Path> temp = allPaths.get(hash);
            
            for(Path p : temp)
            {
                if(output.equals(p))
                {
                    return p;
                }
            }
            
            temp.add(output);
            return output;
        }
        else
        {
            ArrayList<Path> temp = new ArrayList<Path>();
            temp.add(output);
            allPaths.put(hash, temp);
            return output;
        }
    }

    
    public void dijkstras(Node o, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        dijkstras(o, dep_time, vot, driver, costFunc, link_dijkstras);
    }
    
    public void dijkstras(Node o, int dep_time, double vot, DriverType driver, TravelCost costFunc, boolean link_dijkstras)
    {
        if(link_dijkstras)
        {
            link_dijkstras(o, dep_time, vot, driver, costFunc);
        }
        else
        {
            node_dijkstras(o, dep_time, vot, driver, costFunc);
        }
    }
    

    public void link_dijkstras(Node o, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        for(Link l : links)
        {
            l.label = Integer.MAX_VALUE;
            l.arr_time = Integer.MAX_VALUE;
            l.prev = null;
        }
        
        Set<Link> Q = new HashSet<Link>();
        
        for(Link l : o.getOutgoing())
        {
            double tt = l.getAvgTT(dep_time);
            l.arr_time = (int)(dep_time + tt);
            double fuel = l.getAvgFuel(dep_time);
            
            //l.label = vot * (tt / 3600) + fuel;
            l.label = tt + 10;
            
            Q.add(l);
        }
        
        
        while(!Q.isEmpty())
        {
            double min = Integer.MAX_VALUE-1;
            Link u = null;
            
            for(Link l : Q)
            {
                if(l.label < min)
                {
                    min = l.label;
                    u = l;
                }
            }
            
            Q.remove(u);
            

            
            Node d = u.getDest();
            
            for(Link v : d.getOutgoing())
            {
                if(!d.canMove(u, v, driver))
                {
                    continue;
                }
                
                
                double tt = v.getAvgTT(u.arr_time);
                
                double new_label = u.label + costFunc.cost(v, vot, u.arr_time);

                
                if(new_label < v.label)
                {
                    v.arr_time = (int)(u.arr_time + tt);
                   
                    v.label = new_label;
                    v.prev = u;
                    
                    Q.add(v);
                }
            }
        }
        
    }
    
    

    public void node_dijkstras(Node o, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        for(Node n : nodes)
        {
            n.label = Integer.MAX_VALUE;
            n.prev = null;
        }

        o.arr_time = dep_time;
        o.label = 0;

        /*
        Set<Node> Q = new TreeSet<Node>(new Comparator<Node>()
        {
            public int compare(Node lhs, Node rhs)
            {
                return (int)(10000*(lhs.label - rhs.label));
            }
        });
        */
        
        Set<Node> Q = new HashSet<Node>();

        Q.add(o);


        while(!Q.isEmpty())
        {
            double min = Integer.MAX_VALUE-1;
            Node u = null;

            for(Node n : Q)
            {
                if(n.label < min)
                {
                    u = n;
                    min = n.label;
                }
            }

            Q.remove(u);

            for(Link l : u.getOutgoing())
            {
                
                if(u.prev != null && !u.canMove(u.prev, l, driver))
                {
                    continue;
                }
                
                
                

                Node v = l.getDest();

                double temp = u.label;
                int arr_time = u.arr_time;

                temp += costFunc.cost(l, vot, arr_time);
                
                if(!HVs_use_reservations && !driver.isAV() &&
                        (l.getDest() instanceof Intersection) &&
                        (((Intersection)l.getDest()).getControl() instanceof TBR))
                {
                    temp += 10000;
                }

                arr_time += (int)Math.round(l.getAvgTT(arr_time));

                if(temp < v.label)
                {
                    v.label = temp;
                    v.prev = l;
                    v.arr_time = arr_time;

                    if(!(v instanceof Zone))
                    {
                        Q.add(v);
                    }
                }
            }

        }
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
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/link_vc.txt")), true);
        
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
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/node_vc.txt")), true);
        
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

    public Path trace(Node o, Node d)
    {
        if(link_dijkstras)
        {
            return link_trace(o, d);
        }
        else
        {
            return node_trace(o, d);
        }
    }
    
    public Path node_trace(Node o, Node d)
    {
        Node curr = d;
        
        Path output = new Path();
        
        output.setCost(d.label);
        
        while(curr.prev != null)
        {
            
            output.add(0, curr.prev);
            curr = curr.prev.getSource();
        }
        
        return output;
    }
    
    public Path link_trace(Node o, Node d)
    {
        Path output = new Path();
        
        Link curr = null;
        double min = Integer.MAX_VALUE - 1;
        
        for(Link l : d.getIncoming())
        {
            if(l.label < min)
            {
                min = d.label;
                curr = l;
            }
        }
        
        output.setCost(min);
        
        while(curr != null)
        {
            output.add(0, curr);
            
            curr = curr.prev;
            
            
        }
        
        return output;
    }
    
    public void setVehicles(List<PersonalVehicle> vehicles)
    {
        Collections.sort(vehicles);
        
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
        if(!validate())
        {
            return;
        }
        
        
        
        
        
        resetSim();
        
        PrintStream sim_vat = null;
        
        if(recording)
        {
            File dir = new File("results/"+name+"/"+scenario);
            dir.mkdirs();
            File file = new File("results/"+name+"/"+scenario+"/sim.vat");

            sim_vat = new PrintStream(new FileOutputStream(file), true);
            
            fileout = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/cell_enter.txt")), true);
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
    
    public void initialize()
    {
        for(Node n : nodes)
        {
            n.initialize();
        }
        
        for(Link l : links)
        {
            l.initialize();
        }
        
        if(dlr && linktype == Link.CTM)
        {
            tieLinks();
        }
    }
    
    public void printLinkTdd() throws IOException
    {
        for(Link l : links)
        {
            l.postProcessFlowin();
        }
        

        PrintStream flowout = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/flowin.csv")), true);
        PrintStream ttout = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/linktt.csv")), true);
        
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
        if(linktype == Link.CTM)
        {
            for(Link l : links)
            {
                l.prepare();
            }
        }

        for(Link l : links)
        {
            l.step();
        }

        for(Node n : nodes)
        {
            exit_count += n.step();
        }

        if(linktype == Link.CTM)
        {
            for(Link l : links)
            {
                l.update();
            }
        }
    }
    
    public void printLinkTT(int start, int end) throws IOException
    {
        printLinkTT(start, end, new File("results/"+name+"/"+scenario+"/linktt.txt"));
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
    
    public void printLinkFlow(int start, int end) throws IOException
    {
        printLinkFlow(start, end, new File("results/"+name+"/"+scenario+"/linkq.txt"));
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
    
    /*
    public void printRecords(int max_time)
    {
        try
        {
            File dir = new File("results/"+scenario);
            dir.mkdirs();
            
            PrintStream fileout = new PrintStream(new FileOutputStream(new File("results/"+scenario+"/vehicle_path.txt")), true);
            PrintStream fileout2 = new PrintStream(new FileOutputStream(new File("results/"+scenario+"/vehicle_path_time.txt")), true);
            
            fileout.println("id\torigin\tdest\tsize\tfreeflowtt\tlength\tlinks");
            fileout2.println("id\ttype\torigin\tdest\tdep_time\texit_time\tpath\tefficiency\tdistance\tenergy\tMPG");
            
            Set<Integer> printed_paths = new HashSet<Integer>();
            
            for(Vehicle v : vehicles)
            {
                Path p = v.getPath();
                if(!printed_paths.add(p.getId()))
                {
                    fileout.println(p.getId()+"\t"+p.getOrigin()+"\t"+p.getDest()+"\t"+p.size()+"\t"+p.getFFTime()+"\t"+p.getLength()+"\t"+p);
                }
                
                fileout2.println(v.getId()+"\t"+v.getDriver()+"\t"+v.getOrigin()+"\t"+v.getDest()+"\t"+v.getDepTime()+"\t"+v.getExitTime()+"\t"+v.getPath().getId()+"\t"+
                        v.getEfficiency()+"\t"+v.getPath().getLength()+"\t"+v.getTotalEnergy()+"\t"+v.getMPG());
            }
            
            fileout.close();
            fileout2.close();
            
            printed_paths = null;
            
            fileout = new PrintStream(new FileOutputStream(new File("results/"+scenario+"/link_enter.txt")), true);
            fileout2 = new PrintStream(new FileOutputStream(new File("results/"+scenario+"/link_exit.txt")), true);
            
            fileout.print("id\ttype\tsource\tdest");
            fileout2.print("id\ttype\tsource\tdest");
            
            for(int i = 0; i <= Math.ceil(max_time / ast_duration) && i < duration / ast_duration; i++)
            {
                fileout.print("\t"+(i * ast_duration));
                fileout2.print("\t"+(i * ast_duration));
            }
            
            fileout.println();
            fileout2.println();
            
            for(Link l : links)
            {
                fileout.print(l.getId()+"\t"+l.getType()+"\t"+l.getSource()+"\t"+l.getDest());
                fileout2.print(l.getId()+"\t"+l.getType()+"\t"+l.getSource()+"\t"+l.getDest());
                
                for(int i = 0; i <= Math.ceil(max_time / ast_duration) && i < duration / ast_duration; i++)
                {
                    fileout.print("\t"+l.enter[i]);
                    fileout2.print("\t"+l.exit[i]);
                }
                
                fileout.println();
                fileout2.println();
            }
            
            fileout.close();
            fileout2.close();
            
            fileout = new PrintStream(new FileOutputStream(new File("results/"+scenario+"/link_tt.txt")), true);
            
            fileout.print("id\ttype\tsource\tdest");
            
            for(int i = 0; i <= Math.ceil(max_time / ast_duration) && i < duration / ast_duration; i++)
            {
                fileout.print("\t"+(i * ast_duration));
            }
            
            fileout.println();
            
            for(Link l : links)
            {
                fileout.print(l.getId()+"\t"+l.getType()+"\t"+l.getSource()+"\t"+l.getDest());
                
                for(int i = 0; i <= Math.ceil(max_time / ast_duration) && i < duration / ast_duration; i++)
                {
                    fileout.print("\t"+l.getAvgTT(i * ast_duration));
                }
                
                fileout.println();
            }
            
            fileout.close();
            
            fileout = new PrintStream(new FileOutputStream(new File("results/"+scenario+"/node_movement.txt")), true);
            
            fileout.print("id");
            
            for(int i = 0; i <= Math.ceil(max_time / ast_duration) && i < duration / ast_duration; i++)
            {
                fileout.print("\t"+(i * ast_duration));
            }
            
            fileout.println();
            
            for(Node n : nodes)
            {
                if(n.isZone())
                {
                    continue;
                }
                
                fileout.print(n.getId());
                
                for(int i = 0; i <= Math.ceil(max_time / ast_duration) && i < duration / ast_duration; i++)
                {
                    int count = 0;
                    
                    for(Link l : n.getOutgoing())
                    {
                        count += l.enter[i];
                    }
                    
                    fileout.print("\t"+count);
                }
                fileout.println();
            }
            
            fileout.close();
            
        }
        catch(IOException ex){}
    }
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
    
    public boolean validate()
    {
        for(Link l : links)
        {
            if(l.getType() != linktype && !l.isCentroidConnector())
            {
                return false;
            }
        }
        
        return true;
    }

    public Link getLink(int id)
    {
        for(Link l : links)
        {
            if(l.getId() == id)
            {
                return l;
            }
        }
        
        return null;
    }
    
}
