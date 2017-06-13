/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav.dispatch;

import avdta.network.Path;
import avdta.network.Simulator;
import avdta.sav.AssignedTaxi;
import avdta.sav.SAVOrigin;
import avdta.sav.SAVTraveler;
import avdta.sav.Taxi;
import java.util.Iterator;

/**
 * This class assumes SAVs are assigned to fixed paths. They will travel along those paths when able, picking up passengers as assigned.
 * 
 * @author ml26893
 */
public class AssignedDispatch extends Dispatch
{
    public void updateShortestPaths()
    {
        // overridden, nothing to do here
    }
    
    public void travelerDeparted(Taxi taxi, SAVTraveler person)
    {
        // nothing to do
    }

    
    public void newTimestep()
    {
        
    }
    
    public void addFreeTaxi(Taxi t)
    {
        // nothing to do here
    }
    
    public void removeFreeTaxi(Taxi t)
    {
        // nothing to do here
    }
    
    public void taxiArrived(Taxi t, SAVOrigin node)
    {
        AssignedTaxi taxi = (AssignedTaxi)t;
        
        Path nextSegment = taxi.getNextSegment();
        if(nextSegment != null)
        {
            SAVTraveler nextTraveler = taxi.getNextTraveler();
            if(nextTraveler == null || nextTraveler.getOrigin() != node)
            {
                taxi.setPath(nextSegment);
                getSimulator().addDeparting(taxi);
            }
            else if(node.getWaitingTravelers().contains(nextTraveler))
            {
                getSimulator().addTravelerToTaxi(taxi, nextTraveler);
                taxi.setPath(nextSegment);
                getSimulator().addDeparting(taxi);
            }

        }
    }
    
    public void newTraveler(SAVTraveler person)
    {
        // if taxi is already there, enter taxi
        // otherwise wait for taxi to show up. taxiArrived() event call will assign traveler to taxi in this case
        
        Iterator<Taxi> iter = person.getOrigin().getParkedTaxis().iterator();
        
        while(iter.hasNext())
        {
            AssignedTaxi taxi = (AssignedTaxi)iter.next();
            
            if(taxi.getNextTraveler() == person)
            {
                getSimulator().addTravelerToTaxi(taxi, person);
                taxi.setPath(taxi.getNextSegment());
                getSimulator().addDeparting(taxi);
                
                iter.remove();
                break;
            }
        }
    }
}
