/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Path;
import avdta.traveler.Traveler;
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
    private int traveler_idx;
    private int segment_idx;
    
    public AssignedTaxi(int id, SAVOrigin startLocation, int capacity)
    {
        super(id, startLocation, capacity);
        
        travelers = new ArrayList<SAVTraveler>();
        segments = new ArrayList<>();
    }
    
    public void assignTraveler(SAVTraveler t, Path path)
    {
        travelers.add(t);
        segments.add(path);
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
