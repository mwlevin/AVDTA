/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.link.Link;
import avdta.vehicle.Vehicle;

/**
 *
 * @author Michael
 */
public class EndCell extends Cell
{
    public EndCell(Cell prev, CTMLink link)
    {
        super(prev, link);
    }
    
    public void addVehicle(Vehicle v)
    {
        Link j = v.getNextLink();
        
        if(j != null && j instanceof CTMLink)
        {
            ((CTMLink)j).addToUsSendingFlow();
        }
        super.addVehicle(v);
    }
}
