/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.CTMLink;
import avdta.vehicle.Vehicle;
import java.util.Iterator;

/**
 * This is a regular link cell, and appears between the start and end cells on a {@link CTMLink}.
 * @author Michael
 */
public class LinkCell extends Cell
{
    private Cell prev;
    
    /**
     * Constructs this {@link LinkCell} as part of the specified link with the specified previous {@link Cell}
     * @param link the link this cell is part of
     * @param prev the {@link Cell} that directly precedes this {@link EndCell}
     */
    public LinkCell(Cell prev, CTMLink link)
    {
        super(link);
        
        this.prev = prev;
    }
    
    /**
     * Executes one time step of simulation.
     * Moves vehicles from the previous cell to this cell and updates their position.
     */
    public void step()
    {
        double y = Math.min(prev.getNumSendingFlow(), getReceivingFlow());

        Iterator<Vehicle> iter = prev.curr.iterator();
        

        while(y >= 1)
        {
            y -= 1;

            Vehicle v = iter.next();
            addVehicle(v);
            iter.remove();

            v.updatePosition(this);

        }
        
    }
}
