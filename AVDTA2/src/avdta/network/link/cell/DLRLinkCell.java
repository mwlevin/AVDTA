/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.DLRCTMLink;
import avdta.network.link.cell.DLRCell;
import avdta.vehicle.Vehicle;
import java.util.Iterator;

/**
 * This is a regular cell for dynamic lane reversal links. (See {@link DLRCTMLink}.)
 * @author Michael
 */
public class DLRLinkCell extends DLRCell
{
    private DLRCell prev;
    
    /**
     * Constructs this cell as part of the specified link with the specified previous cell
     * @param link the link this cell is part of
     * @param prev the {@link Cell} that directly precedes this {@link EndCell}
     */
    public DLRLinkCell(DLRCell prev, DLRCTMLink link)
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
