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
import avdta.network.node.obj.ObjFunction;
import avdta.network.node.policy.MCKSTBR;
import avdta.vehicle.StaticWallet;
import avdta.network.node.Turn;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.Wallet;
import avdta.network.node.Diverge;
import avdta.network.node.Intersection;
import avdta.network.node.Merge;
import avdta.network.node.IntersectionControl;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.network.node.StopSign;
import avdta.network.node.Phase;
import avdta.network.node.TrafficSignal;
import avdta.network.node.PriorityTBR;
import avdta.network.node.policy.SignalWeightedTBR;
import avdta.network.node.Signalized;
import avdta.network.AST;
import avdta.network.DemandProfile;
import avdta.network.link.BusLink;
import avdta.network.link.CACCLTMLink;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.SharedTransitCTMLink;
import avdta.network.link.TransitLane;
import avdta.network.node.obj.BackPressureObj;
import avdta.network.node.obj.P0Obj;
import avdta.project.DTAProject;
import avdta.network.node.PhasedTBR;
import avdta.network.node.policy.TransitFirst;
import avdta.project.Project;
import avdta.project.TransitProject;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.fuel.VehicleClass;
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
import java.util.TreeMap;
import java.util.TreeSet;

/** 
 * This class contains methods to read network data from the VISTA data format.
 * @author Michael
 */
public class ReadNetwork 
{
    public static final int LTM = 200;
    public static final int CTM = 100;
    public static final int DLR = 2;
    public static final int SHARED_TRANSIT = 3;
    public static final int TRANSIT_LANE = 4;
    public static final int CACC = 5;
    public static final int CENTROID = 1000;
    
    
    //public static final int CENTROID = 100;
    public static final int SIGNAL = 100;
    public static final int STOPSIGN = 200;
    public static final int RESERVATION = 300;
    
    // reservation policies
    public static final int FCFS = 1;
    public static final int FIFO = 4;
    public static final int AUCTION = 8;
    public static final int RANDOM = 9;
    
    public static final int IP = 50;
    public static final int MCKS = 30;
    public static final int VOT = 1;
    public static final int Q2 = 4;
    public static final int DE4 = 5;
    public static final int PRESSURE = 2;
    public static final int P0 = 3;
    public static final int TRANSIT_FIRST = 40;
    

 
    
    public static final int SIGNALIZED_RESERVATION = 20;
    public static final int PHASED = SIGNALIZED_RESERVATION+1;
    public static final int WEIGHTED = SIGNALIZED_RESERVATION+2;
    
    // vehicles
    public static final int HV = 10;
    public static final int AV = 20;
    
    
    public static final int ICV = 1;
    public static final int BEV = 2;
    
    public static final int DA_VEHICLE = 100;
    public static final int BUS = 500;        
    
    
    
    public Map<Integer, Node> nodesmap;
    public Map<Integer, Link> linksmap;
    public Map<Integer, Zone> zones;
    
    public List<Vehicle> vehicles;
    public List<BusLink> busLinks;
    
    
    private double mesodelta;
    
    public ReadNetwork()
    {
        zones = new HashMap<Integer, Zone>();
        
        nodesmap = new HashMap<Integer, Node>();
        linksmap = new HashMap<Integer, Link>();
        vehicles = new ArrayList<Vehicle>();
        
        busLinks = new ArrayList<BusLink>();
        
        mesodelta = 0.5;
    }
    
    public Simulator readNetwork(Project project) throws IOException
    {
        readOptions(project);
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        
        Simulator sim = new Simulator(project, nodes, links);
        
        sim.initialize();
        
        
        sim.setVehicles(vehicles);
        
        if(project instanceof TransitProject)
        {
            readTransit((TransitProject)project);
        }
        
        return sim;
    }
    
    public void createTransit(TransitProject project) throws IOException
    {
        int curr_id = -1;
        Map<Integer, Integer[]> busPeriod = new HashMap<Integer, Integer[]>();
        
        Scanner filein = new Scanner(project.getBusPeriodFile());
        filein.nextLine();
        
        while(filein.hasNextLine())
        {
            int id = filein.nextInt();
            int starttime = filein.nextInt();
            int endtime = filein.nextInt();
            filein.nextLine();
            
            busPeriod.put(id, new Integer[]{starttime, endtime});
        }     
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getBusFile()), true);
        fileout.println(getBusFileHeader());
        
        filein = new Scanner(project.getBusFrequencyFile());
        filein.nextLine();
        
        int type = BUS + AV + ICV;
        
        while(filein.hasNextLine())
        {
            int routeId = filein.nextInt();
            int periodId = filein.nextInt();
            int frequency = filein.nextInt();
            int offset = filein.nextInt();
            filein.nextLine();
            
            Integer[] period = busPeriod.get(periodId);
            
            if(period == null)
            {
                throw new RuntimeException("Period not found - "+periodId+" "+routeId);
            }
            
            int starttime = period[0];
            int endtime = period[1];
            
            for(int t = starttime + offset; t < endtime; t+=frequency)
            {
                fileout.println((--curr_id)+"\t"+type+"\t"+routeId+"\t"+t);
            }
        }
        filein.close();
        fileout.close();
        
        
    }
    
    public void readTransit(TransitProject project) throws IOException
    {
        Map<Integer, Map<Integer, Object[]>> temproutes = new HashMap<Integer, Map<Integer, Object[]>>();
        
        Map<Node, Map<Node, BusLink>> tempLinks = new HashMap<Node, Map<Node, BusLink>>();
        Map<Integer, ArrayList<BusLink>> routeStops = new HashMap<Integer, ArrayList<BusLink>>();
        Map<Integer, Path> routes = new HashMap<Integer, Path>();
        
        Scanner filein = new Scanner(project.getBusRouteLinkFile());
        
        filein.nextLine();
        
        
        while(filein.hasNextInt())
        {
            int routeid = filein.nextInt();
            int sequence = filein.nextInt();
            int linkid = filein.nextInt();
            boolean stop = filein.nextInt() == 1;
            int dwelltime = filein.nextInt();
            filein.nextLine();
            
            Link link = linksmap.get(linkid);
            
            if(!temproutes.containsKey(routeid))
            {
                temproutes.put(routeid, new TreeMap<Integer, Object[]>());
            }
            
            temproutes.get(routeid).put(sequence, new Object[]{linkid, stop, dwelltime});
        }
        
        filein.close();
        
        // construct busLinks
        for(int routeid : temproutes.keySet())
        {
            Map<Integer, Object[]> temp = temproutes.get(routeid);
            
            ArrayList<BusLink> stops = new ArrayList<BusLink>();
            Path route = new Path();
            routeStops.put(routeid, stops);
            routes.put(routeid, route);
            
            Node prev = null;
            
            for(int seq : temp.keySet())
            {
                Object[] data = temp.get(seq);
                Link link = linksmap.get((Integer)data[0]);
                boolean stop = (Boolean)data[1];
                int dwelltime = (Integer)data[2];
                
                if(route.size() > 0 && route.get(route.size()-1).getDest() != link.getSource())
                {
                    throw new RuntimeException("Route "+routeid+" is not connected around links "+route.get(route.size()-1).getId()+" and "+link.getId());
                }
                
                route.add(link);
                
                
                
                if(stop)
                {
                    if(prev != null)
                    {
                        Node source = prev;
                        Node dest = link.getDest();
                        
                        BusLink busLink;
                        
                        if(!tempLinks.containsKey(source))
                        {
                            tempLinks.put(source, new HashMap<Node, BusLink>());
                        }
                        if(!tempLinks.get(source).containsKey(dest))
                        {
                            tempLinks.get(source).put(dest, busLink = new BusLink(source, dest));
                            busLinks.add(busLink);
                        }
                        else
                        {
                            busLink = tempLinks.get(source).get(dest);
                        }
                        
                        stops.add(busLink);
                    }
                    
                    prev = link.getDest();
                }
            }
        }
        
        temproutes = null;
        tempLinks = null;
        
        // create buses, assign to routes
        
        filein = new Scanner(project.getBusFile());
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int routeid = filein.nextInt();
            int dtime = filein.nextInt();
            filein.nextLine();
            
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
            
            Bus bus = new Bus(id, routeid, dtime, routes.get(routeid), routeStops.get(routeid), vehClass, driver);
            
            vehicles.add(bus);
        }
        filein.close();
        
    }
    
    public void readIntersections(Project project) throws IOException
    {
        Scanner filein = new Scanner(project.getNodesFile());
        
        filein.nextLine();
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            double x = filein.nextDouble();
            double y = filein.nextDouble();
            double elevation = filein.nextDouble();
            filein.nextLine();
            
            if(type/100 == CENTROID/100)
            {
                continue;
            }
            
            Intersection node = (Intersection)nodesmap.get(id);
        
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
                BackPressureObj backpressureobj = new BackPressureObj();
                P0Obj p0obj = new P0Obj();
                
                switch(type/100)
                {
                    case SIGNAL/100:
                        node.setControl(new TrafficSignal());
                        break;
                    case STOPSIGN/100:
                        node.setControl(new StopSign());
                        break;
                    case RESERVATION/100:
                    {
                        switch(type % 100)
                        {
                            case FCFS: 
                                node.setControl(new PriorityTBR(node, IntersectionPolicy.FCFS));
                                break;
                            case AUCTION: 
                                node.setControl(new PriorityTBR(node, IntersectionPolicy.auction));
                                break;
                            case RANDOM: 
                                node.setControl(new PriorityTBR(node, IntersectionPolicy.random));
                                break;
                            case PRESSURE: 
                                node.setControl(new MCKSTBR(node, backpressureobj));
                                break;
                            case P0: 
                                node.setControl(new MCKSTBR(node, p0obj));
                                break;
                            case PHASED:
                                node.setControl(new PhasedTBR());
                                break;
                            case WEIGHTED:
                                node.setControl(new SignalWeightedTBR());
                                break;
                            case TRANSIT_FIRST + FCFS: 
                                node.setControl(new PriorityTBR(node, new TransitFirst(IntersectionPolicy.FCFS)));
                                break;
                            case TRANSIT_FIRST + AUCTION: 
                                node.setControl(new PriorityTBR(node, new TransitFirst(IntersectionPolicy.auction)));
                                break;
                            case TRANSIT_FIRST + RANDOM: 
                                node.setControl(new PriorityTBR(node, new TransitFirst(IntersectionPolicy.random)));
                                break;
                            case TRANSIT_FIRST + PRESSURE: 
                                node.setControl(new MCKSTBR(node, backpressureobj));
                                break;
                            case TRANSIT_FIRST + P0: 
                                node.setControl(new MCKSTBR(node, p0obj));
                                break;
                            default:
                                throw new RuntimeException("Reservation type not recognized: "+type);
                        }
                        break;
                    }
                    default:
                        throw new RuntimeException("Node type not recognized: "+type);
                }
            }
        
        
        }
    }
    
    public Set<Node> readNodes(Project project) throws IOException
    {
        Set<Node> nodes = new HashSet<Node>();
        
        Scanner filein = new Scanner(project.getNodesFile());
        
        filein.nextLine();
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            double x = filein.nextDouble();
            double y = filein.nextDouble();
            double elevation = filein.nextDouble();
            filein.nextLine();
            
            Node node;

            if(type / 100 == CENTROID/100)
            {
                node = new Zone(id, new Location(x, y));
            }
            else
            {
                node = new Intersection(id, new Location(x, y), null);
            }
            
            nodesmap.put(id, node);
            nodes.add(node);
        }
        filein.close();
        
        return nodes;
    }
    
    
    
    public Set<Link> readLinks(Project project) throws IOException
    {
        Set<Link> links = new HashSet<Link>();
        
        
        
        Scanner filein = new Scanner(project.getLinksFile());
        
        filein.nextLine();
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int source_id = filein.nextInt();
            int dest_id = filein.nextInt();
            double length = filein.nextDouble() / 5280;
            double ffspd = filein.nextDouble();
            double w = filein.nextDouble();
            double capacity = filein.nextDouble();
            int numLanes = filein.nextInt();
            double jamd = 5280.0/Vehicle.vehicle_length;
            
            
            filein.nextLine();
            
            Link link = null;
            
            
            switch(type/100)
            {   

                case CENTROID/100: 
                    link = new CentroidConnector(id, nodesmap.get(source_id), nodesmap.get(dest_id));
                    break;
                case LTM/100:
                    if(type % 10 == CACC)
                    {
                        if(CACCLTMLink.checkK2(capacity, ffspd, length))
                        {
                            link = new CACCLTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, w, jamd, length, numLanes);
                            
                            CACCLTMLink l = (CACCLTMLink)link;
                        }
                        else
                        {
                            link = new LTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, w, jamd, length, numLanes);
                        }
                        
                        
                        
                        
                    }
                    else
                    {
                        link = new LTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, w, jamd, length, numLanes);
                    }
                    break;
                case CTM/100:
                    if(type%10 == DLR)
                    {
                        Network.dlr = true;
                        link = new DLRCTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, w, jamd, length, numLanes);
                    }
                    else if(type % 10 == SHARED_TRANSIT && numLanes > 1)
                    {
                        TransitLane transitLane = new TransitLane(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, w, jamd, length);
                        links.add(transitLane);
                        link = new SharedTransitCTMLink(-id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, 
                                ffspd, ffspd*mesodelta, jamd, length, numLanes-1, transitLane);
                    }
                    else
                    {
                        link = new CTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, w, jamd, length, numLanes);
                    }
                    break;
                default:
                    throw new RuntimeException("Link type not recognized: "+type);
            }
            
            
            links.add(link);
            
            
            linksmap.put(id, link);
        }
        
        filein.close();
        
        
        filein = new Scanner(project.getLinkPointsFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            String points = filein.nextLine().trim();

            // create list of locations
            ArrayList<Location> list = new ArrayList<Location>();
            
            while(points.indexOf(')')>0)
            {
                String point = points.substring(points.indexOf('(')+1, points.indexOf(')'));
                points = points.substring(points.indexOf(')')+1);
                
                String x = point.substring(0, point.indexOf(','));
                String y = point.substring(point.indexOf(',')+1);
                
                Location loc = new Location(Double.parseDouble(x), Double.parseDouble(y));
                
                list.add(loc);
            }
            
            linksmap.get(id).setCoordinates(list);
        }
        filein.close();

        return links;
    }
    
    public void readPhases(Project project) throws IOException
    {
        Scanner filein;
        
        
        filein = new Scanner(project.getPhasesFile());
        
        filein.nextLine();

        while(filein.hasNext())
        {
            
            int nodeid = filein.nextInt();
            int type = filein.nextInt();
            int sequence = filein.nextInt();
            double timered = filein.nextDouble();
            double timeyellow = filein.nextDouble();
            double timegreen = filein.nextDouble();

            int num_moves = filein.nextInt();

            String temp = filein.nextLine();
            
            Node node = nodesmap.get(nodeid);
            
            
            if(!(node instanceof Intersection) || !(((Intersection)node).getControl() instanceof Signalized))
            {
                continue;
            }

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

            List<Turn> turns = new ArrayList<Turn>();

            for(int x = 0; x < num_moves; x++)
            {
                Link i = linksmap.get(Integer.parseInt(split_inc[x].trim()));
                Link j = linksmap.get(Integer.parseInt(split_out[x].trim()));

                if(i == null)
                {
                    throw new RuntimeException("Link "+i+" not found in phases for node "+nodeid);
                }
                else if(j == null)
                {
                    throw new RuntimeException("Link "+j+" not found in phases for node "+nodeid);
                }
                
                turns.add(new Turn(i, j));
                
                if(i instanceof SharedTransitCTMLink)
                {
                    if(j instanceof SharedTransitCTMLink)
                    {
                        turns.add(new Turn(((SharedTransitCTMLink)i).getTransitLane(),
                        ((SharedTransitCTMLink)j).getTransitLane()));
                    }
                    
                    turns.add(new Turn(((SharedTransitCTMLink)i).getTransitLane(), j));
                }
                else if(j instanceof SharedTransitCTMLink)
                {
                    turns.add(new Turn(i, ((SharedTransitCTMLink)j).getTransitLane()));
                }
                
                
            }

            Phase phase = new Phase(sequence, turns, timegreen, timeyellow, timered);

            if(turns.size() > 0)
            {   
                ((Signalized)((Intersection)node).getControl()).addPhase(phase);
            }
        }

        filein.close();
        
        filein = new Scanner(project.getSignalsFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int node = filein.nextInt();
            double offset = filein.nextDouble();
            filein.nextLine();
            
            Node n = nodesmap.get(node);
            
            if(n instanceof Intersection)
            {
                Intersection i = (Intersection)n;
                
                if(i.getControl() instanceof Signalized)
                {
                    ((Signalized)i.getControl()).setOffset(offset);
                }
            }
        }
        
        filein.close();
    }
    
    
    

    
    
    
    /*
    public void linkZones(Network sim)
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
            
            if(id >= 10000 && id < 20000)
            {
                zone.setLinkedZone(zones.get(id+10000));
                zones.get(id+10000).setLinkedZone(zone);
            }
        }

    }
    */
    
    public void readOptions(Project project)
    {
        try
        {
            Scanner filein = new Scanner(project.getOptionsFile());
            
            filein.nextLine();
            
            while(filein.hasNext())
            {
                String key = filein.next().toLowerCase();
                String val = filein.next().toLowerCase();
                
                project.setOption(key, val);
                
                if(key.equals("simulation-mesoscopic-delta"))
                {
                    double delta = Double.parseDouble(val);
                }
                else if(key.equals("simulation-duration"))
                {
                    Simulator.duration = Integer.parseInt(val);
                }
                else if(key.equals("ast-duration"))
                {
                    Simulator.ast_duration = Integer.parseInt(val);
                }
                else if(key.equals("simulation-mesoscopic-step"))
                {
                    Network.dt = Integer.parseInt(val);
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
                    Network.HVs_use_reservations = val.equalsIgnoreCase("true");
                }
                else if(key.equals("dynamic-lane-reversal") && val.equalsIgnoreCase("true"))
                {
                    Network.setDLR(true);
                }
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    
   
    

    
    
    public static Set<String> listProjects() throws IOException
    {
        return listProjects(null);
    }
    public static Set<String> listProjects(String typefilter) throws IOException
    {
        TreeSet<String> output = new TreeSet<String>();
        
        
        File dir = new File("projects/");
        
        for(File f : dir.listFiles())
        {
            if(f.isDirectory())
            {
                String name = f.getName();
                
                name = name.substring(name.lastIndexOf("/")+1);
                output.add(name);
                
                String path = f.getCanonicalPath();
                
                File properties = new File(path+"\\project.dat");
                
                if(properties.exists())
                {
                    Scanner filein = new Scanner(properties);
                    
                    
                    String type = null;
                    
                    filein.nextLine();
                    
                    while(filein.hasNext())
                    {
                        String key = filein.next().trim();
                        String val = filein.next().trim();
                        
                        
                        if(key.equals("type"))
                        {
                            type = val;
                            break;
                        }
                    }
                    filein.close();
                    
                    if(typefilter == null || type.equals(typefilter))
                    {
                        output.add(name+" ("+type+")");
                    }
                }    
            }
        }
        
        return output;
    }

    public static String getLinksFileHeader()
    {
        return "id\ttype\tsource\tdest\tlength (ft)\tffspd (mph)\tw (mph)\tcapacity\tnum_lanes";
    }
    
    public static String getNodesFileHeader()
    {
        return "id\ttype\tlongitude\tlatitude\televation";
    }
    
    public static String getLinkPointsFileHeader()
    {
        return "id\tcoordinates";
    }
    
    public static String getPhasesFileHeader()
    {
        return "node\ttype\tsequence\ttime_red\ttime_yellow\ttime_green\tnum_moves\tlink_from\tlink_to";
    }
    
    public static String getSignalsFileHeader()
    {
        return "node id\ttime_offset";
    }
    
    public static String getOptionsFileHeader()
    {
        return "name\tvalue";
    }
    
    public static String getBusFileHeader()
    {
        return "id\ttype\troute\tdtime";
    }
    
    public static String getBusFrequencyFileHeader()
    {
        return "route\tperiod\tfrequency\toffset";
    }
    
    public static String getBusRouteLinkFileHeader()
    {
        return "route\tsequence\tlink\tstop\tdwelltime";
    }
    
    public static String getBusPeriodFileHeader()
    {
        return "id\tstarttime\tendtime";
    }
}
