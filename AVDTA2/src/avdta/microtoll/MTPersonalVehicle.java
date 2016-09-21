/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.microtoll;

import avdta.network.Simulator;
import avdta.cost.TravelCost;
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
    
    private Path traveled;
    
    private double total_toll;
    
    public MTPersonalVehicle(int id, Node origin, Node dest, int dtime, double vot)
    {
        super(id, origin, dest, dtime, vot, Wallet.EMPTY, VehicleClass.icv, DriverType.AV);
        
        traveled = new Path();
        total_toll = 0;
    }
    
    public MTPersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, VehicleClass vehClass, DriverType driver)
    {
        super(id, origin, dest, dtime, vot, Wallet.EMPTY, vehClass, driver);
        
        traveled = new Path();
        total_toll = 0;
    }
    
    public double getTotalToll()
    {
        return total_toll;
    }
    
    public void enteredLink(Link l)
    {
        total_toll += l.getToll(Simulator.time);
        
        Simulator sim = Simulator.active;
        sim.dijkstras(l, sim.time, getVOT(), getDriver(), MTSimulator.costFunc);
        
        traveled.add(l);
        
        Path newPath = sim.trace(l.getDest(), getDest());
        newPath.add(0, l);
        setPath(newPath);
        super.enteredLink(l);

    }
    
    public void exited()
    {
        super.exited();
        
        setPath(traveled);
        traveled = null;
    }
    
    // override because enteredLink will reset net_enter_time
    public int getNetEnterTime()
    {
        return net_enter_time;
    }
    
    public void entered()
    {
        net_enter_time = Simulator.time;
    }
    
    public boolean checkInvalidPath(Path p)
    {
        return p == null || p.size() == 0 || p.getDest() != getDest();
    }
}
