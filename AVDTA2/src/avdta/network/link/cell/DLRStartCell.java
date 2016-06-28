/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.DLRCTMLink;
import avdta.network.link.cell.DLRCell;
import avdta.vehicle.Vehicle;

/**
 *
 * @author micha
 */
public class DLRStartCell extends DLRCell
{
    public DLRStartCell(DLRCTMLink link)
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
