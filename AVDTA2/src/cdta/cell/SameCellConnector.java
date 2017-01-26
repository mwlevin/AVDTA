/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import java.util.Iterator;

/**
 *
 * @author micha
 */
public class SameCellConnector extends Connector
{
    private boolean reservation, congestion;

    private Cell j;
    
    public SameCellConnector(Cell i, Cell j)
    {
        this.j = j;
    }
    
    public Iterator<Cell> iterator(Cell inc)
    {
        return new SingleIterator();
    }

    public boolean validate()
    {
        return true;
    }
    
    public int sumY()
    {
        return 0;
    }
    
    public int sumYIn(Cell i)
    {
        return 0;
    }
    
    public int sumYOut(Cell j)
    {
        return 0;
    }
    
    public void initConnectivity()
    {
        reservation = true;
        congestion = false;
    }
    
    public boolean isConnected(Cell i, Cell j)
    {
        return reservation && congestion;
    }
    
    public int getY(Cell i, Cell j)
    {
        return 0;
    }
    
    public void addY(Cell i, Cell j)
    {
        // do nothing
    }
    
    public void setReservationConnectivity(Cell i, Cell j, boolean connect)
    {
        reservation = connect;
    }
    
    public void setReservationConnectivity(boolean connect)
    {
        reservation = connect;
    }
    
    public void setCongestionConnectivity(Cell i, Cell j, boolean connect)
    {
        congestion = connect;
    }
    
    public void setCongestionConnectivity(boolean connect)
    {
        congestion = connect;
    }
    
    class SingleIterator implements Iterator<Cell>
    {
        private boolean hasNext;
        
        public SingleIterator()
        {
            hasNext = true;
        }
        
        public boolean hasNext()
        {
            return hasNext;
        }
        
        public void remove(){}
        
        public Cell next()
        {
            if(hasNext)
            {
                hasNext = false;
                return j;
            }
            else
            {
                return null;
            }
        }
        
    }

}
