/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;


import avdta.network.link.Link;
import avdta.network.node.ConflictRegion;
import avdta.network.node.TBR;
import cdta.TECLink;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author micha
 */
public class IntersectionConnector extends Connector
{
    private Map<Cell, Map<Cell, Tuple>> connected;
    

    
    public IntersectionConnector(TBR inter)
    {
        connected = new HashMap<Cell, Map<Cell, Tuple>>();
        
        
    }
    
    public Set<Cell> getOutgoing(Cell inc)
    {
        if(!connected.containsKey(inc))
        {
            return new HashSet<Cell>();
        }
        return connected.get(inc).keySet();
    }
    
    public Set<Cell> getIncoming(Cell out)
    {
        Set<Cell> output = new HashSet<Cell>();
        
        for(Cell inc : connected.keySet())
        {
            if(connected.get(inc).containsKey(out))
            {
                output.add(inc);
            }
        }
        
        return output;
    }
    
    public Set<Cell> getIncoming()
    {
        return connected.keySet();
    }
    
    public boolean connects(Cell i, Cell j)
    {
        return connected.containsKey(i) && connected.get(i).containsKey(j);
    }
    
    public boolean validate()
    {
        for(Cell i : connected.keySet())
        {
            Map<Cell, Tuple> temp = connected.get(i);
            
            for(Cell j : temp.keySet())
            {
                for(TECConflictRegion cr : temp.get(j).conflicts)
                {
                    if(!cr.validate())
                    {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
    
    public boolean isConnected(Cell i, Cell j)
    {
        if(!connected.containsKey(i))
        {
            return false;
        }
        
        Map<Cell, Tuple> temp = connected.get(i);
        
        if(!temp.containsKey(j))
        {
            return false;
        }
        
        Tuple tuple = temp.get(j);
        
        return tuple.reservation && tuple.congested;
    }
    
    public void printConnectivity(Cell inc)
    {
        for(Cell j : connected.get(inc).keySet())
        {
            Tuple tuple = connected.get(inc).get(j);
            System.out.println(inc+"\t"+j+"\t"+tuple.reservation+"\t"+tuple.congested);
        }
    }
    
    public int getY(Cell i, Cell j)
    {
        Tuple tuple = makeTuple(i, j);
        
        return tuple.y;
    }
    
    public void checkConnectivity(Cell i, Cell j)
    {
        Tuple tuple = makeTuple(i, j);
        
        for(TECConflictRegion cr : tuple.conflicts)
        {
            removeConnectivity(cr);
        }
    }
    
    public void removeConnectivity(TECConflictRegion cr)
    {
        for(Cell i : connected.keySet())
        {
            Map<Cell, Tuple> temp = connected.get(i);
            
            for(Cell j : temp.keySet())
            {
                Tuple tuple = temp.get(j);
                
                if(contains(tuple.conflicts, cr) && !cr.canAddY(i, j))
                {
                    tuple.congested = false;
                }
            }
        }
    }
    
    private boolean contains(TECConflictRegion[] conflicts, TECConflictRegion cr)
    {
        for(TECConflictRegion c : conflicts)
        {
            if(c == cr)
            {
                return true;
            }
        }
        
        return false;
    }
    
    public void addY(Cell i, Cell j)
    {
        Tuple tuple = makeTuple(i, j);
        tuple.y ++;
        
        for(TECConflictRegion cr : tuple.conflicts)
        {
            cr.addY(i, j);
        }
    }
    
    private Tuple makeTuple(Cell i, Cell j)
    {
        Map<Cell, Tuple> temp;
        
        if(connected.containsKey(i))
        {
            temp = connected.get(i);
        }
        else
        {
            connected.put(i, temp = new HashMap<Cell, Tuple>());
        }
        
        Tuple output;
        
        if(temp.containsKey(j))
        {
            output = temp.get(j);
        }
        else
        {
            temp.put(j, output = new Tuple());
        }
        
        return output;
    }
    
    public void addConnection(TECLink i, TECLink j, Link i_real, Link j_real, int t, TBR inter, List<TECConflictRegion> allConflicts)
    {
        List<TECConflictRegion> conflicts = new ArrayList<TECConflictRegion>();
        
        for(ConflictRegion cr : inter.getConflicts(i_real, j_real))
        {
            conflicts.add(findCR(allConflicts, cr.getId()));
        }
        
        Tuple tuple = makeTuple(i.getLastCell(t), j.getFirstCell(t+1));
        tuple.setConflicts(conflicts);
        
        ((StartCell)j.getFirstCell(t+1)).setIncConnector(this);
        
    }
    
    private TECConflictRegion findCR(List<TECConflictRegion> allConflicts, int id)
    {
        for(TECConflictRegion c : allConflicts)
        {
            if(c.getId() == id)
            {
                return c;
            }
        }
        
        return null;
    }
    
    public int sumY()
    {
        int output = 0;
        
        for(Cell i : connected.keySet())
        {
            Map<Cell, Tuple> temp = connected.get(i);
            
            for(Cell j : temp.keySet())
            {
                output += temp.get(j).y;
            }
        }
       
        return output;
    }
    
    public int sumYIn(Cell i)
    {
        if(!connected.containsKey(i))
        {
            return 0;
        }
        
        int output = 0;
        
        Map<Cell, Tuple> temp = connected.get(i);
        
        for(Cell j : temp.keySet())
        {
            output += temp.get(j).y;
        }
        
        return output;
    }
    
    public int sumYOut(Cell j)
    {
        int output = 0;
        
        for(Cell i : connected.keySet())
        {
            Map<Cell, Tuple> temp = connected.get(i);
            
            if(temp.containsKey(j))
            {
                output += temp.get(j).y;
            }
        }
        return output;
    }
    
    public void setReservationConnectivity(Cell i, Cell j, boolean connect)
    {
        Tuple tuple = makeTuple(i, j);
        tuple.reservation = connect;
    }
    
    public void setCongestionConnectivity(Cell i, Cell j, boolean connect)
    {
        Tuple tuple = makeTuple(i, j);
        tuple.congested = connect;
    }
    
    public void initConnectivity()
    {
        for(Cell i : connected.keySet())
        {
            Map<Cell, Tuple> temp = connected.get(i);
            
            for(Cell j : temp.keySet())
            {
                Tuple tuple = temp.get(j);
                tuple.reservation = true;
                tuple.congested = i.getCapacity() > 0 && j.getCapacity() > 0;
            }
        }
    }
    
    private static class Tuple
    {
        public int y;
        public boolean reservation;
        public boolean congested;

        
        public TECConflictRegion[] conflicts;
        
        public Tuple()
        {
            
        }
        
        public void setConflicts(List<TECConflictRegion> list)
        {
            conflicts = new TECConflictRegion[list.size()];
            
            for(int i = 0; i < conflicts.length; i++)
            {
                conflicts[i] = list.get(i);
            }
        }
    }
}
