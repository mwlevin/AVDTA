/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.dta.DTASimulator;
import static avdta.dta.ReadDTANetwork.getDemandFileHeader;
import avdta.dta.VehicleRecord;
import avdta.demand.AST;
import avdta.demand.DemandProfile;
import avdta.demand.ReadDemandNetwork;
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
 * This contains methods to read a SAV network, fleet, and demand from file.
 * To access, call {@link ReadSAVNetwork#readNetwork(avdta.project.SAVProject)}.
 * @author Michael
 */
public class ReadSAVNetwork extends ReadDemandNetwork
{
    public static final int TAXI = 200;
    
    public static final int TRAVELER = 600;
    

    /**
     * Creates a new {@link ReadSAVNetwork}
     */
    public ReadSAVNetwork()
    {
   
    }
    
    /**
     * Reads a {@link SAVSimulator} from the files specified by the project.
     * This replaces zones with {@link SAVOrigin} and {@link SAVDest} as appropriate.
     * This also links origin and destination zones.
     * Finally, it creates travelers and the SAV fleet.
     * @param project the project
     * @return the associated simulator
     * @throws IOException if a file is not found
     */
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
            Zone i = zones.get(id);
            
            for(int id2 : zones.keySet())
            {
                Zone j = zones.get(id2);
                
                if(i == j)
                {
                    continue;
                }
                else if(i.getCoordinate().equals(j.getCoordinate()))
                {
                    i.setLinkedZone(j);
                    j.setLinkedZone(i);
                }
            }
            
        }

    }
    
    /**
     * This creates a list of travelers from the demand file.
     * @param project the project
     * @return a list of travelers
     * @throws IOException if a file is not found
     */
    public List<Traveler> readTravelers(SAVProject project) throws IOException
    {
        List<Traveler> travelers = new ArrayList<Traveler>();
        
        Scanner filein = new Scanner(project.getDemandFile());
        
        filein.nextLine();
        
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int origin_id = filein.nextInt();
            int dest_id = filein.nextInt();
            int dtime = filein.nextInt();
            double vot = filein.nextDouble();
            filein.nextLine();
         
            if(type == TRAVELER)
            {
                try
                {
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
    

    
    /**
     * This method replaces zones with SAVOrigin or SAVDest, as appropriate
     * @param nodes the current set of nodes
     * @return the set of nodes with replaced zones
     */
    public Set<Node> replaceZones(Set<Node> nodes) throws IOException
    {
        Set<Node> nodes2 = new HashSet<Node>();
        
        
        for(Node n : nodes)
        {
            
            if(n instanceof Zone)
            {
                Zone z = (Zone)n;
                
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
                
                n.setIncoming(new HashSet<Link>());
                n.setOutgoing(new HashSet<Link>());
                
                nodes2.add(node);
                nodesmap.put(n.getId(), node);
                
                zones.remove(n.getId());
                zones.put(node.getId(), node);
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

    
  
    /**
     * Returns the header for the fleet file.
     * @return the header for the fleet file
     */
    public static String getFleetFileHeader()
    {
        return "id\torigin\tcapacity";
    }
    
    /**
     * This creates a fleet of the specified size, distributing them among centroids proportional to productions.
     * The output is in the project fleet file.
     * @param project the project
     * @param total the fleet size
     */
    public void createFleet(SAVProject project, int total) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getFleetFile()), true);
        
        fileout.println(getFleetFileHeader());
        
        int total_productions = project.getSimulator().getNumTrips();
        
        int new_id = 1;
        
        for(Node n : project.getSimulator().getNodes())
        {
            if(!(n instanceof Zone))
            {
                continue;
            }
            
            Zone z = (Zone)n;
            

            if(z instanceof SAVOrigin)
            {
                
                SAVOrigin o = (SAVOrigin)z;
                
                int num = (int)Math.round((double)total * o.getProductions() / total_productions);
                

                for(int i = 0; i < num; i++)
                {
                    fileout.println((new_id++)+"\t"+z.getId()+"\t"+4);
                }
            }
        }
        fileout.close();
        
    }
    
    /**
     * Read the fleet from the fleet file.
     * @param project the project
     * @param sim the {@link SAVSimulator} (its construction is in progress)
     * @throws IOException 
     */
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
    
    /**
     * This goes through the static_od, dynamic_od, and demand files, and changes the type to {@link ReadSAVNetwork#TRAVELER}.
     * Then, this calls {@link ReadDemandNetwork#prepareDemand(avdta.project.DemandProject, double)} on the modified trip table.
     */
    public void setTripsToTravelers(SAVProject project) throws IOException
    {
        ReadSAVNetwork read = new ReadSAVNetwork();
        Map<Integer, Double> proportion = new HashMap<Integer, Double>();
        
        proportion.put(TRAVELER, 1.0);
        read.changeDynamicType(project, proportion);
        read.changeStaticType(project, proportion);
        read.prepareDemand(project, 1.0);
    }
}
