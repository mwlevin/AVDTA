/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.CTMLink;
import avdta.vehicle.Vehicle;

/**
 *
 * @author Michael
 */
public class StartCell extends Cell
{
    public StartCell(CTMLink link)
    {
        super(link);
    }
    
    public void step(){}
    
    public void addVehicle(Vehicle v)
    {
        super.addVehicle(v);
        v.updatePosition(this);
    }
}
