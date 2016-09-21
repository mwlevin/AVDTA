/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.CTMLink;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.cell.LinkCell;
import avdta.network.link.Link;
import avdta.network.link.Link;
import avdta.vehicle.Vehicle;

/**
 * This type of {@link Cell} appears only at the end of a {@link CTMLink}. It is overridden to calculate the dynamic lane reversal policy.
 * @author Michael
 */
public class EndCell extends LinkCell
{
    /**
     * Constructs this cell as part of the specified link with the specified previous {@link Cell}
     * @param link the link this cell is part of
     * @param prev the {@link Cell} that directly precedes this {@link EndCell}
     */
    public EndCell(Cell prev, CTMLink link)
    {
        super(prev, link);
    }
    
    /**
     * Adds the {@link Vehicle} to the link (calls {@link LinkCell#addVehicle(avdta.vehicle.Vehicle)}) and updates the upstream sending flow for the link ({@link DLRCTMLink#addToUsSendingFlow()}) if it is a {@link DLRCTMLink}.
     * @param v the {@link Vehicle} to be added
     */
    public void addVehicle(Vehicle v)
    {
        Link j = v.getNextLink();
        
        if(j != null && j instanceof DLRCTMLink)
        {
            ((DLRCTMLink)j).addToUsSendingFlow();
        }
        super.addVehicle(v);
    }
}
