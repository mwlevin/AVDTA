/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.DLRCTMLink;
import avdta.network.link.Link;
import avdta.network.link.cell.DLRCell;
import avdta.vehicle.Vehicle;

/**
 *
 * @author micha
 */
public class DLREndCell extends DLRLinkCell
{
    /**
     * Constructs this cell as part of the specified link with the specified previous cell
     * @param link the link this cell is part of
     * @param prev the {@link Cell} that directly precedes this {@link EndCell}
     */
    public DLREndCell(DLRCell prev, DLRCTMLink link)
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
