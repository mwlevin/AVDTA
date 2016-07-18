/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Location;
import avdta.network.Simulator;
import avdta.cost.TravelCost;
import avdta.network.Zone;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Michael
 */
public class SAVOrigin extends SAVZone
{
    private TreeSet<Traveler> waitingTravelers;
    
    private Set<Taxi> enrouteTaxis;
    
    private int storedGoal;
    public int storedDiff;
    
    public SAVOrigin(int id)
    {
        super(id);
        
        waitingTravelers = new TreeSet<Traveler>();
        enrouteTaxis = new TreeSet<Taxi>(new Comparator<Taxi>()
        {
            public int compare(Taxi lhs, Taxi rhs)
            {
                return lhs.eta - rhs.eta;
            }
        });
    }
    
    public SAVOrigin(int id, Location loc)
    {
        super(id, loc);
        
        waitingTravelers = new TreeSet<Traveler>();
        enrouteTaxis = new TreeSet<Taxi>(new Comparator<Taxi>()
        {
            public int compare(Taxi lhs, Taxi rhs)
            {
                return lhs.eta - rhs.eta;
            }
        });
    }
    
    public void setStoredGoal(int g)
    {
        storedGoal = g;
    }
    
    public int getStoredGoal()
    {
        return storedGoal;
    }
    
    public int getNumWaiting()
    {
        return waitingTravelers.size();
    }
    
    public TreeSet<Traveler> getWaitingTravelers()
    {
        return waitingTravelers;
    }
    
    public void addTraveler(Traveler p)
    {
        waitingTravelers.add(p);
        
        for(Taxi t : getParkedTaxis())
        {
            if(t.getPath() == null)
            {
                SAVSimulator.assignTaxi(t);
                return;
            }
        }
        
        
        // update etd
        if(enrouteTaxis.size() >= waitingTravelers.size())
        {
            int count = waitingTravelers.size()-1;
            
            Iterator<Taxi> iter = enrouteTaxis.iterator();
            
            for(int i = 0; i < count; i++)
            {
                iter.next();
            }
            
            p.etd = iter.next().eta;
        }
    }
    
    public void removeTraveler(Traveler person)
    {
        waitingTravelers.remove(person);  
    }
    
    public void reset()
    {
        super.reset();
        waitingTravelers.clear();
        enrouteTaxis.clear();
    }
    
    public int getEnrouteTaxis()
    {
        return enrouteTaxis.size();
    }
    
    public void addEnrouteTaxi(Taxi t)
    {
        enrouteTaxis.add(t);
        
        // check if all passengers in t are destined here
        for(Traveler p : t.getPassengers())
        {
            if(p.getDest() != getLinkedZone())
            {
                return;
            }
        }
        updateEtds(t.eta);
        // whenever a taxi is on the way, update etd for next traveler
    }
    
    public void updateEtds(int eta)
    {
        for(Traveler p : waitingTravelers)
        {
            if(p.etd < eta)
            {
                int old = p.etd;
                p.etd = eta;
                updateEtds(old);
                break;
            }
        }     
    }
    
    public int addTaxi(Taxi taxi)
    {
        taxi.setLocation(this);
        
        enrouteTaxis.remove(taxi);
        
        // decide what to do with taxi
        addParkedTaxi(taxi);
        
        if(SAVSimulator.ride_share && taxi.getNumPassengers() > 0 && taxi.getNumPassengers() < taxi.getCapacity() && waitingTravelers.size() > 0)
        {
            Set<SAVDest> dests = new HashSet<SAVDest>();
            
            for(Traveler p : taxi.getPassengers())
            {
                dests.add(p.getDest());
            }
            
            taxi.setPath(SAVSimulator.getPath(this, taxi.getPassengers().get(0).getDest()));
            
            Set<Traveler> removed = new HashSet<Traveler>();
            for(Traveler p : waitingTravelers)
            {
                if(dests.contains(p.getDest()))
                {
                    SAVSimulator.addTraveler(taxi, p);
                    removed.add(p);

                    if(taxi.getNumPassengers() >= taxi.getCapacity())
                    {
                        break;
                    }
                }
            }

            for(Traveler p : removed)
            {
                waitingTravelers.remove(p);
            }
            
            if(taxi.getNumPassengers() < taxi.getCapacity())
            {
                SAVSimulator.assignTaxi_ride_share(taxi);
            }
            
        }
        
        
        if(taxi.getNumPassengers() > 0)
        {
            taxi.setPath(SAVSimulator.getPath(this, taxi.getPassengers().get(0).getDest()));
        }
        // if no travelers waiting, hold taxi
        else if(waitingTravelers.size() == 0)
        {
            SAVSimulator.addFreeTaxi(taxi);
        }
        // otherwise, give taxi to longest waiting traveler
        else
        {
            SAVSimulator.assignTaxi(taxi);
            taxi.delay = Taxi.DELAY_ENTER;
        }
        
        return 0;
    }
    
    public int step()
    {
        Iterator<Taxi> iterator = parkedTaxis.iterator();
        
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
                    t.eta = (int)(t.getPath().getAvgCost(Simulator.time, 1.0, TravelCost.dnlTime));
                    ((SAVOrigin)((Zone)t.getPath().getDest()).getLinkedZone()).addEnrouteTaxi(t);
                }
                else
                {
                    t.delay -= Simulator.dt;
                }
            }
            else if(waitingTravelers.size() > 0)
            {
                SAVSimulator.assignTaxi(t);
                t.delay = Taxi.DELAY_ENTER;
            }
        }
        
        return 0;
    }
}
