package avdta.network;

import avdta.network.cost.TravelCost;
import avdta.network.link.CTMLink;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.Link;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;


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
    
    public Network()
    {
        this(new HashSet<Node>(), new HashSet<Link>());
    }
    public Network(Set<Node> nodes, Set<Link> links)
    {
        setNetwork(nodes, links);
        
        this.costFunc = TravelCost.ttCost;
        paths = new PathList();
    }
    
    public static boolean getHVsUseReservations()
    {
        return HVs_use_reservations;
    }
    
    public void setNodes(Set<Node> nodes)
    {
        this.nodes = nodes;
        
        for(Node n : nodes)
        {
            n.initialize();
        }
    }
    
    public Set<Link> getLinks()
    {
        return links;
    }
    
    public void setHVsUseReservations(boolean h)
    {
        HVs_use_reservations = h;
    }
    
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
    
    public void setNetwork(Set<Node> nodes, Set<Link> links)
    {
        setNodes(nodes);
        setLinks(links);
    }
    
    public void setUseLinkDijkstras(boolean l)
    {
    	link_dijkstras = l;
    }
    
    public Set<Node> getNodes()
    {
        return nodes;
    }
    
    public TravelCost getCostFunction()
    {
        return costFunc;
    }
    
    public PathList getPaths()
    {
        return paths;
    }
    
    public void setPaths(PathList p)
    {
        paths = p;
    }
    
    public void setCostFunction(TravelCost cost)
    {
        costFunc = cost;
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
    
    public Node findNode(int id)
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
    
    public Link findLink(int id)
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
    
    public Path findPath(Node o, Node d)
    {
        return findPath(o, d, 0, 0, DriverType.AV, costFunc);
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
        
        return paths.addPath(output);
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
    
    public static void setDLR(boolean d)
    {
        dlr = d;
        
        
    }
    
    public static boolean isDLR()
    {
        return dlr;
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
    
    public Map<Integer, Link> createLinkIdsMap()
    {
        Map<Integer, Link> output = new HashMap<Integer, Link>();
        
        for(Link l : links)
        {
            output.put(l.getId(), l);
        }
        
        return output;
    }
    
    public Map<Integer, Node> createNodesMap()
    {
        Map<Integer, Node> output = new HashMap<Integer, Node>();
        
        for(Node n : nodes)
        {
            output.put(n.getId(), n);
        }
        
        return output;
    }
}