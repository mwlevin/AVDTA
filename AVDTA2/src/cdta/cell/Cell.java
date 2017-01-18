/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import avdta.network.Simulator;
import cdta.TECLink;

/**
 *
 * @author micha
 */
public class Cell 
{
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
        this.link = link;
        
        out = new Connector[0];
        
    }
    
    public int getSendingFlow()
    {
        return (int)Math.min(getCapacity(), n);
    }
    
    public void setN(int n)
    {
        this.n = n;
    }
    
    public int getN()
    {
        return n;
    }
    
    public void addN(int n)
    {
        this.n += n;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public Connector[] getOutgoing()
    {
        return out;
    }
    
    public void setOutgoing(Connector[] out)
    {
        this.out = out;
    }
    
    public void setOutgoing(Connector out)
    {
        setOutgoing(new Connector[]{out});
    }
    
    public void addOutgoing(Connector c)
    {
        Connector[] output = new Connector[out.length+1];
        
        for(int i = 0; i < out.length; i++)
        {
            output[i] = out[i];
        }
        
        output[output.length-1] = c;
        setOutgoing(output);
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
