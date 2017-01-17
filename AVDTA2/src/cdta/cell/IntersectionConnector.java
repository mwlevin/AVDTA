/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;


import avdta.network.node.TBR;
import cdta.TECLink;
import java.util.HashMap;
import java.util.Map;

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
    
    public void addConnection(TECLink i, TECLink j, int t)
    {
        makeTuple(i.getLastCell(t), j.getFirstCell(t+1));
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
        
        //private ConflictRegion[] conflicts;
        
        public Tuple()
        {
            
        }
    }
}
