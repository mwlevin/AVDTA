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
 *
 * @author micha
 */
public class DLRLinkCell extends DLRCell
{
    private DLRCell prev;
    
    public DLRLinkCell(DLRCell prev, DLRCTMLink link)
    {
        super(link);
        
        this.prev = prev;
    }
    
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
