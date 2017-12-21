/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.DLRCTMLink;
import avdta.vehicle.Vehicle;

/**
 * This is an extension of {@link Cell} for dynamic lane reversal (see {@link DLRCTMLink}). It tracks an opposite cell on a parallel but opposite link.
 * It stores {@link Vehicle}s currently in the cell, and determines sending and receiving flows.
 * This class is abstract; see {@link DLRLinkCell}.
 * Cells at the start and ends of links are slightly different; see {@link DLRStartCell} and {@link DLREndCell}
 * @author Michael
 */
public abstract class DLRCell extends Cell
{
    private DLRCell opposite; 
    
    /**
     * Constructs this cell as part of the specified link.
     * @param link the link this cell is part of.
     */
    public DLRCell(DLRCTMLink link)
    {
        super(link);
    }
    
    /**
     * Sets the opposing cell on the parallel but opposite link (if one exists)
     * @param o the opposing cell
     */
    public void setOppositeCell(DLRCell o)
    {
        opposite = o;
    }
    
    /**
     * Returns the opposing cell on the parallel but opposite link (if one exists)
     * @return the opposing cell
     */
    public DLRCell getOpposite()
    {
        return opposite;
    }
    
    public boolean isValid(int numLanes)
    {
        return numLanes >= getMinLanes() && numLanes <= getMaxLanes();
    }
    /**
     * Returns the maximum number of lanes this cell can have for the next time step. 
     * This depends on the total lanes shared between this cell and its opposite.
     * @return the maximum number of lanes this cell can have for the next time step
     */
    public int getMaxLanes()
    {
        int total_road_lanes = getLink().getNumLanes();
        
        if(opposite != null)
        {
            total_road_lanes += opposite.getLink().getNumLanes();
        }
        
        if(curr.size() > 0)
        {
            return (int)Math.min(getNumLanes() + 1, total_road_lanes-1);
        }
        else
        {
            return total_road_lanes-1;
        }
    }
    
    /**
     * Returns the minimum number of lanes necessary for this time step, which depends on the jam density per lane and the occupancy.
     * @return the minimum number of lanes necessary for this time step
     */
    public int getMinLanes()
    {
        int numLanes = getNumLanes();
        
        if(curr.size() == 0)
        {
            return 1;
        }
        else if(curr.size() > getLink().getCellJamdPerLane() * (numLanes - 1))
        {
            return numLanes;
        }
        else
        {
            return numLanes - 1;
        }
    }
}
