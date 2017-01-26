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
import cdta.cell.SinkCell;
import cdta.cell.SourceCell;
import cdta.cell.StartCell;
import cdta.cell.TECConflictRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 *
 * @author micha
 */
public class TECNetwork 
{
    private Map<Integer, TECConnector> zones;
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
        zones = new HashMap<Integer, TECConnector>();
        
        for(Link l : sim.getLinks())
        {
            if(l instanceof CentroidConnector)
            {
                TECConnector link = new TECConnector((CentroidConnector)l);
                links.add(link);
                if(link.isOrigin())
                {
                    zones.put(link.getZoneId(), link);
                }
            }
            else
            {
                links.add(new TECLink(l));
            }
        }
        
        
        //T = Simulator.duration / Simulator.dt;
        T = 3600/Simulator.dt;
        
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
                    
                    
                    tec.getLastCell(t).setNextCellConnector(inter);
                
                    for(Link o : link.getDest().getOutgoing())
                    {
                        TECLink tec2 = tecmap.get(o.getId());

                        inter.addConnection(tec, tec2, link, o, t, tbr, conflicts); 
                    }
                }
            }
        }
    }
    
    public boolean validate()
    {
        for(TECLink link : links)
        {
            for(int c = 0; c < link.getNumCells(); c++)
            {
                for(int t = 0; t < T; t++)
                {
                    Cell cell = link.getCell(c, t);
                    
                    if(cell.getN() > cell.getJamD())
                    {
                        System.err.println("n > jam density");
                        return false;
                    }
                    
                }
                
                for(int t = 0; t < T-1; t++)
                {
                    Cell cell = link.getCell(c, t);
                    
                    double y_tot = 0;
                    
                    if(!(cell instanceof SinkCell))
                    {
                        y_tot = cell.getNextCellConnector().sumY();
                        
                        if(!cell.getNextCellConnector().validate())
                        {
                            System.err.println("Connector issue");
                            return false;
                        }
                    }
                    
                    if(!cell.getSameCellConnector().validate())
                    {
                        System.err.println("Connector issue");
                        return false;
                    }
                    
                    
                    
  
                    
                    if(y_tot > cell.getSendingFlow())
                    {
                        System.err.println("y > sending flow");
                        return false;
                    }
                }
            }
        }
        
        
        
        return true;
    }
    
    public void initializeConnectivity()
    {
        for(TECLink l : links)
        {
            for(int c = 0; c < l.getNumCells(); c++)
            {
                for(int t = 0; t < T-1; t++)
                {
                    Cell cell = l.getCell(c, t);
                    cell.setN(0);
                    
                    cell.getSameCellConnector().initConnectivity();
                    
                    if(!(cell instanceof SinkCell))
                    {
                        cell.getNextCellConnector().initConnectivity();
                    }
                    
                    
                    
                    if(c == l.getNumCells()-1)
                    {
                        cell.getSameCellConnector().setCongestionConnectivity(cell, cell, true);
                    }
                    
                }
            }
        }
    }
    
    public void reserve(Trajectory path)
    {
        // update n and y
        for(int x = 0; x < path.size()-1; x++)
        {
            Cell i = path.get(x);
            Cell j = path.get(x+1);
            
            i.addN();
            
            if(i.getId() != j.getId())
            {
                i.getNextCellConnector().addY(i, j);
            }
        }
        path.get(path.size()-1).addN();
        
        // update connectivity
        
        // congestion connectivity
        for(Cell c : path)
        {
            int t = c.getT();
            
            if(c.getN() +1 > c.getCapacity())
            {

                if(t > 0)
                {
                    c.getLink().getCell(c.getId(), t-1).getSameCellConnector().setReservationConnectivity(false);
                }
                c.getSameCellConnector().setCongestionConnectivity(true);
            }
            
            // receiving flow check
            if(c.getId() > 0)
            {
                Cell inc = c.getLink().getCell(c.getId()-1, t-1);
                
                if(inc.getNextCellConnector().getY(inc, c) > c.getReceivingFlow())
                {
                    c.getSameCellConnector().setCongestionConnectivity(true);
                }
            }
            /*
            else if(!(c instanceof SourceCell))
            {
                StartCell cell = (StartCell)c;
                IntersectionConnector edge = cell.getIncConnector();
                
                if(edge.sumYOut(cell) > cell.getReceivingFlow())
                {
                    for(Cell i : edge.getIncoming())
                    {
                        i.getSameCellConnector().setCongestionConnectivity(true);
                    }
                }
            }
            */
        }
        
        // reservation connectivity
        
    }
    
    public Trajectory shortestPath(int origin, int dest, int dtime)
    {
        Cell end = dijkstras(origin, dest, dtime);
        return trace(origin, end, dtime);
    }
    
    public Cell dijkstras(int origin, int dest, int dtime)
    {
        for(TECLink l : links)
        {
            for(int c = 0; c < l.getNumCells(); c++)
            {
                for(int t = 0; t < T; t++)
                {
                    Cell cell = l.getCell(c, t);
                    
                    cell.label = false;
                    cell.prev = null;
                    cell.added = false;
                }
            }
        }
        
        PriorityQueue<Cell> Q = new PriorityQueue<Cell>();
        
        Q.add(zones.get(origin).getCell(dtime));
        
        while(!Q.isEmpty())
        {
            Cell u = Q.remove();
            
            if(u.getZoneId() == dest)
            {
                return u;
            }
            
            if(u.getNextCellConnector() != null)
            {
                Iterator<Cell> iter = u.getNextCellConnector().iterator(u);
                
                while(iter.hasNext())
                {
                    Cell v = iter.next();
                    if(!v.added)
                    {
                        v.added = true;
                        Q.add(v);
                        v.label = true;
                        v.prev = u;
                    }
                }
            }
            
            if(u.getSameCellConnector() != null)
            {
                Iterator<Cell> iter = u.getSameCellConnector().iterator(u);
                
                while(iter.hasNext())
                {
                    Cell v = iter.next();
                    
                    if(!v.added)
                    {
                        v.added = true;
                        Q.add(v);
                        v.prev = u;
                        v.label = true;
                    }
                }
            }
        }
        
        return null;
    }
    
    public Trajectory trace(int origin, Cell dest, int dtime)
    {
        ArrayList<Cell> path = new ArrayList<Cell>();
        
        Cell curr = dest;

        while(curr.getZoneId() != origin)
        {
            path.add(curr);
            curr = curr.prev;
        }
        
        path.add(curr);
        
        Trajectory output = new Trajectory();
        
        for(int i = path.size()-1; i >= 0; i--)
        {
            output.add(path.get(i));
        }
        
        return output;
    }
    
}
