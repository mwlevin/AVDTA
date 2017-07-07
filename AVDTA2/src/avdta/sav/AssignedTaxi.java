/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Path;
import avdta.traveler.Traveler;
import avdta.vehicle.DriverType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ml26893
 */
public class AssignedTaxi extends Taxi
{
    private List<SAVTraveler> travelers;
    private List<Path> segments;
    public int traveler_idx;
    private int segment_idx;
    
    public AssignedTaxi(int id, SAVOrigin startLocation, int capacity)
    {
        super(id, startLocation, capacity);
        
        travelers = new ArrayList<SAVTraveler>();
        segments = new ArrayList<>();
    }
    
    public AssignedTaxi(int id, SAVOrigin startLocation, SAVOrigin location, int capacity, double dropTime, int ttt, List<SAVTraveler> passengers, int delay, int eta, int park_time, double total_distance, double empty_distance){
        super(id, startLocation, capacity, location, dropTime, ttt, passengers);
        setDriver(DriverType.AV);
        this.delay = delay;
        this.eta = eta;
        this.park_time = park_time;
        this.total_distance = total_distance;
        this.empty_distance = empty_distance;
        setEfficiency(1);
    }
    
    public void addSegment(Path path)
    {
        segments.add(path);
    }
    
    public void addPassenger(SAVTraveler person)
    {
        super.addPassenger(person);
        traveler_idx++;
    }
    
    public void entered()
    {
        super.entered();
        segment_idx++;
        
        
        if(segments.size() > segment_idx && segments.get(segment_idx).isEmpty())
        {
            segment_idx++;
        }
    }
    
    public List<Path> getSegments()
    {
        return segments;
    }
    
    public void assignTraveler(SAVTraveler t, Path path)
    {
        travelers.add(t);
        segments.add(path);
        //t.setAssignedTaxi(this);
    }
    public List<SAVTraveler> getTravelers()
    {
        return travelers;
    }
    
    // do not call this during simulation
    public void assignTaxi(List<SAVTraveler> travelers, List<Path> segments)
    {
        this.travelers = travelers;
        this.segments = segments;
        
        /*
        for(SAVTraveler t : travelers)
        {
            t.setAssignedTaxi(this);
        }
        */
        traveler_idx = 0;
        segment_idx = 0;
    }
    
    public Path getNextSegment()
    {
        if(segment_idx < segments.size())
        {
            return segments.get(segment_idx);
        }
        else
        {
            return null;
        }
    }
    public SAVTraveler getNextTraveler()
    {
        if(traveler_idx < travelers.size())
        {
            return travelers.get(traveler_idx);
        }
        else
        {
            return null;
        }
    }
    
    
}
