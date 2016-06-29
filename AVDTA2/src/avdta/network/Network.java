package avdta.network;

import avdta.cost.TravelCost;
import avdta.network.link.CTMLink;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.Link;
import avdta.network.node.Intersection;
import avdta.network.node.Node;
import avdta.network.node.TBR;
import avdta.network.node.Zone;
import avdta.vehicle.DriverType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Network
{
    
    private PathList paths;

    public static int dt = 6;
    
    
    
    public static boolean dlr;  
    
    
    public List<Node> nodes;
    public List<Link> links;
    
    protected static boolean HVs_use_reservations = false;
    
    private boolean link_dijkstras=false; // dijkstras
    
    public TravelCost costFunc;
    
    public Network()
    {
        nodes = new ArrayList<Node>();
        links = new ArrayList<Link>();
    }
    public Network(List<Node> nodes, List<Link> links)
    {
        this.nodes = nodes;
        this.links = links; 
        
        this.costFunc = TravelCost.ttCost;
        
        for(Node n : nodes)
        {
            n.initialize();
        }
        
        paths = new PathList();
    }
    
    public void setNodes(List<Node> nodes)
    {
        this.nodes = nodes;
    }
    
    public void setHVsUseReservations(boolean h)
    {
        HVs_use_reservations = h;
    }
    
    public void setLinks(List<Link> links)
    {
        this.links = links;
    }
    
    public void setNetwork(List<Node> nodes, List<Link> links, int linktype)
    {
        setNodes(nodes);
        setLinks(links);
    }
    
    public void setUseLinkDijkstras(boolean l)
    {
    	link_dijkstras = l;
    }
    
    public List<Node> getNodes()
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
        
        for(Link l : links)
        {
            l.initialize();
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