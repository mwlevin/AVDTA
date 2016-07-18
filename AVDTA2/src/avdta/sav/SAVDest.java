/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Link;
import avdta.network.Location;
import avdta.network.Simulator;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 *
 * @author Michael
 */
public class SAVDest extends SAVZone
{
    public SAVDest(int id)
    {
        super(id);
    }
    
    public SAVDest(int id, Location loc)
    {
        super(id, loc);
    }
    
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
