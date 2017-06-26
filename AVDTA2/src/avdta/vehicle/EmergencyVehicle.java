/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.dta.ReadDTANetwork;
import avdta.network.node.Node;
import avdta.vehicle.fuel.VehicleClass;
import avdta.vehicle.wallet.Wallet;

/**
 *
 * @author ml26893
 */
public class EmergencyVehicle extends Vehicle
{
    private Node origin, dest;
    private int deptime;
    
    public EmergencyVehicle(int id, Node origin, Node dest, int deptime)
    {
        super(id, Wallet.EMPTY, VehicleClass.icv, DriverType.AV);
        this.origin = origin;
        this.dest = dest;
        this.deptime = deptime;
    }
    
    public int getType()
    {
        return ReadDTANetwork.EMERGENCY_VEHICLE + getDriver().getType() + getVehClass().getType();
    }
    
    public Node getOrigin()
    {
        return origin;
    }
    
    public Node getDest()
    {
        return dest;
    }
    
    public int getDepTime()
    {
        return deptime;
    }
}
