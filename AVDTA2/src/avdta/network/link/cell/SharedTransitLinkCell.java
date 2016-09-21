/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.CTMLink;
import avdta.network.link.SharedTransitCTMLink;
import avdta.vehicle.Vehicle;
import java.util.Iterator;

/**
 * This is a regular link cell, and appears between the start and end cells on a {@link SharedTransitCTMLink}.
 * @author Michael
 */
public class SharedTransitLinkCell extends SharedTransitCell
{
    private SharedTransitCell prev;
    
    /**
     * Constructs this {@link LinkCell} as part of the specified link with the specified previous {@link Cell}
     * @param link the link this cell is part of
     * @param prev the {@link Cell} that directly precedes this {@link EndCell}
     */
    public SharedTransitLinkCell(SharedTransitCell prev, SharedTransitCTMLink link)
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
