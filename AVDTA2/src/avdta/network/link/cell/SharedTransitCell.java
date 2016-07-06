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
 *
 * @author micha
 */
public abstract class SharedTransitCell extends Cell
{
    private Cell transitLane;
    
    public SharedTransitCell(SharedTransitCTMLink link)
    {
        super(link);
        
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
