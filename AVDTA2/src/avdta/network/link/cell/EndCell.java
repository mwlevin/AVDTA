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
 *
 * @author Michael
 */
public class EndCell extends LinkCell
{
    public EndCell(Cell prev, CTMLink link)
    {
        super(prev, link);
    }
    
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
