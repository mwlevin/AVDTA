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
import avdta.network.node.BackPressureObj;
import avdta.network.node.P0Obj;
import avdta.project.DTAProject;
import avdta.project.Project;
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
import java.util.TreeSet;

/** 
 * This class contains methods to read network data from the VISTA data format.
 * @author Michael
 */
public class ReadNetwork 
{
    // link types - actual type is multiplied by 100
    public static final int LTM = 2;
    public static final int CTM = 1;
    public static final int CENTROID = 10;
    
    
    // node types - actual type is multiplied by 100
    //public static final int CENTROID = 100;
    public static final int SIGNAL = 1;
    public static final int STOPSIGN = 2;
    public static final int RESERVATION = 3;
    
    // reservation policies
    public static final int FCFS = 1;
    public static final int PRESSURE = 2;
    public static final int P0 = 3;
    public static final int AUCTION = 10;
    public static final int RANDOM = 9;
 
    
    public static final int SIGNALIZED_RESERVATION = 20;
    public static final int PHASED = SIGNALIZED_RESERVATION+1;
    public static final int WEIGHTED = SIGNALIZED_RESERVATION+2;
    
    
    
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
    
    public Simulator readNetwork(Project project) throws IOException
    {
        readOptions(project);
        List<Node> nodes = readNodes(project);
        List<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        
        Simulator sim = new Simulator(project, nodes, links);
        
        sim.initialize();
        
        return sim;
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
            
            if(type/100 == CENTROID)
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
                    case SIGNAL:
                        node.setControl(new TrafficSignal());
                        break;
                    case STOPSIGN:
                        node.setControl(new StopSign());
                        break;
                    case RESERVATION:
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
    
    public List<Node> readNodes(Project project) throws IOException
    {
        List<Node> nodes = new ArrayList<Node>();
        
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

            if(type / 100 == CENTROID)
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
    
    public List<Link> readLinks(Project project) throws IOException
    {
        List<Link> links = new ArrayList<Link>();
        
        
        
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
            double capacity = filein.nextDouble();
            int numLanes = filein.nextInt();
            double jamd = 5280.0/Vehicle.vehicle_length;
            
            filein.nextLine();
            
            Link link = null;
            
            
            switch(type/100)
            {   

                case CENTROID: 
                    link = new CentroidConnector(id, nodesmap.get(source_id), nodesmap.get(dest_id));
                    break;
                case LTM:
                     link = new LTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, ffspd*mesodelta, jamd, length, numLanes);
                     break;
                case CTM:
                     link = new CTMLink(id, nodesmap.get(source_id), nodesmap.get(dest_id), capacity, ffspd, ffspd*mesodelta, jamd, length, numLanes);
                     break;
                default:
                    throw new RuntimeException("Link type not recognized: "+type);
            }
            
            
            links.add(link);
            
            
            linksmap.put(id, link);
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
            int type = filein.nextInt();
            int nodeid = filein.nextInt();
            int offset = filein.nextInt();
            int phaseid = filein.nextInt();
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

            double duration = timegreen + timeyellow + timered;
            Phase phase = new Phase(turns, timegreen, duration);

            if(turns.length > 0)
            {   
                ((Signalized)((Intersection)node).getControl()).addPhase(phase);
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
            
            while(filein.hasNext())
            {
                String key = filein.next();
                String val = filein.next();
                
                project.setOption(key, val);
                
                if(key.equals("simulation-mesoscopic-delta"))
                {
                    double delta = Double.parseDouble(val);
                }
                else if(key.equals("simulation-duration"))
                {
                    Simulator.duration = Integer.parseInt(val);
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
                    Network.HVs_use_reservations = Integer.parseInt(val) > 0;
                }
                else if(key.equals("dynamic-lane-reversal") && Integer.parseInt(val) == 1)
                {
                    Network.setDLR(true);
                }
            }
        }
        catch(IOException ex)
        {
            
        }
    }
    
    
    public static void fillOptions(String name) throws IOException
    {
        PrintStream fileout = new PrintStream(new File("data/"+name+"/options.txt"));
        fileout.println("key\tvalue");
        fileout.println("simulation-mesoscopic-delta\t0.5");
        fileout.println("simulation-duration\t36000");
        fileout.println("hv-reaction-time\t1");
        fileout.println("av-reaction-time\t1");
        fileout.println("hvs-use-reservations\t0");
        fileout.println("dynamic-lane-reversal\t0");
        fileout.close();
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

}
