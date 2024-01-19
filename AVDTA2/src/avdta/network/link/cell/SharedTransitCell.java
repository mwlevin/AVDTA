/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.Network;
import avdta.network.link.CTMLink;
import avdta.network.link.Link;
import avdta.network.link.SharedTransitCTMLink;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a cell in the cell transmission model of a dynamic transit lane link ({@link SharedTransitCTMLink}). 
 * It stores {@link Vehicle}s currently in the cell, and determines sending and receiving flows.
 * This class is abstract; see {@link SharedTransitLinkCell}.
 * Cells at the start and ends of links are slightly different; see {@link SharedTransitStartCell} and {@link SharedTransitEndCell}
 * 
 * @author Michael
 */
public abstract class SharedTransitCell extends Cell
{
    private Cell transitLane;
    
    /**
     * Constructs this {@link SharedTransitCell} as part of the specified {@link SharedTransitCTMLink}.
     * @param link the link this {@link SharedTransitCell} is part of
     */
    public SharedTransitCell(SharedTransitCTMLink link)
    {
        super(link);
        
    }
    
    /**
     * Returns the parallel, associated transit lane cell.
     * @return the associated transit lane cell
     */
    public Cell getTransitLane()
    {
        return transitLane;
    }
    
    /**
     * Updates the parallel, associated transit lane cell.
     * @param cell the new associated transit lane cell
     */
    public void setTransitLane(Cell cell)
    {
        transitLane = cell;
    }
    
    /**
     * Updates the number of lanes available to this cell based on transit lane occupancy.
     */
    public void updateLanes()
    {
        if(transitLane.getOccupancy() == 0)
        {
            setNumLanes(getLink().getNumLanes()+1);
        }
        else
        {
            setNumLanes(getLink().getNumLanes());
        }
    }
    
    /**
     * Returns the jam density, which is based on the minimum number of lanes this cell can have.
     * @return the jam density (veh/mi)
     */
    public double getJamD()
    {
        CTMLink link = getLink();
        return link.getCellJamdPerLane() * link.getNumLanes();
    }
}
