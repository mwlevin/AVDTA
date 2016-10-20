package avdta.network;

import avdta.network.cost.TravelCost;
import avdta.network.link.CTMLink;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.Link;
import avdta.network.link.TransitLane;
import avdta.network.link.LinkRecord;
import avdta.network.node.Intersection;
import avdta.network.node.Node;
import avdta.network.link.SharedTransitCTMLink;
import avdta.network.node.Location;
import avdta.network.node.NodeRecord;
import avdta.network.node.Phase;
import avdta.network.node.Signalized;
import avdta.network.node.TBR;
import avdta.network.node.Turn;
import avdta.network.node.Zone;
import avdta.project.Project;
import avdta.vehicle.DriverType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A {@link Network} contains the {@link Node}s and {@link Link}s that vehicles would pass through during simulation. 
 * It also contains methods for controlling or accessing the dynamic network loading, such as shortest path.
 * The {@link Network} does not itself contain or simulate vehicles. See {@link Simulator}.
 * @author Michael
 */
public class Network
{
    
    private PathList paths;

    public static int dt = 6;
    
    
    
    public static boolean dlr;  
    
    
    public Set<Node> nodes;
    public Set<Link> links;
    
    protected static boolean HVs_use_reservations = false;
    
    private boolean link_dijkstras=true; // dijkstras
    
    public TravelCost costFunc;
    
    /**
     * Constructs an empty {@link Network}
     */
    public Network()
    {
        this(new HashSet<Node>(), new HashSet<Link>());
    }
    
    /**
     * Constructs this {@link Network} with the specified {@link Node}s and {@link Link}s
     * @param nodes the set of {@link Node}s
     * @param links the set of {@link Link}s
     */
    public Network(Set<Node> nodes, Set<Link> links)
    {
        setNetwork(nodes, links);
        
        this.costFunc = TravelCost.ttCost;
        paths = new PathList();
    }
    
    /**
     * Returns whether human-driven vehicles can use reservations. 
     * If so, they are required to reserve all possible turning movements.
     * If not, they will attempt to avoid reservations during path finding.
     * Even if this returns false, human-driven vehicles can still be forced to use reservations due to lack of connectivity.
     * @return whether human-driven vehicles can use reservations
     */
    public static boolean getHVsUseReservations()
    {
        return HVs_use_reservations;
    }
    
    /**
     * Updates the {@link Node}s of this network.
     * All {@link Node}s in the set are initialized (see {@link Node#initialize()}).
     * @param nodes the new set of {@link Node}s
     */
    public void setNodes(Set<Node> nodes)
    {
        this.nodes = nodes;
        
        for(Node n : nodes)
        {
            n.initialize();
        }
    }
    
    /**
     * Returns the total travel time for all links in the specified set.
     * @param links the set of links to analyze
     * @param time the analysis time (s)
     * @return the total travel time (s)
     */
    public double getTT(Collection<Link> links, int time)
    {
        double output = 0.0;
        
        for(Link l : links)
        {
            output += l.getAvgTT(time);
        }
        
        return output;
    }
    
    
    /**
     * Returns the set of {@link Link}s
     * @return the set of {@link Link}s
     */
    public Set<Link> getLinks()
    {
        return links;
    }
    
    /**
     * Updates whether human-driven vehicles can use reservations.
     * If so, they are required to reserve all possible turning movements.
     * If not, they will attempt to avoid reservations during path finding.
     * Even if this is false, human-driven vehicles can still be forced to use reservations due to lack of connectivity.
     * This is initially set to false.
     * @param h whether human-driven vehicles can use reservations
     */
    public void setHVsUseReservations(boolean h)
    {
        HVs_use_reservations = h;
    }
    
    /**
     * Updates the set of {@link Link}s.
     * All {@link Link}s in the set are initialized (see {@link Link#initialize()}).
     * In addition, dynamic transit lanes ({@link SharedTransitCTMLink}) are tied with their {@link TransitLane} counterparts (see {@link SharedTransitCTMLink#tieCells()}).
     * @param links the new set of {@link Link}s
     */
    public void setLinks(Set<Link> links)
    {
        this.links = links;
        
        boolean sharedTransit = false;
        
        for(Link l : links)
        {
            l.initialize();
            
            sharedTransit = sharedTransit || (l instanceof SharedTransitCTMLink);
        }
        
        if(sharedTransit)
        {
            for(Link l : links)
            {
                if(l instanceof SharedTransitCTMLink)
                {
                    ((SharedTransitCTMLink)l).tieCells();
                }
            }
        }
        
    }
    
    /**
     * Updates both the set of {@link Node}s and the set of {@link Link}s.
     * @param nodes the new set of {@link Node}s
     * @param links the new set of {@link Link}s 
     * @see Network#setNodes(java.util.Set) 
     * @see Network#setLinks(java.util.Set) 
     */
    public void setNetwork(Set<Node> nodes, Set<Link> links)
    {
        setNodes(nodes);
        setLinks(links);
    }
    
    /**
     * Sets whether to use Dijkstra's on the dual graph.
     * This increases computation time, but is the correct method to use in networks containing intersections with limited turning movements.
     * If all intersections do not limit turning movements (such as with reservations or stop signs), this can be set to false.
     * This is initially set to true.
     * @param l whether to use Dijkstra's on the dual graph
     */
    public void setUseLinkDijkstras(boolean l)
    {
    	link_dijkstras = l;
    }
    
    /**
     * Returns the set of {@link Node}s
     * @return the set of {@link Node}s
     */
    public Set<Node> getNodes()
    {
        return nodes;
    }
    
    /**
     * Returns the cost function being used for shortest path.
     * @return the cost function being used for shortest path
     * @see TravelCost
     */
    public TravelCost getCostFunction()
    {
        return costFunc;
    }
    
    /**
     * Returns the list of {@link Path}s.
     * @return the list of {@link Path}s
     * @see PathList
     */
    public PathList getPaths()
    {
        return paths;
    }
    
    /**
     * Updates the list of {@link Path}s.
     * @param p the new list of {@link Path}s
     * @see PathList
     */
    public void setPaths(PathList p)
    {
        paths = p;
    }
    
    /**
     * Updates the cost function used for shortest paths
     * @param cost the new cost function
     * @see TravelCost
     */
    public void setCostFunction(TravelCost cost)
    {
        costFunc = cost;
    }
    
    /**
     * Searches for the {@link Node} with the given id. 
     * Note that this is O(n). 
     * If many searches are desired, creating a mapping of ids to {@link Node}s is faster (see {@link Network#createNodeIdsMap()}).
     * @param id the id to search for
     * @return the {@link Node} with the given id, or null if not found
     */
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
    
    
    /**
     * Searches for the {@link Link} with the given id. 
     * Note that this is O(n). 
     * If many searches are desired, creating a mapping of ids to {@link Link}s is faster (see {@link Network#createLinkIdsMap()}).
     * @param id the id to search for
     * @return the {@link Link} with the given id, or null if not found
     */
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
    
    /**
     * Finds the shortest path between the given origin and destination.
     * This method calls {@link Network#dijkstras(avdta.network.node.Node, int, double, avdta.vehicle.DriverType, avdta.network.cost.TravelCost)} and traces the shortest path.
     * @param o the origin
     * @param d the destination
     * @return the shortest path.
     */
    public Path findPath(Node o, Node d)
    {
        return findPath(o, d, 0, 0, DriverType.AV, costFunc);
    }
    
    /**
     * Finds the shortest path between the given origin and destination with the specified departure time, value of time, driver, and cost function.
     * This method calls {@link Network#dijkstras(avdta.network.node.Node, int, double, avdta.vehicle.DriverType, avdta.network.cost.TravelCost)} and traces the shortest path.
     * @param o the origin
     * @param d the destination
     * @param dep_time the departure time (s)
     * @param vot the value of time ($/hr)
     * @param driver indicates whether the driver is human or autonomous, and whether it is transit
     * @param costFunc the cost function used for link travel costs
     * @return the shortest path.
     * @see DriverType
     * @see TravelCost
     */
    public Path findPath(Node o, Node d, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        node_dijkstras(o, dep_time, vot, driver, costFunc);
        Path output = node_trace(o, d);
        
        if(output.size() == 0)
        {
            link_dijkstras(o, dep_time, vot, driver, costFunc);
            output = link_trace(o, d);
        }
        
        return paths.addPath(output);
    }

    /**
     * Finds the one-to-all shortest paths with the specified parameters. 
     * This calls {@link Network#dijkstras(avdta.network.node.Node, int, double, avdta.vehicle.DriverType, avdta.network.cost.TravelCost, boolean)}.
     * Use {@link Network#trace(avdta.network.node.Node, avdta.network.node.Node)} to get the shortest path between two nodes.
     * @param o the origin
     * @param dep_time the departure time (s)
     * @param vot the value of time ($/hr)
     * @param driver indicates whether the driver is human or autonomous, and whether it is transit
     * @param costFunc the cost function used for link travel costs

     * @see DriverType
     * @see TravelCost
     */
    public void dijkstras(Node o, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        dijkstras(o, dep_time, vot, driver, costFunc, link_dijkstras);
    }
    
    /**
     * Finds the one-to-all shortest paths with the specified parameters. 
     * This calls {@link Network#dijkstras(avdta.network.node.Node, int, double, avdta.vehicle.DriverType, avdta.network.cost.TravelCost, boolean)}.
     * Use {@link Network#node_trace(avdta.network.node.Node, avdta.network.node.Node)} or {@link Network#link_trace(avdta.network.node.Node, avdta.network.node.Node)} to get the shortest path between two nodes.
     * @param o the origin
     * @param dep_time the departure time (s)
     * @param vot the value of time ($/hr)
     * @param driver indicates whether the driver is human or autonomous, and whether it is transit
     * @param costFunc the cost function used for link travel costs
     * @param link_dijkstras whether to find shortest path on the dual graph
     * @see DriverType
     * @see TravelCost
     */
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
    
    /**
     * Finds one-to-all shortest paths on the dual graph.
     * Vehicles may start on any outgoing link from the specified origin.
     * Use {@link Network#link_trace(avdta.network.node.Node, avdta.network.node.Node)} to get the shortest path between two nodes.
     * @param o the origin
     * @param dep_time the departure time (s)
     * @param vot the value of time ($/hr)
     * @param driver indicates whether the driver is human or autonomous, and whether it is transit
     * @param costFunc the cost function used for link travel costs
     * @see DriverType
     * @see TravelCost
     */
    public void link_dijkstras(Node o, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        for(Link l : links)
        {
            l.label = Integer.MAX_VALUE;
            l.arr_time = Integer.MAX_VALUE;
            l.prev = null;
            l.added = false;
        }
        
        PriorityQueue<Link> Q = new PriorityQueue<Link>(8, new Comparator<Link>()
        {
            public int compare(Link lhs, Link rhs)
            {
                return (int)((lhs.label - rhs.label) * 1000);
            }
        });
        
        for(Link l : o.getOutgoing())
        {
            if(!l.canUseLink(driver))
            {
                continue;
            }
            double tt = l.getAvgTT(dep_time);
            l.arr_time = (int)(dep_time);
            
            
            l.label = costFunc.cost(l, vot, dep_time);
            
            Q.add(l);
            l.added = true;
        }
        
        
        while(!Q.isEmpty())
        {
            Link u = Q.remove();
            u.added = false;
            
            Node d = u.getDest();
            
            for(Link v : d.getOutgoing())
            {
                if(!d.canMove(u, v, driver) || !v.canUseLink(driver))
                {
                    continue;
                }
                
                
                double tt = v.getAvgTT(u.arr_time);
                
                double new_label = u.label + costFunc.cost(v, vot, u.arr_time);

                
                if(new_label < v.label)
                {
                    v.arr_time = (int)(u.arr_time + tt);
                   
                    // if v is already in Q, remove v to reset the order
                    if(v.added)
                    {
                        Q.remove(v);
                    }
                    
                    v.label = new_label;
                    v.prev = u;
                    v.added = true;
                    Q.add(v);
                    
                }
            }
        }
        
    }
    
    /**
     * Finds one-to-all shortest paths on the dual graph.
     * Vehicles must start on the specified link. 
     * The origin is the upstream node of the specified {@link Link} ({@link Link#getSource()}).
     * Use {@link Network#link_trace(avdta.network.node.Node, avdta.network.node.Node)} to get the shortest path between two nodes.
     * @param starting the origin {@link Link}
     * @param dep_time the departure time (s)
     * @param vot the value of time ($/hr)
     * @param driver indicates whether the driver is human or autonomous, and whether it is transit
     * @param costFunc the cost function used for link travel costs
     * @see DriverType
     * @see TravelCost
     */
    public void dijkstras(Link starting, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        for(Link l : links)
        {
            l.label = Integer.MAX_VALUE;
            l.arr_time = Integer.MAX_VALUE;
            l.prev = null;
        }
        
        PriorityQueue<Link> Q = new PriorityQueue<Link>(8, new Comparator<Link>()
        {
            public int compare(Link lhs, Link rhs)
            {
                return (int)((lhs.label - rhs.label) * 1000);
            }
        });
        
        double tt = starting.getAvgTT(dep_time);
        starting.arr_time = (int)(dep_time);


        starting.label = 0;

        Q.add(starting);

        
        while(!Q.isEmpty())
        {
            Link u = Q.remove();
            u.added = false;
            
            Node d = u.getDest();
            
            for(Link v : d.getOutgoing())
            {
                if(!d.canMove(u, v, driver))
                {
                    continue;
                }
                
                
                tt = v.getAvgTT(u.arr_time);
                
                double new_label = u.label + costFunc.cost(v, vot, u.arr_time);

                
                if(new_label < v.label)
                {
                    v.arr_time = (int)(u.arr_time + tt);
                   
                    // if v is already in Q, remove v to reset the order
                    if(v.added)
                    {
                        Q.remove(v);
                    }
                    
                    v.label = new_label;
                    v.prev = u;
                    v.added = true;
                    Q.add(v);
                    
                }
            }
        }
        
    }
    
    
    /**
     * Finds one-to-all shortest paths.
     * Use {@link Network#node_trace(avdta.network.node.Node, avdta.network.node.Node)} to get the shortest path between two nodes.
     * @param o the origin
     * @param dep_time the departure time (s)
     * @param vot the value of time ($/hr)
     * @param driver indicates whether the driver is human or autonomous, and whether it is transit
     * @param costFunc the cost function used for link travel costs
     * @see DriverType
     * @see TravelCost
     */
    public void node_dijkstras(Node o, int dep_time, double vot, DriverType driver, TravelCost costFunc)
    {
        for(Node n : nodes)
        {
            n.label = Integer.MAX_VALUE;
            n.prev = null;
            o.added = false;
        }

        o.arr_time = dep_time;
        o.label = 0;

        
        PriorityQueue<Node> Q = new PriorityQueue<Node>(8, new Comparator<Node>()
        {
            public int compare(Node lhs, Node rhs)
            {
                return (int)((lhs.label - rhs.label) * 1000);
            }
        });

        Q.add(o);
        o.added = true;


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

            u.added = false;
            Q.remove(u);

            for(Link l : u.getOutgoing())
            {
                
                if(!l.canUseLink(driver) || (u.prev != null && !u.canMove(u.prev, l, driver)) )
                {
                    continue;
                }
                
                
                

                Node v = l.getDest();

                double temp = u.label;
                int arr_time = u.arr_time;

                
                temp += costFunc.cost(l, vot, arr_time);
                
                if(!l.canUseLink(driver))
                {
                    temp += 10000;
                }

                arr_time += (int)Math.round(l.getAvgTT(arr_time));

                if(temp < v.label)
                {
                    if(v.added)
                    {
                        Q.remove(v);
                    }
                    
                    
                    v.label = temp;
                    v.prev = l;
                    v.arr_time = arr_time;

                    if(!(v instanceof Zone))
                    {
                        v.added = true;
                        Q.add(v);
                    }
                }
            }

        }
    }
    
    /**
     * Returns the shortest path between the two specified nodes.
     * Call this after calling {@link Network#dijkstras(avdta.network.node.Node, int, double, avdta.vehicle.DriverType, avdta.network.cost.TravelCost)}.
     * Calls {@link Network#node_trace(avdta.network.node.Node, avdta.network.node.Node)} or {@link Network#link_trace(avdta.network.node.Node, avdta.network.node.Node)} depending on {@link Network#setUseLinkDijkstras(boolean)}.
     * @param o the origin
     * @param d the destination
     * @return the shortest path between the two nodes, or null if no path was found
     */
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
    
    /**
     * Returns the shortest path between the two specified nodes.
     * Call this after calling {@link Network#node_dijkstras(avdta.network.node.Node, int, double, avdta.vehicle.DriverType, avdta.network.cost.TravelCost)}.
     * @param o the origin
     * @param d the destination
     * @return the shortest path between the two nodes, or null if no path was found
     */
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
    
    /**
     * Returns the shortest path between the two specified nodes.
     * Call this after calling {@link Network#link_dijkstras(avdta.network.node.Node, int, double, avdta.vehicle.DriverType, avdta.network.cost.TravelCost)}.
     * @param o the origin
     * @param d the destination
     * @return the shortest path between the two nodes, or null if no path was found
     */
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
    
    /**
     * Initializes the network. 
     * This should be called before simulating.
     * This calls {@link Node#initialize()} and {@link Link#initialize()} for all {@link Node}s and {@link Link}s.
     * In addition, dynamic transit lanes ({@link SharedTransitCTMLink}) are tied with their {@link TransitLane} counterparts (see {@link SharedTransitCTMLink#tieCells()}).
     * Also, dynamic lane reversal links ({@link DLRCTMLink}) are tied (see {@link Network#tieLinks()}) if dynamic lane reversal is enabled.
     */
    public void initialize()
    {
        for(Node n : nodes)
        {
            n.initialize();
        }
        
        boolean sharedTransit = false;
        
        for(Link l : links)
        {
            l.initialize();
            
            sharedTransit = sharedTransit || (l instanceof SharedTransitCTMLink);
        }
        
        if(sharedTransit)
        {
            for(Link l : links)
            {
                if(l instanceof SharedTransitCTMLink)
                {
                    ((SharedTransitCTMLink)l).tieCells();
                }
            }
        }
        
        if(dlr)
        {
            tieLinks();
        }
        
      
    }
    
    
    /**
     * Sets whether dynamic lane reversal is enabled. 
     * This is false by default.
     * Dynamic lane reversal only occurs on dynamic lane reversal links ({@link DLRCTMLink}).
     * @param d whether DLR is enabled
     */
    public static void setDLR(boolean d)
    {
        dlr = d;
    }
    
    /**
     * Returns whether dynamic lane reversal is enabled. 
     * This is false by default.
     * Dynamic lane reversal only occurs on dynamic lane reversal links ({@link DLRCTMLink}).
     * @return whether dynamic lane reversal is enabled
     */
    public static boolean isDLR()
    {
        return dlr;
    }
    
    /**
     * This ties links for dynamic lane reversal.
     * @see DLRCTMLink
     * @see DLRCTMLink#tieCells(avdta.network.link.DLRCTMLink)
     */
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
                    if(!j.isCentroidConnector() && i.getSource() == j.getDest() && 
                            (i instanceof DLRCTMLink) && (j instanceof DLRCTMLink))
                    {
                        if (((DLRCTMLink)i).tieCells((DLRCTMLink)j))
                        {
                            count++;
                        }
                    } 
                }
            }
        }
        
        System.out.println("Tied "+count+" links");
    }
    
    /**
     * This saves the network data to the specified {@link Project}. 
     * This includes saving nodes, links, link coordinates, and signal data.
     * Note that data will be overwritten.
     * Also, signal data is saved only for intersections with a {@link Signalized}.
     * @param project the {@link Project} to save to.
     * @throws IOException if a file cannot be accessed
     * @see Signalized
     */
    public void save(Project project) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getNodesFile()), true);
        fileout.println(ReadNetwork.getNodesFileHeader());
        
        for(Node n : nodes)
        {
            NodeRecord record = n.createNodeRecord();
            
            if(record != null)
            {
                fileout.println(record);
            }
        }
        
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(project.getLinksFile()), true);
        fileout.println(ReadNetwork.getLinksFileHeader());
        
        for(Link l : links)
        {
            LinkRecord record = l.createLinkRecord();
            
            if(record != null)
            {
                fileout.println(record);
            }
        }
        
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(project.getSignalsFile()), true);
        fileout.println(ReadNetwork.getSignalsFileHeader());
        
        for(Node n : nodes)
        {
            if(n instanceof Intersection)
            {
                Intersection i = (Intersection)n;
                
                if(i instanceof Signalized)
                {
                    fileout.println(i.getId()+"\t"+((Signalized)i).getOffset());
                }
            }
        }
        fileout.close();
        
        
        fileout = new PrintStream(new FileOutputStream(project.getPhasesFile()), true);
        fileout.println(ReadNetwork.getPhasesFileHeader());
        
        for(Node n : nodes)
        {
            if(n instanceof Intersection)
            {
                Intersection i = (Intersection)n;
                Signalized signal = i.getControl().getSignal();
                   
                if(signal != null)
                {
                    int count = 1;
                    for(Phase p : signal.getPhases())
                    {
                        fileout.print(i.getId()+"\t1\t"+count+"\t"+p.getRedTime()+"\t"+p.getYellowTime()+"\t"+p.getGreenTime());
                        
                        
                        Turn[] turns = p.getTurns();
                        fileout.print("\t"+turns.length+"\t{");
                        
                        for(int t = 0; t < turns.length-1; t++)
                        {
                            fileout.print(turns[t].i.getId()+", ");
                        }
                        
                        if(turns.length > 0)
                        {
                            fileout.print(turns[turns.length-1].i.getId());
                        }
                        
                        fileout.print("}\t{");
                        
                        for(int t = 0; t < turns.length-1; t++)
                        {
                            fileout.print(turns[t].j.getId()+", ");
                        }
                        
                        if(turns.length > 0)
                        {
                            fileout.print(turns[turns.length-1].j.getId());
                        }
                        
                        fileout.print("}");
                        fileout.println();
                    }
                }
            }
        }
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(project.getLinkPointsFile()));
        fileout.println(ReadNetwork.getLinkPointsFileHeader());
        
        for(Link l : links)
        {
            fileout.print(l.getId()+"\t");
            
            Location[] coords = l.getCoordinates();
            
            for(int i = 0; i < coords.length-1; i++)
            {
                fileout.print("("+coords[i].getX()+", "+coords[i].getY()+"), ");
            }
            
            if(coords.length > 0)
            {
                fileout.print("("+coords[coords.length-1].getX()+", "+coords[coords.length-1].getY()+")");
            }
            
            fileout.println();
        }
        fileout.close();
    }
    
    /**
     * Creates a mapping of ids to {@link Link}s.
     * @return a mapping of ids to {@link Link}s
     */
    public Map<Integer, Link> createLinkIdsMap()
    {
        Map<Integer, Link> output = new HashMap<Integer, Link>();
        
        for(Link l : links)
        {
            output.put(l.getId(), l);
        }
        
        return output;
    }
    
    /**
     * Creates a mapping of ids to {@link Node}s.
     * @return a mapping of ids to {@link Node}s
     */
    public Map<Integer, Node> createNodeIdsMap()
    {
        Map<Integer, Node> output = new HashMap<Integer, Node>();
        
        for(Node n : nodes)
        {
            output.put(n.getId(), n);
        }
        
        return output;
    }
}