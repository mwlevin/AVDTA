/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.dta.ReadDTANetwork;
import avdta.network.link.BusLink;
import avdta.network.link.Link;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.vehicle.fuel.VehicleClass;
import java.util.ArrayList;

/**
 *
 * @author Michael
 */
public class Bus extends PersonalVehicle
{
    public static final double VOT = 100;
    public static final Wallet wallet = new StaticWallet(Integer.MAX_VALUE);
    
    private ArrayList<BusLink> transitStops;
    
    private int stop_idx;
    private int stop_arr_time;
    
    private int route_id;
    
    public Bus(int id, int route_id, int dtime, Path path, ArrayList<BusLink> transitStops)
    {
        super(id, path.getOrigin(), path.getDest(), dtime, Bus.VOT, Bus.wallet, path, VehicleClass.icv, DriverType.BUS_AV);
        
        this.transitStops = transitStops;
        this.route_id = route_id;
        
        stop_idx = 0;
        stop_arr_time = dtime;
    }
    
    public boolean isTransit()
    {
        return true;
    }
    
    public int getType()
    {
        return ReadDTANetwork.BUS + getVehClass().getType() + getDriver().getType();
    }
    
    public int getRouteId()
    {
        return route_id;
    }
    
    public void reset()
    {
        super.reset();
        stop_idx = 0;
        stop_arr_time = getDepTime();
    }
    
    public void enteredLink(Link l)
    {
        super.enteredLink(l);
        
        if(l.getSource() == transitStops.get(stop_idx).getDest())
        {
            if(stop_arr_time > Simulator.time)
            {
                System.out.println("check1 "+stop_arr_time+" "+Simulator.time+" "+getDepTime());
            }
            transitStops.get(stop_idx++).setTT(stop_arr_time, Simulator.time);

            stop_arr_time = Simulator.time;
        } 
    }
    
    public void exited()
    {
        super.exited();
        
        if(stop_arr_time > Simulator.time)
            {
                System.out.println("check2 "+stop_arr_time+" "+Simulator.time+" "+getDepTime());
            }
        
        transitStops.get(stop_idx).setTT(stop_arr_time, Simulator.time);
    }
}
