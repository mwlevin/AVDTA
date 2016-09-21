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
    
    public SharedTransitCell(SharedTransitCTMLink link)
    {
        super(link);
        
    }
    
    public Cell getTransitLane()
    {
        return transitLane;
    }
    
    public void setTransitLane(Cell cell)
    {
        transitLane = cell;
    }
    
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
    
    public double getJamD()
    {
        CTMLink link = getLink();
        return link.getCellJamdPerLane() * link.getNumLanes();
    }
}
