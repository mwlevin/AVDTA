/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author micha
 */
public class SameCellConnector extends Connector
{
    private boolean reservation, congestion;

    private Cell i, j;
    
    public SameCellConnector(Cell i, Cell j)
    {
        this.j = j;
        this.i = i;
    }
    
    public void printConnectivity(Cell inc)
    {
        System.out.println(i+"\t"+j+"\t"+reservation+"\t"+congestion);
    }
    
    public Set<Cell> getOutgoing(Cell inc)
    {
        Set<Cell> output = new HashSet<Cell>();
        output.add(j);
        
        return output;
    }
    
    public Set<Cell> getIncoming(Cell out)
    {
        Set<Cell> output = new HashSet<Cell>();
        output.add(i);
        
        return output;
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
    
    

}
