/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.dta.ReadDTANetwork;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.network.node.Node;
import avdta.vehicle.fuel.VehicleClass;
import avdta.vehicle.route.FixedPath;
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
        
        setPath(Simulator.active.findPath(this, TravelCost.ffTime));
    }
    
    public void exited()
    {
        super.exited();
        Simulator.active.getEmergencyVehicles().remove(this);
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
