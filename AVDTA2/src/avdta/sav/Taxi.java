/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Simulator;
import avdta.vehicle.VehicleClass;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class Taxi extends Vehicle
{
    private List<Traveler> passengers;
    
    private SAVOrigin startLocation, location;
    
    public static final int DELAY_ENTER = 60;
    public static final int DELAY_EXIT = 60;
    
    
    public int delay;
    
    public int eta;
    
    public int park_time;
    
    public double total_distance, empty_distance;
    
    public boolean tempTaxi = false;
    
    public Taxi(int id, SAVOrigin startLocation)
    {
        super(id);
        setDriver(DriverType.AV);
        passengers = new ArrayList<Traveler>();
        this.startLocation = startLocation;
        this.location = startLocation;
       
        total_distance = 0;
        empty_distance = 0;
        
        
        setEfficiency(1);
    }
    
    public int getCapacity()
    {
        return 4;
    }
    
    public SAVOrigin getLocation()
    {
        return location;
    }
    
    public void setLocation(SAVOrigin origin)
    {
        park_time = Simulator.time;
        location = origin;
    }
    
    public SAVOrigin getStartLocation()
    {
        return startLocation;
    }
    
    public int getType()
    {
        return TAXI;
    }
    
    public void reset()
    {
        super.reset();
        passengers.clear();
        
        total_distance = 0;
    }
    
    public double getMPG()
    {
        return total_distance / (getTotalEnergy() / VehicleClass.E_PER_GALLON);
    }
    
    
    public void addPassenger(Traveler person)
    {
        if(!passengers.contains(person))
        {
            passengers.add(person);
        }
    }
    
    public List<Traveler> getPassengers()
    {
        return passengers;
    }
    
    public int getNumPassengers()
    {
        return passengers.size();
    }
    
    public boolean isEmpty()
    {
        return getNumPassengers() == 0;
    }
}
