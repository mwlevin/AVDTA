/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.dta.DTASimulator;
import static avdta.dta.ReadDTANetwork.getDemandFileHeader;
import avdta.dta.VehicleRecord;
import avdta.network.AST;
import avdta.network.DemandProfile;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.sav.Traveler;
import avdta.sav.SAVSimulator;
import avdta.sav.SAVDest;
import avdta.sav.SAVOrigin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import avdta.project.SAVProject;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.TreeSet;

/**
 *
 * @author Michael
 */
public class ReadSAVNetwork extends ReadNetwork
{
    public static final int TAXI = 200;
    
    public static final int TRAVELER = 100;
    
    private int total_productions;

    public ReadSAVNetwork()
    {
   
    }
    
    public SAVSimulator readNetwork(SAVProject project) throws IOException
    {
        readOptions(project);
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        nodes = replaceZones(nodes);
        
        linkZones(nodes);
        createMissingZones(nodes, links);
        
        SAVSimulator sim = new SAVSimulator(project, nodes, links);

        sim.initialize();
        
        
        sim.setTravelers(readTravelers(project));
        readFleet(project, sim);
        
        return sim;
    }
    /**
     * When origins/destinations have separate zones, this attempts to link these separated zones. Origins are expected to have id of 100000+x, with corresponding destination id of 200000+x. Linked zones can be accessed from {@link Zone}.
     * @param nodes the set of nodes
     */
    public void linkZones(Set<Node> nodes)
    {
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
    
    
    public List<Traveler> readTravelers(SAVProject project) throws IOException
    {
        List<Traveler> travelers = new ArrayList<Traveler>();
        
        Scanner filein = new Scanner(project.getTripsFile());
        
        filein.nextLine();
        
        total_productions = 0;
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int origin_id = filein.nextInt();
            int dest_id = filein.nextInt();
            
            filein.next();
            int dtime = filein.nextInt();
            int type = filein.nextInt();
         
            if(type == TRAVELER)
            {
                try
                {
                    total_productions++;
                    SAVOrigin origin = (SAVOrigin)nodesmap.get(origin_id);
                    SAVDest dest = (SAVDest)nodesmap.get(dest_id);

                    origin.addProductions(1);
                    dest.addAttractions(1);

                    travelers.add(new Traveler(id, origin, dest, dtime));
                }
                catch(Exception ex)
                {
                    System.out.println(origin_id+" "+dest_id);
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
                

                
                
            }
        }
        
        filein.close();

        return travelers;
        
    }
    

    
    
    public Set<Node> replaceZones(Set<Node> nodes) throws IOException
    {
        Set<Node> nodes2 = new HashSet<Node>();
        
        for(Node n : nodes)
        {
            if(n instanceof Zone)
            {
                Zone node;
                if(n.getOutgoing().size() > 0)
                {
                    node = new SAVOrigin(n.getId());
                    
                }
                else
                {
                    node = new SAVDest(n.getId());
                }
                
                for(Link l : n.getOutgoing())
                {
                    l.setSource(node);
                }
                
                for(Link l : n.getIncoming())
                {
                    l.setDest(node);
                }
                
                
                node.setIncoming(n.getIncoming());
                node.setOutgoing(n.getOutgoing());
                nodes2.add(node);
                nodesmap.put(n.getId(), node);
            }
            else
            {
                nodes2.add(n);
            }
        }
        
        return nodes2;
    }
    
    /**
     * After linking zones, if some origins/destinations lack a counterpart, this creates the missing origins/destinations as well as their centroid connectors.
     * @param nodes the set of nodes
     * @param links the set of links 
     */
    public void createMissingZones(Set<Node> nodes, Set<Link> links)
    {
        for(int id : zones.keySet())
        {
            
            Zone zone = zones.get(id);
            
            if(zone.getLinkedZone() == null)
            {
                if(zone instanceof SAVOrigin)
                {
                    Zone newZone = new SAVDest(id+400000);
                    
                    // replicate centroid connectors
                    for(Link l : zone.getOutgoing())
                    {
                        if(l instanceof CentroidConnector)
                        {
                            CentroidConnector newLink = new CentroidConnector(l.getId()+400000, l.getDest(), newZone);
                            
                            links.add(newLink);
                        }
                    }
                    
                    zone.setLinkedZone(newZone);
                    newZone.setLinkedZone(zone);
                    nodes.add(newZone);
                    
                    System.out.println("Added "+newZone);
                }
                else
                {
                    Zone newZone = new SAVOrigin(id+400000);
                    
                    // replicate centroid connectors
                    for(Link l : zone.getIncoming())
                    {
                        if(l instanceof CentroidConnector)
                        {
                            CentroidConnector newLink = new CentroidConnector(l.getId()+300000, newZone, l.getSource());
                            
                            links.add(newLink);
                        }
                    }
                    
                    zone.setLinkedZone(newZone);
                    newZone.setLinkedZone(zone);
                    nodes.add(newZone);
                    
                    System.out.println("Added "+newZone);
                }
            }
            
        }
    }
    
    public int prepareDemand(SAVProject project, double prop) throws IOException
    {
        DemandProfile profile = readDemandProfile(project);
        Scanner filein = new Scanner(project.getDynamicODFile());
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getTripsFile()), true);
        fileout.println(getTripsFileHeader());
        
        filein.nextLine();
        
        Random rand = project.getRandom();
        
        int total = 0;

        int new_id = 1;
        while(filein.hasNextInt())
        {
            filein.nextInt();
            int type = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            int t = filein.nextInt();
            double demand = filein.nextDouble() * prop;
            
            filein.nextLine();

           
            AST ast = profile.get(t);
            
            
            
            int num_trips = (int)Math.floor(demand);
            double rem = demand - Math.floor(demand);
            
            
            if(rand.nextDouble() < rem)
            {
                num_trips ++;
            }
            
            int dtime_interval = ast.getDuration() / (num_trips+1);
            
            
            for(int i = 0; i < num_trips; i++)
            {
                int dtime = ast.getStart() + (i+1) * dtime_interval;
                
                fileout.println((new_id++)+"\t"+type+"\t"+origin+"\t"+dest+"\t"+dtime);
            }
            
            total += num_trips;
           
        }
        
        filein.close();       
        fileout.close();
        

        return total;
        
    }
    
    public DemandProfile readDemandProfile(SAVProject project) throws IOException
    {
        DemandProfile output = new DemandProfile();
        
        Scanner filein = new Scanner(project.getDemandProfileFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            double weight = filein.nextDouble();
            int start = filein.nextInt();
            int duration = filein.nextInt();
            
            filein.nextLine();
            
            output.put(id, new AST(id, start, duration, weight));
        }
        
        filein.close();

        output.normalizeWeights();
        
        return output;
    }
    
    public void createDynamicOD(SAVProject project) throws IOException
    {
        DemandProfile profile = readDemandProfile(project);
        
        Scanner filein = new Scanner(project.getStaticODFile());
        
        if(!filein.hasNextInt())
        {
            filein.close();
            
            return;
        }
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDynamicODFile()), true);
        
        fileout.println("id\ttype\torigin\tdest\tast\tdemand");
        filein.nextLine();
        
        int new_id = 1;
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            double demand = filein.nextDouble();
            
            filein.nextLine();
            
            for(int t : profile.keySet())
            {
                AST ast = profile.get(t);
                
                fileout.println((new_id++)+"\t"+type+"\t"+origin+"\t"+dest+"\t"+ast.getId() + "\t" + demand * ast.getWeight());
            }
        }
        
        filein.close();
        fileout.close();
    }
    
    public static String getTripsFileHeader()
    {
        return "id\ttype\torigin\tdest\tdtime";
    }
    
    public static String getDemandProfileFileHeader()
    {
        return "id\tweight\tstart\tduration";
    }
    
    public static String getDynamicODFileHeader()
    {
        return "id\ttype\torigin\tdestination\tast\tdemand";
    }
    
    public static String getStaticODFileHeader()
    {
        return "id\ttype\torigin\tdestination\tdemand";
    }
  
    public static String getFleetFileHeader()
    {
        return "id\torigin\tcapacity";
    }
    
    public void createFleet(SAVProject project, int total) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getFleetFile()), true);
        
        int new_id = 1;
        
        for(int id : zones.keySet())
        {
            Zone z = zones.get(id);
            
            if(z instanceof SAVOrigin)
            {
                SAVOrigin o = (SAVOrigin)z;
                
                int num = (int)Math.round((double)total * o.getProductions() / total_productions);
                
                for(int i = 0; i < num; i++)
                {
                    fileout.println((new_id++)+"\t"+id+"\t"+4);
                }
            }
        }
        fileout.close();
        
    }
    public void readFleet(SAVProject project, SAVSimulator sim) throws IOException
    {
        Scanner filein = new Scanner(project.getFleetFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int origin_id = filein.nextInt();
            int capacity = filein.nextInt();
            
            Taxi taxi = new Taxi(id, (SAVOrigin)zones.get(origin_id), capacity);
            sim.addTaxi(taxi);
        }
        
        filein.close();
    }
}
