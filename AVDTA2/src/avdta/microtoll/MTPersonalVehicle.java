/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.microtoll;

import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.network.Path;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.wallet.Wallet;
import avdta.network.node.Node;
import avdta.vehicle.fuel.VehicleClass;
import avdta.network.link.Link;

/**
 *
 * @author ml26893
 */
public class MTPersonalVehicle extends PersonalVehicle
{
    private int net_enter_time;

    private double total_toll;
    
    public MTPersonalVehicle(int id, Node origin, Node dest, int dtime, double vot)
    {
        super(id, origin, dest, dtime, vot, Wallet.EMPTY, VehicleClass.icv, DriverType.AV);

        total_toll = 0;
    }
    
    public MTPersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, VehicleClass vehClass, DriverType driver)
    {
        super(id, origin, dest, dtime, vot, Wallet.EMPTY, vehClass, driver);

        total_toll = 0;
    }
    
    public double getTotalToll()
    {
        return total_toll;
    }
    
    
    public void exited()
    {
        super.exited();
    }
    
    // override because enteredLink will reset net_enter_time
    public int getNetEnterTime()
    {
        return net_enter_time;
    }
    
    public void entered()
    {
        net_enter_time = Simulator.time;
        super.entered();
    }
    
    public boolean checkInvalidPath(Path p)
    {
        return p == null || p.size() == 0 || p.getDest() != getDest();
    }
}
