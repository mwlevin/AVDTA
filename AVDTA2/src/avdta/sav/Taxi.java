/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.network.node.Node;
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
    
    public static final int DELAY_ENTER = 30;
    public static final int DELAY_EXIT = 30;
    
    
    public int delay;
    
    public int eta;
    
    public int park_time;
    
    public double total_distance, empty_distance;
    
    public boolean tempTaxi = false;
    
    
    private int ttt;
    
    private int capacity;
    
    private double dropTime;
    
    public Taxi(int id, SAVOrigin startLocation, int capacity)
    {
        super(id);
        setDriver(DriverType.AV);
        passengers = new ArrayList<SAVTraveler>();
        this.startLocation = startLocation;
        this.location = startLocation;
       
        total_distance = 0;
        empty_distance = 0;
        dropTime = 0;
        
        this.capacity = capacity;
        
        setEfficiency(1);
    }
    
    public Taxi(int id, SAVOrigin startLocation, SAVOrigin location, int capacity, double dropTime, int ttt, List<SAVTraveler> passengers, int delay, int eta, int park_time, double total_distance, double empty_distance){
        super(id);
        setDriver(DriverType.AV);
        this.startLocation = startLocation;
        this.location = location;
        this.capacity = capacity;
        this.dropTime = dropTime;
        this.ttt = ttt;
        this.passengers = passengers;
        this.delay = delay;
        this.eta = eta;
        this.park_time = park_time;
        this.total_distance = total_distance;
        this.empty_distance = empty_distance;
        setEfficiency(1);
    }
    
    public SAVZone getOrigin()
    {
        return getLocation();
    }
    
    public Node getDest()
    {
        Path curr = getPath();
        
        if(curr != null)
        {
            return curr.getDest();
        }
        else
        {
            return null;
        }
    }
    
    public Node getCurrentLocation(){
        if(!passengers.isEmpty()){
            int size = passengers.size();
            SAVDest dest = null != passengers.get(size)? passengers.get(size).getDest() : null;
            return null != dest ? dest.getLinkedZone():null;
        }
        else{
            return getLocation();
        }
    }
    
    public int getDepTime()
    {
        return Simulator.time + delay;
    }
    
    public void exited()
    {
        super.exited();
        
        ttt += super.getTT();
    }
    
    public void enteredLink(Link l)
    {
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
        
        if(curr != null && !curr.isEmpty())
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

    public double getDropTime() {
        return dropTime;
    }

    public void setDropTime(double dropTime) {
        this.dropTime = dropTime;
    }
}
