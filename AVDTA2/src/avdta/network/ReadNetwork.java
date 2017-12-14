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
import avdta.vehicle.wallet.StaticWallet;
import avdta.network.node.Turn;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.wallet.Wallet;
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
import avdta.demand.AST;
import avdta.demand.DemandProfile;
import avdta.network.link.transit.BusLink;
import avdta.network.link.CACCLTMLink;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.SharedTransitCTMLink;
import avdta.network.link.AbstractSplitLink;
import avdta.network.link.LinkRecord;
import avdta.network.link.SplitCTMLink;
import avdta.network.link.TransitLane;
import avdta.network.node.Connector;
import avdta.network.node.Highway;
import avdta.network.node.obj.BackPressureObj;
import avdta.network.node.obj.P0Obj;
import avdta.project.DTAProject;
import avdta.network.node.NodeRecord;
import avdta.network.node.PhaseRecord;
import avdta.network.node.PhasedTBR;
import avdta.network.node.SignalRecord;
import avdta.network.node.TurnRecord;
import avdta.network.node.obj.MaxPressureObj;
import avdta.network.node.policy.TransitFirst;
import avdta.network.type.ExtendedType;
import avdta.network.type.Type;
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
 * This class contains methods to read network and transit data for AVDTA.
 * To use this class, see {@link ReadNetwork#readNetwork(avdta.project.Project)}.
 * @author Michael
 */
public class ReadNetwork 
{
    public static final Type LTM = new Type(200, "LTM");
    public static final Type CTM = new Type(100, "CTM");
    public static final ExtendedType DLR = new ExtendedType(2, "DLR", CTM);
    public static final ExtendedType SHARED_TRANSIT = new ExtendedType(3, "Shared-transit", CTM)
    {
        public boolean isValid(LinkRecord link)
        {
            return link.getNumLanes() > 1;
        }
    };
    public static final ExtendedType SPLIT_TRANSIT = new ExtendedType(4, "Split-transit", CTM)
    {
        public boolean isValid(LinkRecord link)
        {
            return link.getNumLanes() > 1;
        }
    };
    
    public static final ExtendedType CACC = new ExtendedType(5, "CACC", LTM);
    
    public static final ExtendedType TRANSIT_LANE = new ExtendedType(10, "Transit lane", CTM);
    public static final Type CENTROID = new Type(1000, "Centroid");
    
    // arrays of all types for searching purposes
    public static final Type[] LINK_TYPES = new Type[]{LTM, CTM, CENTROID};
    public static final ExtendedType[] LINK_EXT_TYPES = new ExtendedType[]{DLR, SHARED_TRANSIT, SPLIT_TRANSIT, CACC, TRANSIT_LANE};
    
    // these are the options that will show up in the GUI.
    public static final Type[] LINK_OPTIONS = new Type[]{LTM, CTM}; 
    public static final ExtendedType[] LINK_EXT_OPTIONS = new ExtendedType[]{DLR, SHARED_TRANSIT, SPLIT_TRANSIT, CACC};
    
    //public static final int CENTROID = 100;
    public static final Type SIGNAL = new Type(100, "Signal");
    public static final Type DIVERGE = new Type(500, "Diverge");
    public static final Type MERGE = new Type(600, "Merge");
    public static final Type CONNECTOR = new Type(700, "Connector");
    public static final Type STOPSIGN = new Type(800, "Stop sign");
    
    
    public static final Type RESERVATION = new Type(300, "AIM");
    public static final Type HIGHWAY = new Type(400, "Highway");
    
    // reservation policies
    public static final ExtendedType FCFS = new ExtendedType(1, "FCFS", RESERVATION);
    public static final ExtendedType EMERGENCY_FIRST = new ExtendedType(61, "Emergency-first", RESERVATION);
    public static final ExtendedType EMERGENCY_FIRST_LIMITED = new ExtendedType(62, "Emergency-first ltd", RESERVATION);
    public static final ExtendedType FIFO = new ExtendedType(4, "FIFO", RESERVATION);
    public static final ExtendedType AUCTION = new ExtendedType(8, "Auction", RESERVATION);
    public static final ExtendedType RANDOM = new ExtendedType(9, "Random", RESERVATION);
    
    public static final ExtendedType IP = new ExtendedType(50, "IP", RESERVATION);
    public static final ExtendedType MCKS = new ExtendedType(30, "MCKS", RESERVATION);
    public static final ExtendedType VOT = new ExtendedType(1, "VOT", MCKS);
    public static final ExtendedType Q2 = new ExtendedType(4, "Q2", MCKS);
    public static final ExtendedType DE4 = new ExtendedType(5, "DE4", MCKS);
    public static final ExtendedType PRESSURE = new ExtendedType(2, "Backpressure", MCKS);
    public static final ExtendedType P0 = new ExtendedType(3, "P0", MCKS);
    public static final ExtendedType MAX_PRESSURE = new ExtendedType(6, "Max-pressure", MCKS);
    public static final ExtendedType TRANSIT_FIRST = new ExtendedType(41, "Transit-first", RESERVATION);
    
    // arrays of all types for searching purposes
    public static final Type[] NODE_TYPES = new Type[]{SIGNAL, RESERVATION, HIGHWAY, DIVERGE, MERGE, CONNECTOR};
    public static final Type[] NODE_EXT_TYPES = new Type[]{FCFS, EMERGENCY_FIRST,
        EMERGENCY_FIRST_LIMITED, FIFO, AUCTION, RANDOM, VOT, Q2, DE4, PRESSURE, P0, MAX_PRESSURE, TRANSIT_FIRST};
    
    // these are the options that will show up in the GUI.
    public static final Type[] NODE_OPTIONS = new Type[]{SIGNAL, RESERVATION, STOPSIGN}; 
    public static final Type[] NODE_EXT_OPTIONS = new Type[]{FCFS, EMERGENCY_FIRST,
        FIFO, AUCTION, MAX_PRESSURE, TRANSIT_FIRST};

 

    public static final ExtendedType PHASED = new ExtendedType(21, "Phased", RESERVATION);
    public static final ExtendedType WEIGHTED = new ExtendedType(22, "Weighted", RESERVATION);
    
    // travelers
    public static final int HV = 10;
    public static final int AV = 20;
    public static final int CV = 30;
    
    
    
    public static final int ICV = 1;
    public static final int BEV = 2;
    public static final int HEV = 3;
    
    public static final int DA_VEHICLE = 100;
    public static final int BUS = 500;  
    public static final int EMERGENCY_VEHICLE = 600;
    

    
    // used for sanity check
    public static final int MALFORMED_DATA = 1;
    public static final int BAD_FILE = 1;
    public static final int ODD_DATA = 3;
    public static final int DATA_MISMATCH = 2;
    public static final int NORMAL = 0;
    
    
    public Map<Integer, Node> nodesmap;
    public Map<Integer, Link> linksmap;
    public Map<Integer, Zone> zones;
    
    public List<Vehicle> vehicles;
    public List<BusLink> busLinks;
    
    
    private double mesodelta;
    
    /**
     * Constructs an empty {@link ReadNetwork} object.
     * This initializes the nodes and links maps.
     */
    public ReadNetwork()
    {
        zones = new HashMap<Integer, Zone>();
        
        nodesmap = new HashMap<Integer, Node>();
        linksmap = new HashMap<Integer, Link>();
        vehicles = new ArrayList<Vehicle>();
        
        busLinks = new ArrayList<BusLink>();
        
        mesodelta = 0.5;
    }
    
    public void setNodesMap(Map<Integer, Node> map)
    {
        nodesmap = map;
    }
    
    /**
     * Creates a {@link Simulator} corresponding to the data contained with the specified project.
     * If the project is a {@link TransitProject}, transit vehicles will be added.
     * @param project the project
     * @return the {@link Simulator} for the project
     * @throws IOException if a file cannot be accessed
     */
    public Simulator readNetwork(Project project) throws IOException
    {
        readOptions(project);
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        
        Simulator sim = new Simulator(project, nodes, links);
        

        sim.setVehicles(vehicles);
        
        sim.initialize();
        

        
        if(project instanceof TransitProject)
        {
            readTransit((TransitProject)project);
        }
        
        return sim;
    }
    
    /**
     * Creates transit vehicles and writes them to the bus file.
     * The number of vehicles created depends on the routes in the bus_period and bus_frequency files.
     * This overwrites the bus file.
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
    public void createTransit(TransitProject project) throws IOException
    {
        int curr_id = -1;
        Map<Integer, Integer[]> busPeriod = new HashMap<Integer, Integer[]>();
        
        Scanner filein = new Scanner(project.getBusPeriodFile());
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int starttime = filein.nextInt();
            int endtime = filein.nextInt();
            
            if(filein.hasNextLine())
            {
                filein.nextLine();
            }
            
            busPeriod.put(id, new Integer[]{starttime, endtime});
        }     
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getBusFile()), true);
        fileout.println(getBusFileHeader());
        
        filein = new Scanner(project.getBusFrequencyFile());
        filein.nextLine();
        
        int type = BUS + AV + ICV;
        
        while(filein.hasNextInt())
        {
            int routeId = filein.nextInt();
            int periodId = filein.nextInt();
            int frequency = filein.nextInt();
            int offset = filein.nextInt();
            
            if(filein.hasNextLine())
            {
                filein.nextLine();
            }
            
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
    
    /**
     * Reads created transit vehicles from the bus file and assigns them to the appropriate paths.
     * This method also initializes {@link BusLink}s for mode choice models using transit.
     * If there are no transit vehicles, see {@link ReadNetwork#createTransit(avdta.project.TransitProject)}.
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
    public void readTransit(TransitProject project) throws IOException
    {
        Map<Integer, Map<Integer, Object[]>> temproutes = new HashMap<Integer, Map<Integer, Object[]>>();
        
        Map<Node, Map<Node, BusLink>> tempLinks = new HashMap<Node, Map<Node, BusLink>>();
        Map<Integer, ArrayList<BusLink>> routeStops = new HashMap<Integer, ArrayList<BusLink>>();
        Map<Integer, Path> routes = new HashMap<Integer, Path>();
        
        if(linksmap.size() == 0)
        {
            linksmap = project.getSimulator().createLinkIdsMap();
        }
        
        Scanner filein = new Scanner(project.getBusRouteLinkFile());
        
        filein.nextLine();
        
        
        while(filein.hasNextInt())
        {
            int routeid = filein.nextInt();
            int sequence = filein.nextInt();
            int linkid = filein.nextInt();
            boolean stop = filein.nextInt() == 1;
            int dwelltime = filein.nextInt();
            
            if(filein.hasNextLine())
            {
                filein.nextLine();
            }
            
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
                
                if(link == null)
                {
                     System.out.println(data[0]+" "+linksmap.size());
                }
                
                boolean stop = (Boolean)data[1];
                int dwelltime = (Integer)data[2];
                
                if(route.size() > 0 && route.get(route.size()-1).getDest() != link.getSource())
                {
                    throw new RuntimeException("Route "+routeid+" is not connected around links "+route.get(route.size()-1).getId()+" and "+link.getId());
                }
                

                if(link.hasTransitLane())
                {
                    AbstractSplitLink mainLink = (AbstractSplitLink)link;
                    TransitLane transitLane = mainLink.getTransitLane();
                    route.add(transitLane);
                }
                else
                {
                    route.add(link);
                }
                
                
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
        
        int count = 0;
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int routeid = filein.nextInt();
            int dtime = filein.nextInt();

            count++;
            if(filein.hasNextLine())
            {
                filein.nextLine();
            }
            
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
    
    /**
     * Reads intersection controls from the nodes file.
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
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
            
            if(filein.hasNextLine())
            {
                filein.nextLine();
            }
            
            if(type/100 == CENTROID.getCode()/100)
            {
                continue;
            }
            
            Intersection node = (Intersection)nodesmap.get(id);
        
            /*
            if(node.getIncoming().size() == 1 && type != HIGHWAY)
            {
                node.setControl(new Diverge());
            }
            else if(node.getOutgoing().size() == 1 && type != HIGHWAY)
            {
                node.setControl(new Merge());
            }
            else
            {
            */
            BackPressureObj backpressureobj = new BackPressureObj();
            P0Obj p0obj = new P0Obj();
            MaxPressureObj maxpressureobj = new MaxPressureObj();

            if(type/100 == SIGNAL.getCode()/100)
            {
                node.setControl(new TrafficSignal());
            }
            else if(type == STOPSIGN.getCode())
            {
                node.setControl(new StopSign());
            }
            else if(type == DIVERGE.getCode())
            {
                node.setControl(new PriorityTBR(node, IntersectionPolicy.FCFS));
                //node.setControl(new Diverge());
            }
            else if(type == MERGE.getCode())
            {
                node.setControl(new PriorityTBR(node, IntersectionPolicy.FCFS));
                //node.setControl(new Merge());
            }
            else if(type == CONNECTOR.getCode())
            {
                node.setControl(new PriorityTBR(node, IntersectionPolicy.FCFS));
                //node.setControl(new Connector());
            }
            else if(type/100 == HIGHWAY.getCode()/100)
            {
                node.setControl(new Highway());
            }
            else if(type/100 == RESERVATION.getCode()/100)
            {
                if(type%100 == FCFS.getCode()%100)
                {
                    node.setControl(new PriorityTBR(node, IntersectionPolicy.FCFS));
                }
                else if(type%100 == AUCTION.getCode()%100)
                {
                    node.setControl(new PriorityTBR(node, IntersectionPolicy.auction));
                }
                else if(type%100 == RANDOM.getCode()%100)
                {
                    node.setControl(new PriorityTBR(node, IntersectionPolicy.random));
                }
                else if(type%100 == EMERGENCY_FIRST.getCode()%100)
                {
                    node.setControl(new PriorityTBR(node, IntersectionPolicy.emergency));
                }
                else if(type%100 == EMERGENCY_FIRST_LIMITED.getCode()%100)
                {
                    node.setControl(new PriorityTBR(node, IntersectionPolicy.emergency_limited));
                }
                else if(type%100 == PRESSURE.getCode()%100)
                {
                    node.setControl(new MCKSTBR(node, backpressureobj));
                }
                else if(type%100 == MAX_PRESSURE.getCode()%100)
                {
                    node.setControl(new MCKSTBR(node, maxpressureobj));
                }
                else if(type%100 == P0.getCode()%100)
                {
                    node.setControl(new MCKSTBR(node, p0obj));
                }
                else if(type%100 == PHASED.getCode()%100)
                {
                    node.setControl(new PhasedTBR());
                }
                else if(type%100 == WEIGHTED.getCode()%100)
                {
                    node.setControl(new SignalWeightedTBR());
                }
                else if(type%100 == TRANSIT_FIRST.getCode()%100)
                {
                    node.setControl(new PriorityTBR(node, new TransitFirst(IntersectionPolicy.FCFS)));
                }
                else
                {
                    throw new RuntimeException("Node type not recognized: "+type);
                }
            }
            else
            {
                throw new RuntimeException("Node type not recognized: "+type);
            }

        }
    }
    
    /**
     * Reads in all nodes from the nodes file.
     * @param project the project
     * @return a set of all {@link Node}s read
     * @throws IOException if a file cannot be accessed
     */
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
            
            if(filein.hasNextLine())
            {
                filein.nextLine();
            }
            
            Node node;

            if(type / 100 == CENTROID.getCode()/100)
            {
                Zone[] zones = createZones(id, new Location(x, y));
                
                for(Zone z : zones)
                {
                    nodesmap.put(z.getId(), z);
                    nodes.add(z);

                }
            }
            else
            {
                node = new Intersection(id, new Location(x, y), null);
                nodesmap.put(id, node);
                nodes.add(node);

            }
            
            
        }
        filein.close();

        return nodes;
    }
    
    /**
     * Creates zones for the specified id and location.
     * The destination zone uses -id.
     * @param id the id of the zone
     * @param loc the location
     * @return returns an array containing the created zones in the order (origin, dest).
     */
    public Zone[] createZones(int id, Location loc)
    {
        Zone origin = new Zone(id, loc);
        Zone dest = new Zone(-id, loc);
        
        origin.setLinkedZone(dest);
        dest.setLinkedZone(origin);
        
        return new Zone[]{origin, dest};
    }
    
    /**
     * Reads in all links from the links file.
     * For links with a {@link TransitLane}, two separate links will be created (the {@link TransitLane} and the main link).
     * {@link TransitLane}s do not appear separately in the links file.
     * @param project the project
     * @return a set of all {@link Link}s
     * @throws IOException if a file cannot be accessed
     */
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
            
            
            if(filein.hasNextLine())
            {
                filein.nextLine();
            }
            
            Link link = null;
            
            Node source = nodesmap.get(source_id);
            Node dest = nodesmap.get(dest_id);
            
            if(source == null)
            {
                throw new RuntimeException("Cannot find node "+source_id);
            }
            
            if(dest == null)
            {
                throw new RuntimeException("Cannot find node "+dest_id);
            }
            
            // positive dest ids will return the origin zone, swap to get the destination zone
            if(dest_id > 0 && dest instanceof Zone)
            {
                dest = ((Zone)dest).getLinkedZone();
            }
            
            
            if(type/100 == CENTROID.getCode()/100)
            {
                link = new CentroidConnector(id, source, dest);
            }
            else if(type/100 == LTM.getCode()/100)
            {
                if(type == CACC.getCode())
                {
                    if(CACCLTMLink.checkK2(capacity, ffspd, length))
                    {
                        //link = new LTMLink(id, source, dest, capacity, ffspd, w, jamd, length, numLanes);
                        link = new CACCLTMLink(id, source, dest, capacity, ffspd, w, jamd, length, numLanes);

                    }
                    else
                    {
                        link = new LTMLink(id, source, dest, capacity, ffspd, w, jamd, length, numLanes);
                    }
                }
                else
                {
                    link = new LTMLink(id, source, dest, capacity, ffspd, w, jamd, length, numLanes);
                }
            }
            else if(type/100 == CTM.getCode()/100)
            {
                if(type%10 == DLR.getCode()%10)
                {
                    //Network.dlr = true;
                    link = new DLRCTMLink(id, source, dest, capacity, ffspd, w, jamd, length, numLanes);
                }
                else if(type % 10 == SHARED_TRANSIT.getCode()%10 && numLanes > 1)
                {
                    TransitLane transitLane = new TransitLane(-id, source, dest, capacity, ffspd, w, jamd, length);
                    links.add(transitLane);
                    link = new SharedTransitCTMLink(id, source, dest, capacity, 
                            ffspd, ffspd*mesodelta, jamd, length, numLanes-1, transitLane);
                }
                else if(type % 10 == SPLIT_TRANSIT.getCode()%10 && numLanes > 1)
                {
                    TransitLane transitLane = new TransitLane(-id, source, dest, capacity, ffspd, w, jamd, length);
                    links.add(transitLane);
                    link = new SplitCTMLink(id, source, dest, capacity, 
                            ffspd, ffspd*mesodelta, jamd, length, numLanes-1, transitLane);
                }
                else
                {
                    link = new CTMLink(id, source, dest, capacity, ffspd, w, jamd, length, numLanes);
                }
            }
            else
            {
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
            
            if(linksmap.containsKey(id))
            {
                linksmap.get(id).setCoordinates(list);
            }
        }
        filein.close();

        return links;
    }
    
    /**
     * Reads signal phases and signal cycle offsets from the phases and signals files.
     * Signal data will be ignored if the node it is assigned to is does not have a {@link Signalized}.
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
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

                /*
                if(i == null)
                {
                    throw new RuntimeException("Link "+i+" not found in phases for node "+nodeid);
                }
                else if(j == null)
                {
                    throw new RuntimeException("Link "+j+" not found in phases for node "+nodeid);
                }
                */
                if(i != null && j != null)
                {
                    turns.add(new Turn(i, j));
                
                    if(i instanceof AbstractSplitLink)
                    {
                        if(j instanceof AbstractSplitLink)
                        {
                            turns.add(new Turn(((AbstractSplitLink)i).getTransitLane(),
                            ((AbstractSplitLink)j).getTransitLane()));
                        }

                        turns.add(new Turn(((AbstractSplitLink)i).getTransitLane(), j));
                    }
                    else if(j instanceof AbstractSplitLink)
                    {
                        turns.add(new Turn(i, ((AbstractSplitLink)j).getTransitLane()));
                    }
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
            
            if(filein.hasNextLine())
            {
                filein.nextLine();
            }
            
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
    
    
    

    
    
    
    
    
    /**
     * Reads the project options from the options file.
     * @param project the project
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
                    DriverType.BUS_HV.setReactionTime(Double.parseDouble(val));
                }
                else if(key.equals("av-reaction-time"))
                {
                    DriverType.AV.setReactionTime(Double.parseDouble(val));
                    DriverType.BUS_AV.setReactionTime(Double.parseDouble(val));
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
            filein.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    
   
    

    
    /**
     * Creates a list of all projects in the default project directory.
     * Calls {@link ReadNetwork#listProjects(java.lang.String)} with null filter
     * @return  a list of all projects
     * @throws IOException if a file cannot be accessed
     */
    public static Set<String> listProjects() throws IOException
    {
        return listProjects(null);
    }
    
    /**
     * Creates a list of all projects in the default project directory of the specified type.
     * @return  a list of all projects
     * @param typefilter the project type
     * @throws IOException if a file cannot be accessed
     */
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

    /**
     * This returns the header for the links file.
     * @return the header for the links file
     */
    public static String getLinksFileHeader()
    {
        return "id\ttype\tsource\tdest\tlength (ft)\tffspd (mph)\tw (mph)\tcapacity\tnum_lanes";
    }
    
    /**
     * This returns the header for the nodes file.
     * @return the header for the nodes file
     */
    public static String getNodesFileHeader()
    {
        return "id\ttype\tlongitude\tlatitude\televation";
    }
    
    /**
     * This returns the header for the link_points file.
     * @return the header for the link_points file
     */
    public static String getLinkPointsFileHeader()
    {
        return "id\tcoordinates";
    }
    
    /**
     * This returns the header for the phases file.
     * @return the header for the phases file
     */
    public static String getPhasesFileHeader()
    {
        return "node\ttype\tsequence\ttime_red\ttime_yellow\ttime_green\tnum_moves\tlink_from\tlink_to";
    }
    
    /**
     * This returns the header for the signals file.
     * @return the header for the signals file
     */
    public static String getSignalsFileHeader()
    {
        return "node id\ttime_offset";
    }
    
    /**
     * This returns the header for the options file.
     * @return the header for the options file
     */
    public static String getOptionsFileHeader()
    {
        return "name\tvalue";
    }
    
    /**
     * This returns the header for the bus file.
     * @return the header for the bus file
     */
    public static String getBusFileHeader()
    {
        return "id\ttype\troute\tdtime";
    }
    
    /**
     * This returns the header for the bus_frequency file.
     * @return the header for the bus_frequency file
     */
    public static String getBusFrequencyFileHeader()
    {
        return "route\tperiod\tfrequency\toffset";
    }
    
    /**
     * This returns the header for the bus_route_link file.
     * @return the header for the bus_route_link file
     */
    public static String getBusRouteLinkFileHeader()
    {
        return "route\tsequence\tlink\tstop\tdwelltime";
    }
    
    /**
     * This returns the header for the bus_period file.
     * @return the header for the bus_period file
     */
    public static String getBusPeriodFileHeader()
    {
        return "id\tstarttime\tendtime";
    }
    
    
    
    /**
     * Performs a sanity check on the network data contained within the {@link Project}.
     * @param project the {@link Project}
     * @param fileout the {@link PrintStream} to print errors to
     * @return the number of errors found
     */
    public int sanityCheck(Project project, PrintStream fileout)
    {
        int output = 0;
        
        Map<Integer, NodeRecord> tempnodes = new HashMap<Integer, NodeRecord>();
        
        boolean loadNetwork = true;
        
        fileout.println("<h2>Network</h3>");
        
        fileout.println("<h3>"+project.getNodesFile().getName()+"</h3>");
        
        // create map of node ids to NodeRecords
        Scanner filein = null;
        try
        {
            filein = new Scanner(project.getNodesFile());
        }
        catch(IOException ex)
        {
            output++;
            loadNetwork = false;
            print(fileout, BAD_FILE, project.getNodesFile().getName()+" file not found.");
            return output;
        }
        
        if(!filein.hasNextLine())
        {
            output++;
            loadNetwork = false;
            print(fileout, BAD_FILE, project.getNodesFile().getName()+" file is empty.");
            return output;
        }
        filein.nextLine();
        
        int lineno = 1;
        int count = 0;
        
        while(filein.hasNextLine())
        {
            try
            {
                lineno++;
                NodeRecord node = new NodeRecord(filein.nextLine());
                count++;
                
                if(node.getId() <= 0)
                {
                    output++;
                    print(fileout, ODD_DATA, "Node "+node.getId()+" has non-positive id at line "+lineno+".");
                }
                
                // check for duplicate ids
                if(tempnodes.containsKey(node.getId()))
                {
                    output++;
                    print(fileout, DATA_MISMATCH, "Duplicate node id of "+node.getId()+" at line "+lineno+".");
                }
                
                tempnodes.put(node.getId(), node);
                
                
                // check node type
                boolean foundType = false;
                
                for(Type t : NODE_TYPES)
                {
                    if(node.getType() == t.getCode())
                    {
                        foundType = true;
                        break;
                    }
                }
                
                if(foundType == false)
                {
                    for(Type t : NODE_EXT_TYPES)
                    {
                        if(node.getType() == t.getCode())
                        {
                            foundType = true;
                            break;
                        }
                    }
                }
                
                
                if(!foundType)
                {
                    print(fileout, ODD_DATA, "Unrecognized type for node "+node.getId()+": "+node.getType()+" on line "+lineno+".");
                }
                
            }
            catch(Exception ex)
            {
                output++;
                loadNetwork = false;
                print(fileout, MALFORMED_DATA, "Malformed node data at line "+lineno+".");
            }
        }
        filein.close();
        
        print(fileout, NORMAL, "Scanned "+count+" nodes.");
        
        
        
        
        
        
        Set<Integer> linkids = new HashSet<Integer>();
        
        fileout.println("<h3>"+project.getLinksFile()+"</h3>");
        try
        {
            filein = new Scanner(project.getLinksFile());
        }
        catch(IOException ex)
        {
            output++;
            loadNetwork = false;
            print(fileout, BAD_FILE, project.getLinksFile().getName()+" file not found.");
            return output;
        }
        
        if(!filein.hasNextLine())
        {
            output++;
            loadNetwork = false;
            print(fileout, BAD_FILE, project.getLinksFile().getName()+" file is empty.");
            return output;
        }
        
        filein.nextLine();
        
        lineno = 1;
        count = 0;
        
        while(filein.hasNextInt())
        {
            try
            {
                lineno++;
                LinkRecord link = new LinkRecord(filein.nextLine());
                count++;
                
                if(link.getId() <= 0)
                {
                    output++;
                    print(fileout, ODD_DATA, "Link "+link.getId()+" has non-positive id at line "+lineno+".");
                }
                
                // check duplicate id
                if(linkids.contains(link.getId()))
                {
                    output++;
                    print(fileout, DATA_MISMATCH, "Duplicate link id: "+link.getId()+" at line "+lineno+".");
                }
                
                linkids.add(link.getId());
                
                // check that nodes exist
                if(!tempnodes.containsKey(link.getSource()))
                {
                    output++;
                    print(fileout, DATA_MISMATCH, "Cannot find source node "+link.getSource()+" for link "+link.getId()+" at line "+lineno+".");
                }
                if(!tempnodes.containsKey(link.getDest()))
                {
                    output++;
                    print(fileout, DATA_MISMATCH, "Cannot find destination node "+link.getDest()+" for link "+link.getId()+" at line "+lineno+".");
                }
                
                // check link type
                boolean foundType = false;
                
                for(Type type : LINK_TYPES)
                {
                    if(link.getType() == type.getCode())
                    {
                        foundType = true;
                        break;
                    }
                }
                
                if(!foundType)
                {
                    for(Type type : LINK_EXT_TYPES)
                    {
                        if(link.getType() == type.getCode())
                        {
                            foundType = true;
                            break;
                        }
                    }
                }

                
                if(!foundType)
                {
                    print(fileout, ODD_DATA, "Link type not recognized for link "+link.getId()+": "+link.getType()+" on line "+lineno+".");
                }
                
                // for centroid connectors: check that one node is a zone
                if(link.isCentroidConnector())
                {
                    if(tempnodes.get(link.getSource()).isZone() && tempnodes.get(link.getDest()).isZone())
                    {
                        output++;
                        print(fileout, DATA_MISMATCH, "Centroid connector "+link.getId()+" is connected to two zones at line "+lineno+".");
                    }
                    else if(!tempnodes.get(link.getSource()).isZone() && !tempnodes.get(link.getDest()).isZone())
                    {
                        output++;
                        print(fileout, DATA_MISMATCH, "Centroid connector "+link.getId()+" is not connected to any zones at line "+lineno+".");
                    }
                }
                // for non-centroid connectors: check that no nodes are zones
                else
                {
                    if(tempnodes.get(link.getSource()).isZone())
                    {
                        output++;
                        print(fileout, DATA_MISMATCH, "Link "+link.getId()+" has a source zone of "+link.getSource()+" at line "+lineno+".");
                    }
                    if(tempnodes.get(link.getDest()).isZone())
                    {
                        output++;
                        print(fileout, DATA_MISMATCH, "Link "+link.getId()+" has a destination zone of "+link.getDest()+" at line "+lineno+".");
                    }
                }
                
                // check free flow speed is reasonable
                if(!link.isCentroidConnector() && (link.getWaveSpd() < 5 || link.getWaveSpd() > 90))
                {
                    output++;
                    print(fileout, ODD_DATA, "Link "+link.getId()+": congested wave speed is "+link.getWaveSpd()+" mi/hr at line "+lineno+".");
                }
                
                // check congested wave speed
                if(!link.isCentroidConnector() && (link.getFFSpd() < 10 || link.getFFSpd() > 90))
                {
                    output++;
                    print(fileout, ODD_DATA, "Link "+link.getId()+": free flow speed is "+link.getFFSpd()+" mi/hr at line "+lineno+".");
                }
                
                // check number of lanes
                if(!link.isCentroidConnector() && (link.getNumLanes() < 1 || link.getNumLanes() > 6))
                {
                    output++;
                    print(fileout, ODD_DATA, "Link "+link.getId()+": number of lanes is "+link.getNumLanes()+" at line "+lineno+".");
                }
                
                // check length is reasonable
                if(!link.isCentroidConnector() && (link.getLength() < 20.0/5280 || link.getLength() > 20))
                {
                    output++;
                    print(fileout, ODD_DATA, "Link "+link.getId()+": length is "+link.getLength()+" mi at line "+lineno+".");
                }
                
                // check capacity is reasonable
                if(!link.isCentroidConnector() && (link.getCapacity() < 500 || link.getCapacity() > 3000))
                {
                    output++;
                    print(fileout, ODD_DATA, "Link "+link.getId()+": capacity is "+link.getCapacity()+" veh/hr per lane at line "+lineno+".");
                }
            }
            catch(Exception ex)
            {
                output++;
                loadNetwork = false;
                print(fileout, MALFORMED_DATA, "Malformed link data at line "+lineno+".");
            }
        }
        filein.close();
        print(fileout, NORMAL, "Scanned "+count+" links.");
        
        fileout.println("<h3>"+project.getLinkPointsFile().getName()+"</h3>");
        
        try
        {
            filein = new Scanner(project.getLinkPointsFile());
            
            
            if(!filein.hasNextLine())
            {
                output++;
                print(fileout, BAD_FILE, project.getLinkPointsFile().getName()+" file is empty.");
            }
            
            lineno = 1;
            count = 0;

            filein.nextLine();

            while(filein.hasNextInt())
            {
                lineno++;
                

                int id = filein.nextInt();
                filein.nextLine();
                count++;

                if(!linkids.contains(id))
                {
                    output++;
                    print(fileout, DATA_MISMATCH, "Link "+id+" appears in "+project.getLinkPointsFile().getName()+" but not in "+project.getLinksFile().getName()+" at line "+lineno+".");
                }
                linkids.remove(id);
            }
            filein.close();
        }
        catch(IOException ex)
        {
            output++;
            print(fileout, BAD_FILE, project.getLinkPointsFile().getName()+" file not found.");
        }
        
        
        tempnodes = null;
        
        
            
        
        for(int id : linkids)
        {
            print(fileout, ODD_DATA, "Link "+id+" appears in "+project.getLinksFile().getName()+" but not in "+project.getLinkPointsFile().getName()+".txt");
        }
        
        linkids = null;
        
        print(fileout, NORMAL, "Scanned "+count+" links.");
        
        
        if(!loadNetwork)
        {
            print(fileout, NORMAL, "<p>Sanity check ended before network loading due to errors.</p>");
            return output;
        }
   
        
        fileout.println("<h3>Network structure</h3>");
        
        readOptions(project);
        try
        {
            Set<Node> nodes = readNodes(project);
            Set<Link> links = readLinks(project);
            
            // check for regular nodes without incoming/outgoing links
            // check for centroids without any incoming/outgoing links
            for(Node n : nodes)
            {
                if(n.isZone())
                {
                    if(n.getId() > 0)
                    {
                        if(n.getOutgoing().size() == 0 && nodesmap.get(-n.getId()).getIncoming().size() == 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "Zone "+n.getId()+" has no incoming or outgoing links.");
                        }
                    }
                    
                }
                else
                {
                    if(n.getIncoming().size() == 0)
                    {
                        output++;
                        print(fileout, ODD_DATA, "Node "+n.getId()+" has no incoming links.");
                    }
                    if(n.getOutgoing().size() == 0)
                    {
                        output++;
                        print(fileout, ODD_DATA, "Node "+n.getId()+" has no outgoing links.");
                    }
                }
            }
            
            print(fileout, NORMAL, "Scanned "+nodes.size()+" nodes.");
        }
        catch(Exception ex)
        {
            output++;
            print(fileout, BAD_FILE, "Unable to load network.");
            ex.printStackTrace(fileout);
            return output;
        }
        
        fileout.println("<h3>"+project.getSignalsFile().getName()+"</h3>");
        
        Set<Integer> signalnodes = new HashSet<Integer>();
        
        try
        {
            filein = new Scanner(project.getSignalsFile());
            
            if(!filein.hasNextLine())
            {
                output++;
                print(fileout, BAD_FILE, project.getSignalsFile().getName()+" file is empty.");
            }
            else
            {
                filein.nextLine();

                count = 0;
                lineno = 1;

                while(filein.hasNextInt())
                {
                    try
                    {
                        lineno++;
                        SignalRecord signal = new SignalRecord(filein.nextLine());
                        count++;

                        
                        signalnodes.add(signal.getNode());
                        
                        if(!nodesmap.containsKey(signal.getNode()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Node "+signal.getNode()+" appears in "+project.getSignalsFile()+" but not in "+project.getNodesFile()+" on line "+lineno+".");
                        }

                        if(signal.getOffset() < 0)
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Signal for node "+signal.getNode()+" has offset of "+signal.getOffset()+" on line "+lineno+".");
                        }
                    }
                    catch(Exception ex)
                    {
                        output++;
                        print(fileout, BAD_FILE, "Malformed signal data on line "+lineno+".");
                    }
                }
                print(fileout, NORMAL, "Scanned "+count+" signals.");
            }
            filein.close();
        }
        catch(IOException ex)
        {
            output++;
            print(fileout, BAD_FILE, project.getSignalsFile().getName()+" file not found.");
        }
        
            
        
        fileout.println("<h3>"+project.getPhasesFile().getName()+"</h3>");
        
        
        try
        {
            filein = new Scanner(project.getPhasesFile());
            
            if(!filein.hasNextLine())
            {
                output++;
                print(fileout, BAD_FILE, project.getPhasesFile().getName()+" file is empty.");
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
                        PhaseRecord phase = new PhaseRecord(filein.nextLine());
                        count++;
                        
                        if(!signalnodes.contains(phase.getNode()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Node "+phase.getNode()+" appears in "+project.getPhasesFile().getName()+" but not in "+project.getSignalsFile().getName()+" on line "+lineno+".");
                        }

                        if(!nodesmap.containsKey(phase.getNode()))
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Node "+phase.getNode()+" appears in "+project.getPhasesFile().getName()+" but not in "+project.getNodesFile().getName()+" on line "+lineno+".");
                        }
                        else
                        {
                            Node node = nodesmap.get(phase.getNode());
                            for(TurnRecord t : phase.getTurns())
                            {
                                boolean found = false;

                                for(Link i : node.getIncoming())
                                {
                                    if(i.getId() == t.getI())
                                    {
                                        found = true;
                                        break;
                                    }
                                }

                                

                                if(!found)
                                {
                                    print(fileout, DATA_MISMATCH, "Link "+t.getI()+" is not an incoming link for node "+phase.getNode()+" for phase "+phase.getSequence()+" on line "+lineno+".");
                                }
                                
                                found = false;

                                for(Link j : node.getOutgoing())
                                {
                                    if(j.getId() == t.getJ())
                                    {
                                        found = true;
                                        break;
                                    }
                                }

                                if(!found)
                                {
                                    output++;
                                    print(fileout, DATA_MISMATCH, "Link "+t.getJ()+" is not an outgoing link for node "+phase.getNode()+" for phase "+phase.getSequence()+" on line "+lineno+".");
                                }
                            }
                        }

                        if(phase.getTurns().size() == 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "No turns for phase "+phase.getSequence()+" for node "+phase.getNode()+" on line "+lineno+".");
                        }
                        if(phase.getTimeGreen() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "Phase "+phase.getSequence()+" for node "+phase.getNode()+" has green time of "+phase.getTimeGreen()+" on line "+lineno+".");
                        }

                        if(phase.getTimeYellow() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "Phase "+phase.getSequence()+" for node "+phase.getNode()+" has yellow time of "+phase.getTimeYellow()+" on line "+lineno+".");
                        }

                        if(phase.getTimeRed() <= 0)
                        {
                            output++;
                            print(fileout, ODD_DATA, "Phase "+phase.getSequence()+" for node "+phase.getNode()+" has red time of "+phase.getTimeRed()+" on line "+lineno+".");
                        }

                        if(phase.getSequence() <= 0)
                        {
                            output++;
                            print(fileout, DATA_MISMATCH, "Phase "+phase.getSequence()+" for node "+phase.getNode()+" has non-positive sequence number on line "+lineno+".");
                        }


                    }
                    catch(Exception ex)
                    {
                        output++;
                        print(fileout, BAD_FILE, "Malformed phase data on line "+lineno+".");
                    }
                }

                print(fileout, NORMAL, "Scanned "+count+" phases.");
            }
            filein.close();
        }
        catch(IOException ex)
        {
            output++;
            print(fileout, BAD_FILE, project.getPhasesFile().getName()+" file not found.");
        }
        
        
        
        return output;
    }
    
    public void print(PrintStream fileout, int code, String text)
    {
        String color;
        switch(code)
        {
            case 1: 
                color = "#FF0000"; 
                break;
            case 2: 
                color = "#FF8000";
                break;
            case 3: 
                color = "#008000";
                break;
            default: 
                color="#000000";
                break;
        }
        fileout.println("<font color=\""+color+"\">"+text+"</font><br>");
    }
}
