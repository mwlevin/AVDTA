/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.network.node.Zone;
import avdta.vehicle.fuel.VehicleClass;
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
    private List<SAVTraveler> passengers;
    
    private SAVOrigin startLocation, location;
    
    public static final int DELAY_ENTER = 60;
    public static final int DELAY_EXIT = 60;
    
    
    public int delay;
    
    public int eta;
    
    public int park_time;
    
    public double total_distance, empty_distance;
    
    public boolean tempTaxi = false;
    
    
    private int ttt;
    
    private int capacity;
    
    public Taxi(int id, SAVOrigin startLocation, int capacity)
    {
        super(id);
        setDriver(DriverType.AV);
        passengers = new ArrayList<SAVTraveler>();
        this.startLocation = startLocation;
        this.location = startLocation;
       
        total_distance = 0;
        empty_distance = 0;
        
        this.capacity = capacity;
        
        setEfficiency(1);
    }
    
    public void enteredLink(Link l)
    {
        ttt += Simulator.time - enter_time;

        super.enteredLink(l);
    }
    
    public int getTT()
    {
        return ttt;
    }
    
    public boolean isTransit()
    {
        return true;
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
        return ReadSAVNetwork.TAXI + getDriver().getType() + getVehClass().getType();
    }
    
    public void reset()
    {
        super.reset();
        passengers.clear();
        
        total_distance = 0;
        ttt = 0;
    }
    
    public double getMPG()
    {
        return total_distance / (getTotalEnergy() / VehicleClass.E_PER_GALLON);
    }
    
    
    public void addPassenger(SAVTraveler person)
    {
        if(!passengers.contains(person))
        {
            passengers.add(person);
        }
    }
    
    public void setPath(Path p)
    {
        Path curr = getPath();
        
        if(curr != null)
        {
            ((SAVOrigin)((Zone)curr.getDest()).getLinkedZone()).removeEnrouteTaxi(this);
        }
        
        super.setPath(p);
        

        if(p != null)
        {
            eta = (int)(p.getAvgCost(Simulator.time, 1.0, TravelCost.dnlTime)) + delay;
            ((SAVOrigin)((Zone)p.getDest()).getLinkedZone()).addEnrouteTaxi(this);
        }
    }
    
    public List<SAVTraveler> getPassengers()
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
