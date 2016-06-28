/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.CTMLink;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.Link;
import avdta.network.link.SharedTransitCTMLink;
import avdta.vehicle.Vehicle;

/**
 *
 * @author micha
 */
public class SharedTransitEndCell extends SharedTransitLinkCell
{
    public SharedTransitEndCell(SharedTransitCell prev, SharedTransitCTMLink link)
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
