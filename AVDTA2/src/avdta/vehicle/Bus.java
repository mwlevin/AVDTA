/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.vehicle.wallet.StaticWallet;
import avdta.vehicle.wallet.Wallet;
import avdta.dta.ReadDTANetwork;
import avdta.network.link.transit.BusLink;
import avdta.network.link.Link;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.link.SharedTransitCTMLink;
import avdta.vehicle.fuel.VehicleClass;
import avdta.vehicle.fuel.ICV;
import java.util.ArrayList;

/**
 * This vehicle travels along a fixed route, updating travel times on relevant {@link BusLink}s as it travels.
 * @author Michael
 */
public class Bus extends Vehicle
{
    public static final double VOT = 100;
    public static final Wallet wallet = new StaticWallet(Integer.MAX_VALUE);
    
    private ArrayList<BusLink> transitStops;
    
    private int stop_idx;
    private int stop_arr_time;
    private int dtime;
    
    private int route_id;
    
    /**
     * Constructs this {@link Bus} with the given parameters.
     * The {@link DriverType} is set to {@link DriverType#BUS_AV}.
     * The {@link VehicleClass} is set to {@link ICV}
     * @param id the id of this {@link Bus}
     * @param route_id the id of the route this {@link Bus} follows
     * @param dtime the departure time (s)
     * @param path the path followed by this {@link Bus}
     * @param transitStops the list of stops
     */
    public Bus(int id, int route_id, int dtime, Path path, ArrayList<BusLink> transitStops)
    {
        this(id, route_id, dtime, path, transitStops, VehicleClass.icv, DriverType.BUS_AV);
    }
    
    /**
     * Constructs this {@link Bus} with the given parameters.
     * @param id the id of this {@link Bus}
     * @param route_id the id of the route this {@link Bus} follows
     * @param dtime the departure time (s)
     * @param path the path followed by this {@link Bus}
     * @param transitStops the list of stops
     * @param vehClass the {@link VehicleClass} used for calculating energy consumption
     * @param driver the {@link DriverType}
     */
    public Bus(int id, int route_id, int dtime, Path path, ArrayList<BusLink> transitStops, VehicleClass vehClass, DriverType driver)
    {
        super(id, Bus.wallet, vehClass, driver);
        
        this.transitStops = transitStops;
        this.route_id = route_id;
        
        setPath(path);
        stop_idx = 0;
        this.dtime = dtime;
        stop_arr_time = dtime;
    }
    
    public int getDepTime()
    {
        return dtime;
    }
    
    /**
     * Returns whether this is a transit vehicle, which it is
     * @return true
     */
    public boolean isTransit()
    {
        return true;
    }
    
    /**
     * Returns the type code of this vehicle
     * @return {@link ReadDTANetwork#BUS} + vehicle class type + driver type
     */
    public int getType()
    {
        return ReadDTANetwork.BUS + getVehClass().getType() + getDriver().getType();
    }
    
    /**
     * Returns the route id
     * @return the route id
     */
    public int getRouteId()
    {
        return route_id;
    }
    
    /**
     * Resets this {@link Bus} to restart the simulation.
     */
    public void reset()
    {
        super.reset();
        stop_idx = 0;
        stop_arr_time = getDepTime();
    }
    
    /**
     * This is called when the vehicle enters a new link.
     * Calls {@link PersonalVehicle#enteredLink(avdta.network.link.Link)}.
     * Updates stop arrival times if applicable.
     * @param l the link entered
     */
    public void enteredLink(Link l)
    {
        super.enteredLink(l);
        
        if(stop_idx < transitStops.size() && l.getSource() == transitStops.get(stop_idx).getDest())
        {
            transitStops.get(stop_idx++).setTT(stop_arr_time, Simulator.time);

            stop_arr_time = Simulator.time;
        } 
        
        
                
        if(l instanceof SharedTransitCTMLink)
        {
            throw new RuntimeException("Ignoring transit lane on "+l.getId()+" of "+getPath()+" "+l);
        }
                
    }
    
    /**
     * This is called when the vehicle exits the network.
     * Calls {@link PersonalVehicle#exited()}.
     * Updates transit stop travel times if necessary.
     */
    public void exited()
    {
        super.exited();
        
        if(stop_idx < transitStops.size())
        {
            transitStops.get(stop_idx).setTT(stop_arr_time, Simulator.time);
        }
    }
}
