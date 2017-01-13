/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import avdta.network.Simulator;
import avdta.network.link.Link;

/**
 *
 * @author micha
 */
public class TECLink 
{
    private int capacity, jamd; 
    private double meso_delta, length;
    
    private int numCells;
    
    private Cell[][] cells;
    
    public TECLink(Link link)
    {
        length = link.getFFSpeed() * Simulator.dt / 3600.0;
        numCells = (int)Math.max(2, Math.round(link.getLength() / length));
        capacity =(int)Math.round(link.getCapacity() * Simulator.dt/3600.0);
        jamd = (int)Math.round(link.getJamDensity() * length);
        
        meso_delta = link.getWaveSpeed() / link.getFFSpeed();
    }
    
    public int getCellCapacity()
    {
        return capacity;
    }
    
    public int getCellJamD()
    {
        return jamd;
    }
    
    public double getCellLength()
    {
        return length;
    }
    
    public double getMesoDelta()
    {
        return meso_delta;
    }
}
