/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.cdta.cell;

import avdta.network.Simulator;
import avdta.cdta.cell.TECLink;

/**
 *
 * @author micha
 */
public class Cell 
{
    private static int next_id = 0;
    private int id;
    
    private Connector[] out;
    private int t;
    
    private TECLink link;
    
    private int n;
    
    
    
    
    public Cell prev;
    public double label;
    
    public Cell(TECLink link, int t)
    {
        this.t = t;
        
        id = ++next_id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public Connector[] getOutgoing()
    {
        return out;
    }
    
    
    /**
     * Returns the length associated with this cell.
     * @return {@link CTMLink#getCellLength()}
     */
    public double getLength()
    {
        return link.getCellLength();
    }
    
    /**
     * Returns the jam density of this {@link Cell}. The jam density depends on the current number of lanes.
     * @return {@link CTMLink#getCellJamdPerLane()}*{@link Cell#getNumLanes()}
     */
    public int getJamD()
    {
        return link.getCellJamD();
    }
    
    /**
     * Returns the capacity of this {@link Cell}. The capacity depends on the current number of lanes.
     * @return {@link CTMLink#getCapacityPerLane()}*{@link Cell#getNumLanes()}
     */
    public int getCapacity()
    {
        return link.getCellCapacity();
    }
    
    public double getReceivingFlow()
    {
        return Math.min(getCapacity(), getMesoDelta() * (getJamD() - n));
    }
    
    public double getMesoDelta()
    {
        return link.getMesoDelta();
    }
}
