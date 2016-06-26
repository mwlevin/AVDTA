/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
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

/**
 *
 * @author Michael
 */
public class ReadSAVNetwork extends ReadNetwork
{

    public ReadSAVNetwork()
    {
   
    }
    
    public void readTravelers(SAVSimulator sim) throws IOException
    {
        readTravelers(sim, new File("data/"+sim.getName()+"/demand.txt"));
    }
    
    public void readTravelers(SAVSimulator sim, double scale) throws IOException
    {
        readTravelers(sim, new File("data/"+sim.getName()+"/demand.txt"), scale);
    }
    
    public void readTravelers(SAVSimulator sim, File demandfile) throws IOException
    {
        readTravelers(sim, demandfile, 1);
    }
    
    public void readTravelers(SAVSimulator sim, File demandfile, double scale) throws IOException
    {
        Random rand = new Random(9000);
        
        List<Traveler> travelers = new ArrayList<Traveler>();
        
        Scanner filein = new Scanner(demandfile);
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int origin_id = filein.nextInt();
            int dest_id = filein.nextInt();
            
            filein.next();
            int dtime = filein.nextInt();
            int type = filein.nextInt();
         
            if(type != 501 && rand.nextDouble() <= scale)
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

        System.out.println(travelers.size());
        
        sim.setTravelers(travelers);
    }
    

    
    public void readNetwork(Simulator sim, int linktype) throws IOException
    {
        super.readNetwork(sim, linktype);
        
        replaceZones(sim);
        
        linkZones(sim);
        createMissingZones(sim);
    }
    
    public void replaceZones(Simulator sim) throws IOException
    {
        List<Node> nodes = sim.getNodes();
        
        ArrayList<Node> nodes2 = new ArrayList<Node>();
        
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
        
        sim.setNodes(nodes2);
    }
    
    /**
     * After linking zones, if some origins/destinations lack a counterpart, this creates the missing origins/destinations as well as their centroid connectors.
     * @param sim 
     */
    public void createMissingZones(Simulator sim)
    {
        List<Node> nodes = sim.getNodes();
        List<Link> links = sim.getLinks();
        
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
    
}
