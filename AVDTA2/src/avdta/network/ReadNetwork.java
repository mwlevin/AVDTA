/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import avdta.network.link.CentroidConnector;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.link.CTMLink;
import avdta.network.node.Node;
import avdta.network.node.Location;
import avdta.network.node.Zone;
import avdta.network.node.ObjFunction;
import avdta.network.node.MCKSTBR;
import avdta.vehicle.StaticWallet;
import avdta.network.node.Turn;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.Wallet;
import avdta.network.node.Diverge;
import avdta.network.node.Intersection;
import avdta.network.node.Merge;
import avdta.network.node.IntersectionControl;
import avdta.network.node.IntersectionPolicy;
import avdta.network.node.StopSign;
import avdta.network.node.Phase;
import avdta.network.node.PhasedTBR;
import avdta.network.node.TrafficSignal;
import avdta.network.node.PriorityTBR;
import avdta.network.node.SignalWeightedTBR;
import avdta.network.node.Signalized;
import avdta.network.AST;
import avdta.network.DemandProfile;
import avdta.vehicle.DriverType;
import avdta.vehicle.VehicleClass;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/** 
 * This class contains methods to read network data from the VISTA data format.
 * @author Michael
 */
public class ReadNetwork 
{
    protected Map<Integer, Node> nodesmap;
    protected Map<Integer, Link> linksmap;
    protected Map<Integer, Zone> zones;
    
    
    private double mesodelta;
    
    public ReadNetwork()
    {
        zones = new HashMap<Integer, Zone>();
        
        nodesmap = new HashMap<Integer, Node>();
        linksmap = new HashMap<Integer, Link>();
        
        mesodelta = 0.5;
    }
    
    /**
     * Reads the nodes and links for the network from files "data/name/nodes.txt" and "data/name/links.txt", where name is the Simulator name. Note: this does not instantiate intersection controls or read demand
     * @param sim 
     * @param linktype type of link - CTM/LTM (see {@link Link})
     * @throws IOException 
     */
    public void readNetwork(Simulator sim, int linktype) throws IOException
    {
        String network = sim.getName();
        Simulator.setTimestep(linktype);
        
        readOptions(sim);
        readNodes(sim, new File("data/"+network+"/nodes.txt"));
        readLinks(sim, new File("data/"+network+"/linkdetails.txt"), linktype);
    }
    
    /**
     * Reads the nodes for the network from specified file. Note: this does not instantiate intersection controls 
     * @param sim
     * @param nodesfile
     * @throws IOException 
     */
    public void readNodes(Simulator sim, File nodesfile) throws IOException
    {
        List<Node> nodes = new ArrayList<Node>();
        
        Scanner filein = new Scanner(nodesfile);
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            double x = filein.nextDouble();
            double y = filein.nextDouble();
            
            Node node;
            
            if(type == 100)
            {

                if(nodesmap.containsKey(id))
                {
                    continue;
                }
                
                node = createZone(id, new Location(x, y));
            }
            else
            {
                node = new Intersection(id, new Location(x, y), null);
                
            }
            
            nodesmap.put(id, node);
            nodes.add(node);
        }
        sim.setNodes(nodes);
        
        filein.close();
    }
    
    /**
     * 
     * @param id
     * @param loc
     * @return a new Zone with the given id and Location
     */
    protected Zone createZone(int id, Location loc)
    {
        Zone node = new Zone(id, loc);
        
        return node;
    }
    
    /**
     * Reads links for a network from the specified file.
     * @param sim
     * @param linksfile
     * @param linktype type of link - CTM/LTM (see {@link Link})
     * @throws IOException 
     */
    public void readLinks(Simulator sim, File linksfile, int linktype) throws IOException
    {
        List<Link> links = new ArrayList<Link>();
        
        
        
        Scanner filein = new Scanner(linksfile);
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int source_id = filein.nextInt();
            int dest_id = filein.nextInt();
            
            if(!nodesmap.containsKey(source_id))
            {
                System.out.println(source_id);
            }

            
            double length = filein.nextDouble() / 5280;
            double ffspd = filein.nextDouble() * 60;
            double capacity = filein.nextDouble();
            int numLanes = filein.nextInt();
            double jamd = 5280.0/Vehicle.vehicle_length;
            
            Link link = null;
            
            if(type == 100)
            {
                link = new CentroidConnector(id, nodesmap.get(source_id), nodesmap.get(dest_id));
            }
            else
            {
                switch(linktype)
                {   
                    
                    case Link.LTM:
                         link = new LTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, ffspd*mesodelta, jamd, length, numLanes);
                         break;
                    case Link.CTM:
                         link = new CTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, ffspd*mesodelta, jamd, length, numLanes);
                         break;
                    default:
                        throw new RuntimeException("Link type not found");
                }
            }
            
            links.add(link);
            
            
            linksmap.put(id, link);
        }
        
        filein.close();

        sim.setLinks(links, linktype);
    }
    
    /**
     * Instantiates specified type of TBR for all nodes
     * @param sim
     * @param policy the policy controlling the node; should be an appropriate type for the reservation control
     * @param nodetype type of TBR node - CR, MCKS, STOP (see {@link Node})
     * @throws IOException 
     */
    public void readTBR(Simulator sim, Object policy, int nodetype) throws IOException
    {
        if(nodetype == Node.PHASED_TBR || nodetype == Node.WEIGHTED_TBR || nodetype == Node.MULTI || nodetype == Node.MULTI2 || nodetype == Node.MIX_SIGNAL_TBR)
        {
            readPhasedTBR(sim, policy, nodetype);
            return;
        }
        
        IntersectionControl control;

        
        for(Node n : sim.getNodes())
        {
            if(n instanceof Zone)
            {
                continue;
            }
            
            Intersection node = (Intersection)n;
            switch(nodetype)
            {
                case Node.CR: 
                    control = new PriorityTBR(node, (IntersectionPolicy)policy);
                    break;
                case Node.MCKS: 
                    control = new MCKSTBR(node, (ObjFunction)policy);
                    break;
                /*
                case Node.IP: 
                    control = new IPTBR(node, (ObjFunction)policy);
                    break;
                */
                case Node.STOP:
                    control = new StopSign(node);
                    break;
                default:
                    throw new RuntimeException("Invalid node type.");
            }

            node.setControl(control);
        }
        
        for(Node n : sim.getNodes())
        {
            n.initialize();
        }
        
    }  
    
    
    /**
     * Instantiates signal controls based on "data/name/phases.txt" file. Intersections without phases become merge/diverge if possible or stop signs if not.
     * @param sim
     * @throws IOException 
     */
    public void readSignals(Simulator sim) throws IOException
    {
        String network = sim.getName();
        readPhases(sim, new File("data/"+network+"/phases.txt"), null, Node.SIGNALS);
    }
    
    
    public void readPhasedTBR(Simulator sim, Object policy, int nodetype) throws IOException
    {
        String network = sim.getName();
        readPhases(sim, new File("data/"+network+"/phases.txt"), policy, nodetype);
    }
    
    public void readPhases(Simulator sim, Object policy, int nodetype) throws IOException
    {
        String network = sim.getName();
        readPhases(sim, new File("data/"+network+"/phases.txt"), policy, nodetype);
    }
    
    public void readPhases(Simulator sim, Object policy, int nodetype, File special) throws IOException
    {
        String network = sim.getName();
        readPhases(sim, new File("data/"+network+"/phases.txt"), policy, nodetype, special);
    }
    /**
     * Instantiates signal controls from specified file. Intersections without phases become merge/diverge if possible or stop signs if not.
     * @param sim
     * @param signalsFile
     * @throws IOException 
     */
    public void readPhases(Simulator sim, File signalsFile, Object policy, int nodetype) throws IOException
    {
        if(nodetype == Node.MULTI || nodetype == Node.MULTI2)
        {
            readPhases(sim, signalsFile, policy, nodetype, new File("data/"+sim.getName()+"/weighted_nodes.txt"));
        }
        else if(nodetype == Node.MIX_SIGNAL_TBR)
        {
            readPhases(sim, signalsFile, policy, nodetype, new File("data/"+sim.getName()+"/reservation_nodes.txt"));
        }
        else
        {
            readPhases(sim, signalsFile, policy, nodetype, null);
        }
    }
    public void readPhases(Simulator sim, File signalsFile, Object policy, int nodetype, File specialFile) throws IOException
    {
        Scanner filein;
        
        Set<Integer> special = new HashSet<Integer>();
        
        if(nodetype == Node.MULTI || nodetype == Node.MULTI2 || nodetype == Node.MIX_SIGNAL_TBR)
        {
            filein = new Scanner(specialFile);
            
            while(filein.hasNextInt())
            {
                special.add(filein.nextInt());
            }
            
            filein.close();
        }
        
        
        filein = new Scanner(signalsFile);

        while(filein.hasNext())
        {
            filein.next();
            int type = filein.nextInt();
            int nodeid = filein.nextInt();
            filein.next();
            int phaseid = filein.nextInt();
            double timered = filein.nextDouble();
            double timeyellow = filein.nextDouble();
            double timegreen = filein.nextDouble();

            int num_moves = filein.nextInt();

            String temp = filein.nextLine();

            String inc = temp.substring(temp.indexOf('{')+1, temp.indexOf('}'));
            temp = temp.substring(temp.indexOf('}')+1);
            String out = temp.substring(temp.indexOf('{')+1, temp.indexOf('}'));

            String[] split_inc = inc.split(",");
            String[] split_out = out.split(",");

            num_moves = split_inc.length;

            if(inc.length() == 0 || out.length() == 0)
            {
                num_moves = 0;
            }

            Turn[] turns = new Turn[num_moves];

            for(int x = 0; x < num_moves; x++)
            {
                Link i = linksmap.get(Integer.parseInt(split_inc[x].trim()));
                Link j = linksmap.get(Integer.parseInt(split_out[x].trim()));

                if(i == null || j == null)
                {
                    System.out.println(split_inc[x]+" "+split_out[x]+" "+i+" "+j);
                }
                turns[x] = new Turn(i, j);
            }

            //Phase phase = new Phase(turns, timegreen, timegreen + timeyellow + timered);
            double duration = timegreen + timeyellow + timered;
            Phase phase = new Phase(turns, timegreen, duration);

            if(turns.length > 0)
            {
                Intersection node = (Intersection)nodesmap.get(nodeid);
                if(node.getControl() == null)
                {
                    setControl(node, nodetype, policy, special);
                }
                ((Signalized)node.getControl()).addPhase(phase);
            }
        }

        filein.close();

        // look for signal nodes with no phases, replace them with merge, diverge, stop signs
        
        for(Node n : sim.getNodes())
        {
            if(n instanceof Zone)
            {
                continue;
            }
            
            Intersection node = (Intersection)n;
            
            if(/*((TrafficSignal)node.getControl()).getNumPhases() == 0*/node.getControl() == null)
            {
                if(node.getIncoming().size() == 1)
                {
                    node.setControl(new Diverge());
                }
                else if(node.getOutgoing().size() == 1)
                {
                    node.setControl(new Merge());
                }
                else
                {
                    setControl(node, nodetype, policy, special);
                }
            }
        } 
        
        for(Node n : sim.getNodes())
        {
            n.initialize();
        }
        
    }
    
    private void setControl(Intersection node, int nodetype, Object policy, Set<Integer> special)
    {
        int nodeid = node.getId();
        switch(nodetype)
        {
            case Node.SIGNALS:
                node.setControl(new TrafficSignal());
                break;
            case Node.PHASED_TBR:
                node.setControl(new PhasedTBR());
                break;
            case Node.WEIGHTED_TBR:
                node.setControl(new SignalWeightedTBR());
                break;
            case Node.MULTI:
                if(special.contains(nodeid))
                {
                    node.setControl(new SignalWeightedTBR());
                }
                else
                {
                    node.setControl(new PhasedTBR());
                }
                break;
            case Node.MULTI2:
                if(special.contains(nodeid))
                {
                    node.setControl(new PhasedTBR());
                }
                else
                {
                    node.setControl(new SignalWeightedTBR());
                }
                break;
            case Node.MIX_SIGNAL_TBR:
                if(special.contains(nodeid))
                {
                    if(policy instanceof IntersectionPolicy)
                    {
                        node.setControl(new PriorityTBR(node, (IntersectionPolicy)policy));
                    }
                    else if(policy instanceof ObjFunction)
                    {
                        node.setControl(new MCKSTBR(node, (ObjFunction)policy));
                    }
                    else
                    {
                        node.setControl(new PriorityTBR(node, IntersectionPolicy.FCFS));
                    }
                }
                else
                {
                    node.setControl(new TrafficSignal());
                }
                break;
        }
    }
    
    /**
     * Reads demand table from "data/name/demand.txt" file.
     * @param sim
     * @throws IOException 
     */
    public void readVehicles(Simulator sim) throws IOException
    {
        readVehicles(sim, new File("data/"+sim.getName()+"/demand.txt"));
    }
    
    /**
     * Reads demand table from specified file.
     * @param sim
     * @param demandfile
     * @throws IOException 
     */
    public void readVehicles(Simulator sim, File demandfile) throws IOException
    {
        List<PersonalVehicle> vehicles = new ArrayList<PersonalVehicle>();
        
        Scanner filein = new Scanner(demandfile);
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int origin_id = filein.nextInt();
            int dest_id = filein.nextInt();
            
            filein.next();
            int dtime = filein.nextInt();
            int type = filein.nextInt();
  
            if(type != Vehicle.BUS)
            {
                double vot = Simulator.dagum_rand();
            
                Wallet wallet = new StaticWallet(vot);
            
                Zone origin = (Zone)nodesmap.get(origin_id);
                Zone dest = (Zone)nodesmap.get(dest_id);
                
                origin.addProductions(1);
                dest.addAttractions(1);
                
                vehicles.add(new PersonalVehicle(id, origin, dest, dtime, vot, VehicleClass.getVehClass(type), DriverType.getDriver(type)));
            }
        }
        

        filein.close();

        sim.setVehicles(vehicles);
    }
    
    /**
     * Reads elevation data for nodes for energy consumption
     * @param sim
     * @param gradefile
     * @throws IOException 
     */
    
    public static void readGrade(Simulator sim) throws IOException
    {
        readGrade(sim, new File("data/"+sim.getName()+"/elevation.txt"));
    }
    public static void readGrade(Simulator sim, File gradefile) throws IOException
    {
        Scanner filein = new Scanner(gradefile);
        
        Map<Integer, Node> nodes_lookup = new HashMap<Integer, Node>();
        
        for(Node n : sim.getNodes())
        {
            nodes_lookup.put(n.getId(), n);
        }
        
        while(filein.hasNext())
        {
            nodes_lookup.get(filein.nextInt()).setElevation(filein.nextDouble());
        }
        
        filein.close();
        nodes_lookup = null;
        
        for(Link l : sim.getLinks())
        {
            l.setGrade();
        }
    }
    
    /**
     * When origins/destinations have separate zones, this attempts to link these separated zones. Origins are expected to have id of 100000+x, with corresponding destination id of 200000+x. Linked zones can be accessed from {@link Zone}.
     * @param sim 
     */
    public void linkZones(Simulator sim)
    {
        zones.clear();
        
        for(Node n : sim.getNodes())
        {
            if(n instanceof Zone)
            {
                Zone z = (Zone)n;
                
                zones.put(z.getId(), z);
                
            }
        }
        
        for(int id : zones.keySet())
        {
            Zone zone = zones.get(id);
            
            
            if(zone.getOutgoing().size() > 0)
            {
                Node i = zone.getOutgoing().iterator().next().getDest();

                
                
                for(Link l : i.getOutgoing())
                {
                    if(l.getDest() instanceof Zone)
                    {
                        zone.setLinkedZone((Zone)l.getDest());
                        ((Zone)l.getDest()).setLinkedZone(zone);
                        
                        
                        break;
                    }
                }
            }
            /*
            if(id >= 10000 && id < 20000)
            {
                zone.setLinkedZone(zones.get(id+10000));
                zones.get(id+10000).setLinkedZone(zone);
            }*/
        }

    }
    
    
    public void readOptions(Simulator sim)
    {
        try
        {
            Scanner filein = new Scanner(new File("data/"+sim.getName()+"/options.txt"));
            
            while(filein.hasNext())
            {
                String key = filein.next();
                String val = filein.next();
                
                if(key.equals("simulation-mesoscopic-delta"))
                {
                    double delta = Double.parseDouble(val);
                    
                    /*
                    for(Link l : sim.getLinks())
                    {
                        if(l instanceof CTMLink)
                        {
                            ((CTMLink)l).setMesoDelta(delta);
                        }
                    }
                    */ 
                }
                else if(key.equals("simulation-duration"))
                {
                    Simulator.duration = Integer.parseInt(val);
                }
                else if(key.equals("simulation-mesoscopic-step"))
                {
                    Simulator.dt = Integer.parseInt(val);
                }
                else if(key.equals("hv-reaction-time"))
                {
                    DriverType.HV.setReactionTime(Double.parseDouble(val));
                }
                else if(key.equals("av-reaction-time"))
                {
                    DriverType.AV.setReactionTime(Double.parseDouble(val));
                }
                else if(key.equals("hvs-use-reservations"))
                {
                    Simulator.HVs_use_reservations = Integer.parseInt(val) > 0;
                }
                else if(key.equals("dynamic-lane-reversal") && Integer.parseInt(val) == 1)
                {
                    Simulator.setDLR(true);
                }
            }
        }
        catch(IOException ex)
        {
            
        }
    }
    
    public static void createNetwork(String name) throws IOException
    {
        File file = new File("data/"+name);
        file.mkdirs();
        
        PrintStream fileout = new PrintStream(new File("data/"+name+"/nodes.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/linkdetails.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/demand.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/phases.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/static_od.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/dynamic_od.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/bus_frequency.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/bus_period.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/bus_route_link.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/demand_profile.txt"));
        fileout.close();
        
        fileout = new PrintStream(new File("data/"+name+"/elevation.txt"));
        fileout.close();
        
        fillOptions(name);
    }
    
    public static void fillOptions(String name) throws IOException
    {
        PrintStream fileout = new PrintStream(new File("data/"+name+"/options.txt"));
        fileout.println("simulation-mesoscopic-delta\t0.5");
        fileout.println("simulation-duration\t36000");
        fileout.println("hv-reaction-time\t1");
        fileout.println("av-reaction-time\t1");
        fileout.println("hvs-use-reservations\t0");
        fileout.println("dynamic-lane-reversal\t0");
        fileout.close();
    }
    
    public void createDynamicOD(String network) throws IOException
    {
        createDynamicOD(new File("data/"+network+"/static_od.txt"), new File("data/"+network+"/demand_profile.txt"), new File("data/"+network+"/dynamic_od.txt"));
    }
    
    public void createDynamicOD(File static_od, File demand_profile, File dynamic_od) throws IOException
    {
        DemandProfile profile = readDemandProfile(demand_profile);
        
        Scanner filein = new Scanner(static_od);
        
        if(!filein.hasNextInt())
        {
            filein.close();
            
            return;
        }
        
        PrintStream fileout = new PrintStream(new FileOutputStream(dynamic_od), true);
        
        int new_id = 1;
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            double demand = filein.nextDouble();
            
            for(int t : profile.keySet())
            {
                AST ast = profile.get(t);
                
                fileout.println((new_id++)+"\t"+type+"\t"+origin+"\t"+dest+"\t"+demand * ast.getWeight()+"\t"+ast.getId());
            }
        }
        
        filein.close();
        fileout.close();
    }
    
    public int prepareDemand(String network, double prop, double avprop) throws IOException
    {
        
        return prepareDemand(prop, avprop, new File("data/"+network+"/dynamic_od.txt"), new File("data/"+network+"/demand_profile.txt"), new File("data/"+network+"/demand.txt"));
    }
    
    public int prepareDemand(double prop, double avprop, File dynamic_od_file, File demand_profile_file, File demand_file) throws IOException
    {
        prop = prop/100;
        avprop = avprop/100;
        
        DemandProfile profile = readDemandProfile(demand_profile_file);
        Scanner filein = new Scanner(dynamic_od_file);
        PrintStream fileout = new PrintStream(new FileOutputStream(demand_file), true);
        
        int total = 0;

        int new_id = 1;
        while(filein.hasNextInt())
        {
            filein.nextInt();
            int type = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            double demand = filein.nextDouble() * prop;

            int t = filein.nextInt();
            AST ast = profile.get(t);
            
            double av_demand = demand * avprop;
            double hv_demand = demand * (1 - avprop);
            
            
            int num_vehicles = (int)Math.floor(av_demand);
            double rem = av_demand - Math.floor(av_demand);
            
            
            if(Math.random() < rem)
            {
                num_vehicles ++;
            }
            
            int dtime_interval = ast.getDuration() / (num_vehicles+1);
            
            
            for(int i = 0; i < num_vehicles; i++)
            {
                int dtime = ast.getStart() + (i+1) * dtime_interval;
                
                fileout.println((new_id++)+"\t"+origin+"\t"+dest+"\t"+ast.getId()+"\t"+dtime+"\t10");
            }
            
            total += num_vehicles;
            

            
            
            num_vehicles = (int)Math.floor(hv_demand);
            rem = hv_demand - Math.floor(hv_demand);
            if(Math.random() < rem)
            {
                num_vehicles ++;
            }
            
            dtime_interval = ast.getDuration() / (num_vehicles+1);
            
            new_id = 1;
            for(int i = 0; i < num_vehicles; i++)
            {
                int dtime = ast.getStart() + (i+1) * dtime_interval;
                
                fileout.println((new_id++)+"\t"+origin+"\t"+dest+"\t"+ast.getId()+"\t"+dtime+"\t1");
            }
            
            total += num_vehicles;
            
            
        }
        
        filein.close();
        fileout.close();
        

        return total;
        
    }
    
    public DemandProfile readDemandProfile(String network) throws IOException
    {
        return readDemandProfile(new File("data/"+network+"/demand_profile.txt"));
    }
    
    public DemandProfile readDemandProfile(File file) throws IOException
    {
        DemandProfile output = new DemandProfile();
        
        Scanner filein = new Scanner(file);
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            double weight = filein.nextDouble();
            int start = filein.nextInt();
            int duration = filein.nextInt();
            
            output.put(id, new AST(id, start, duration, weight));
        }
        
        filein.close();

        output.normalizeWeights();
        
        return output;
    }
    
    public static Set<String> listNetworks()
    {
        TreeSet<String> output = new TreeSet<String>();
        
        
        File dir = new File("data/");
        
        for(File f : dir.listFiles())
        {
            if(f.isDirectory())
            {
                String name = f.getName();
                
                name = name.substring(name.lastIndexOf("/")+1);
                output.add(name);
                
                
            }
        }
        
        return output;
    }

}
