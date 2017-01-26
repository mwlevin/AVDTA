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
    
    public Iterator<Cell> iterator(Cell inc)
    {
        return connected.get(inc).keySet().iterator();
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
    
    public int getY(Cell i, Cell j)
    {
        Tuple tuple = makeTuple(i, j);
        
        return tuple.y;
    }
    
    public void addY(Cell i, Cell j)
    {
        Tuple tuple = makeTuple(i, j);
        tuple.y++;
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
                tuple.congested = false;
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
