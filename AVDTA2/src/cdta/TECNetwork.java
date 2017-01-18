/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

import cdta.cell.Cell;
import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.ConflictRegion;
import avdta.network.node.Intersection;
import avdta.network.node.PriorityTBR;
import cdta.cell.Connector;
import cdta.cell.IntersectionConnector;
import cdta.cell.SameCellConnector;
import cdta.cell.TECConflictRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author micha
 */
public class TECNetwork 
{
    private Map<Integer, TECConnector> origins;
    private Set<TECLink> links;
    private int T;
    
    /**
     * Note that links that are not CTMLinks or CentroidConnectors will be ignored.
     * @param sim the {@link Simulator}
     */
    public TECNetwork(Simulator sim)
    {
        // construct TECLinks
        links = new HashSet<TECLink>();
        origins = new HashMap<Integer, TECConnector>();
        
        for(Link l : sim.getLinks())
        {
            if(l instanceof CentroidConnector)
            {
                TECConnector link = new TECConnector((CentroidConnector)l);
                links.add(link);
                if(link.isOrigin())
                {
                    origins.put(link.getZoneId(), link);
                }
            }
            else
            {
                links.add(new TECLink(l));
            }
        }
        
        //T = Simulator.duration / Simulator.dt;
        T = 3600*4/Simulator.dt;
        
        for(TECLink link : links)
        {
            link.createCells(T);
        }
        
        Map<Integer, Link> linksmap = sim.createLinkIdsMap();
        
        Map<Integer, TECLink> tecmap = new HashMap<Integer, TECLink>();
        
        for(TECLink link : links)
        {
            tecmap.put(link.getId(), link);
        }
        
        for(TECLink tec: links)
        {
            Link link = linksmap.get(tec.getId());
            
            if(link.getDest() instanceof Intersection)
            {
                PriorityTBR tbr = new PriorityTBR((Intersection)link.getDest());
                tbr.initialize();
                
                for(int t = 0; t < T-1; t++)
                {
                    IntersectionConnector inter = new IntersectionConnector(tbr);
                    
                    // construct this outside to avoid storing a list inside
                    List<TECConflictRegion> conflicts = new ArrayList<TECConflictRegion>();
        
                    for(ConflictRegion cr : tbr.getConflicts())
                    {
                        conflicts.add(new TECConflictRegion(cr.getId()));
                    }
                    
                    tec.getLastCell(t).addOutgoing(inter);
                
                    for(Link o : link.getDest().getOutgoing())
                    {
                        TECLink tec2 = tecmap.get(o.getId());

                        inter.addConnection(tec, tec2, link, o, t, tbr, conflicts); 
                    }
                }
            }
        }
    }
    
    public void initializeConnectivity()
    {
        for(TECLink l : links)
        {
            for(int c = 0; c < l.getNumCells(); c++)
            {
                for(int t = 0; t < T; t++)
                {
                    Cell cell = l.getCell(c, t);
                    cell.setN(0);
                    
                    for(Connector edge : cell.getOutgoing())
                    {
                        edge.initConnectivity();
                        
                        if((edge instanceof SameCellConnector) && c == l.getNumCells()-1)
                        {
                            edge.setCongestionConnectivity(cell, cell, false);
                        }
                    }
                }
            }
        }
    }
    
}
