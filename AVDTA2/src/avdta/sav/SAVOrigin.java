/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.node.Location;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.network.node.Zone;
import avdta.sav.dispatch.AssignedDispatch;
import avdta.sav.dispatch.RealTimeDispatch;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * This is the origin version of a centroid for SAVs.
 * Taxis can wait here indefinitely until dispatched to new locations.
 * This class also contains methods to respond to events of taxis arriving or travelers departing. 
 * See {@link SAVOrigin#addTraveler(avdta.sav.Traveler)} and {@link SAVOrigin#addTaxi(avdta.sav.Taxi)}.
 * @author Michael
 */
public class SAVOrigin extends SAVZone
{
    private TreeSet<SAVTraveler> waitingTravelers;
    
    private TreeSet<Taxi> enrouteTaxis;
    private List<Taxi> freeTaxis;
    
    private int storedGoal;
    public int storedDiff;
    
    /**
     * Creates this {@link SAVOrigin} with the specified id.
     * @param id the id
     */
    public SAVOrigin(int id)
    {
        this(id, new Location());
    }
    
    
    /**
     * Creates this {@link SAVOrigin} with the specified id and location.
     * @param id the id
     * @param loc the location
     */
    public SAVOrigin(int id, Location loc)
    {
        super(id, loc);
        
        waitingTravelers = new TreeSet<SAVTraveler>();

    }
    
    public void initialize()
    {
        super.initialize();
        
        if(SAVSimulator.dispatch instanceof RealTimeDispatch)
        {
            freeTaxis = new ArrayList<Taxi>();
            
            enrouteTaxis = new TreeSet<Taxi>(new Comparator<Taxi>()
            {
                public int compare(Taxi lhs, Taxi rhs)
                {
                    return lhs.eta - rhs.eta;
                }
            });
        }
    }
    
    /**
     * Update the number of taxis that should be stored here.
     * @param g the new stored goal
     */
    public void setStoredGoal(int g)
    {
        storedGoal = g;
    }
    
    /**
     * Returns the number of taxis that should be stored here.
     * @return the number of taxis that should be stored here
     */
    public int getStoredGoal()
    {
        return storedGoal;
    }
    
    /**
     * Attempts to remove the specified taxi from the set of free taxis.
     * @param t the taxi to be removed
     * @return whether the taxi was in the set of free taxis
     */
    public boolean removeFreeTaxi(Taxi t)
    {
        return freeTaxis.remove(t);
    }
    
    /**
     * Attempts to add the specified taxi to the set of free taxis.
     * A taxi cannot be added more than once.
     * @param t the taxi to be added
     * @return whether the taxi was added to the set
     */
    public boolean addFreeTaxi(Taxi t)
    {
        return freeTaxis.add(t);
    }
    
    /**
     * Returns the number of waiting travelers.
     * @return the number of waiting travelers
     */
    public int getNumWaiting()
    {
        return waitingTravelers.size();
    }
    
    /**
     * Returns a set of waiting travelers, sorted by departure times.
     * @return a set of waiting travelers
     */
    public TreeSet<SAVTraveler> getWaitingTravelers()
    {
        return waitingTravelers;
    }
    
    
    public void addTraveler(SAVTraveler p)
    {
        waitingTravelers.add(p);
        
        // update etd
        if(enrouteTaxis != null && enrouteTaxis.size() >= waitingTravelers.size())
        {
            int count = waitingTravelers.size()-1;
            
            Iterator<Taxi> iter = enrouteTaxis.iterator();
            
            for(int i = 0; i < count && iter.hasNext(); i++)
            {
                iter.next();
            }
            
            if(iter.hasNext())
            {
                p.setEtd(iter.next().eta);
            }
            
        }
        
    }
    
    /**
     * Removes a traveler from the list of waiting travelers.
     * @param person the traveler to be removed
     */
    public void removeTraveler(SAVTraveler person)
    {
        waitingTravelers.remove(person);  
    }
    
    /**
     * Resets this {@link SAVOrigin} to restart the simulation.
     */
    public void reset()
    {
        super.reset();
        
        waitingTravelers.clear();
        
        if(enrouteTaxis != null)
        {
            freeTaxis.clear();
            enrouteTaxis.clear();
        }
    }
    
    /**
     * Returns the number of taxis enroute to the linked {@link SAVDest}.
     * @return the number of taxis enroute to this centroid
     */
    public int getEnrouteTaxis()
    {
        return enrouteTaxis.size();
    }
    
    /**
     * Returns a set of free taxis.
     * @return a set of free taxis
     */
    public List<Taxi> getFreeTaxis()
    {
        return freeTaxis;
    }
    
    /**
     * Adds a taxi to the list of enroute taxis.
     * @param t the taxi to be added
     */
    public void addEnrouteTaxi(Taxi t)
    {
        enrouteTaxis.add(t);
        
        // check if all passengers in t are destined here
        for(SAVTraveler p : t.getPassengers())
        {
            if(p.getDest() != getLinkedZone())
            {
                return;
            }
        }
        updateEtds(t.eta);
        // whenever a taxi is on the way, update etd for next traveler
    }
    
    public void removeEnrouteTaxi(Taxi t)
    {
        enrouteTaxis.remove(t);
        
        if(enrouteTaxis.size() > 0)
        {
            updateEtds(enrouteTaxis.first().eta);
        }
    }
    
    /**
     * Updates the expected departure times based on the new eta for the enroute taxi.
     * @param eta the new eta
     */
    public void updateEtds(int eta)
    {
        for(SAVTraveler p : waitingTravelers)
        {
            if(p.getEtd() < eta)
            {
                int old = p.getEtd();
                p.setEtd(eta);
                updateEtds(old);
                break;
            }
        }     
    }
    
    
    public int addTaxi(Taxi taxi)
    {
        taxi.setLocation(this);
        
        if(enrouteTaxis != null)
        {
            enrouteTaxis.remove(taxi);
        }
        
        // decide what to do with taxi
        addParkedTaxi(taxi);
        
        SAVSimulator.dispatch.taxiArrived(taxi, this);
        
        return 0;
    }
    
    /**
     * A single time step of simulation.
     * This iterates through the list of taxis. 
     * Taxis that have elapsed their dwell time (see {@link Taxi#DELAY_ENTER}) will depart.
     * Taxis that do not have a path will be assigned to be moved.
     * @return the number of exiting travelers
     */
    public int step()
    {
        Iterator<Taxi> iterator = getParkedTaxis().iterator();
        
        while(iterator.hasNext())
        {
            Taxi t = iterator.next();
            
            if(t.getPath() != null && t.getPath().size() > 0)
            {
                if(t.delay <= 0)
                {
                    iterator.remove();
                    t.entered();
                    t.getNextLink().addVehicle(t);
                    // whenever a taxi departs, update its eta
                }
                else
                {
                    t.delay -= Simulator.dt;
                }
            }
            /*
            else if(waitingTravelers.size() > 0)
            {
                SAVSimulator.dispatch.assignTaxi(t);
                
            }
            */
        }
        
        return 0;
    }
}
