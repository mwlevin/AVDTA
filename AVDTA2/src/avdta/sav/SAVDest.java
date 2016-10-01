/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.link.Link;
import avdta.network.node.Location;
import avdta.network.node.Zone;
import avdta.network.Simulator;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * The {@link SAVDest} is like a normal centroid except that it creates an event when SAVs arrive at the destination.
 * Taxis arrive here, and if they unload passengers, must wait for a specified dwell time ({@link Taxi#DELAY_EXIT}).
 * After the dwell time has elapsed, taxis are parked at the linked {@link SAVOrigin} (see {@link Zone#getLinkedZone()}).
 * @author Michael
 */
public class SAVDest extends SAVZone
{
    /**
     * Creates this {@link SAVDest} with the specified id.
     * @param id the id
    */
    public SAVDest(int id)
    {
        super(id);
    }
    
    /**
     * Creates this {@link SAVDest} with the specified id and location.
     * @param id the id
     * @param loc the location
     */
    public SAVDest(int id, Location loc)
    {
        super(id, loc);
    }
    
    /**
     * This method removes taxis from incoming centroid connectors and calls {@link SAVDest#addTaxi(avdta.sav.Taxi)}.
     * In addition, taxis that have elapsed their dwell time will be added to the linked {@link SAVOrigin}.
     * @return 
     */
    public int step()
    {
        int output = 0;
        
        for(Link l : getIncoming())
        {
            for(Vehicle v : l.getSendingFlow())
            {
                if(v instanceof Taxi)
                {
                    output += addTaxi((Taxi)v);
                }
                
                v.exited();
                l.removeVehicle(v);
                
            }
        }
        
        Iterator<Taxi> iterator = parkedTaxis.iterator();
        
        while(iterator.hasNext())
        {
            Taxi t = iterator.next();
            if(t.delay <= 0 && !t.tempTaxi)
            {
                ((SAVOrigin)getLinkedZone()).addTaxi(t);
                iterator.remove();
            }
            else
            {
                t.delay -= Simulator.dt;
            }
        }
        

        return output;
    }
    
    /**
     * Adds a taxi to the destination.
     * Any travelers in the taxi that are destined for this destination will exit.
     * The taxi will park at this destination for {@link Taxi#DELAY_EXIT}, then be added to the linked {@link SAVOrigin}.
     * 
     * @param t the taxi to be added
     * @return the number of exiting travelers
     */
    public int addTaxi(Taxi t)
    {
        t.total_distance += t.getPath().getLength();
        
        if(t.getNumPassengers() == 0)
        {
            t.empty_distance += t.getPath().getLength();
        }
        
        t.setPath(null);
        
        int output = 0;
        
        // remove travelers for this destination
        ListIterator<Traveler> passengers = t.getPassengers().listIterator();
        
        while(passengers.hasNext())
        {
            Traveler person = passengers.next();
            
            if(person.getDest() == this)
            {
                person.exited();
                passengers.remove();
                t.delay = Taxi.DELAY_EXIT;
                output++;
            }
        }
        
        if(t.tempTaxi)
        {
            parkedTaxis.add(t);
            t.delay = Integer.MAX_VALUE;
        }
        else if(t.delay > 0)
        {
            parkedTaxis.add(t);
        }
        else
        {
            ((SAVOrigin)getLinkedZone()).addTaxi(t);
        }
        
        return output;
        
    }
}
