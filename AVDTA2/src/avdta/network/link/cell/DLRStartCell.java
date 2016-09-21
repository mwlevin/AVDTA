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
  * This type of {@link Cell} appears only at the start of a {@link DLRCTMLink}
 * @author Michael
 */
public class DLRStartCell extends DLRCell
{
    /**
     * Constructs this cell as part of the specified link
     * @param link the link this cell is part of
     */
    public DLRStartCell(DLRCTMLink link)
    {
        super(link);
    }
    
    /**
     * Executes one time step of simulation. Nothing to be done.
     */
    public void step(){}
    
    /**
     * Add a {@link Vehicle} to this {@link Cell} for the next time step. The {@link Vehicle} will not be part of this {@link Cell} in the current time step.
     * Also updates the position of {@link Vehicle}.
     * @param v the {@link Vehicle} to be added.
     */
    public void addVehicle(Vehicle v)
    {
        super.addVehicle(v);
        v.updatePosition(this);
    }
}
