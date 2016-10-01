/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.node.Location;
import avdta.network.node.Zone;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * This class modifies normal {@link Zone} behavior to reflect SAVs: SAVs may stay parked at {@link Zone}s for longer.
 * Personal vehicles are created and exit the network at zones. 
 * In contrast, SAVs pick up and drop off travelers, who are created and exit at zones.
 * SAVs themselves park or continue traveling in the network.
 * @author Michael
 */
public abstract class SAVZone extends Zone
{
    protected Set<Taxi> parkedTaxis;
    
    /**
     * Creates this {@link SAVZone} with the specified id.
     * @param id the id
     */
    public SAVZone(int id)
    {
        super(id);
        
        parkedTaxis = new HashSet<Taxi>();
    }
    
    /**
     * Creates this {@link SAVZone} with the specified id and location.
     * @param id the id
     * @param loc the location
     */
    public SAVZone(int id, Location loc)
    {
        super(id, loc);
        
        parkedTaxis = new HashSet<Taxi>();
    }
    
    /**
     * Checks whether the specified {@link Taxi} is parked here.
     * @param t the taxi
     * @return whether the specified {@link Taxi} is parked here
     */
    public boolean containsTaxi(Taxi t)
    {
        return parkedTaxis.contains(t);
    }
    
    /**
     * Returns a set of all parked taxis.
     * @return a set of all parked taxis
     */
    public Set<Taxi> getParkedTaxis()
    {
        return parkedTaxis;
    }
    
    /**
     * Adds a taxi to the list of parked taxis.
     * @param t the taxi to be added
     */
    public void addParkedTaxi(Taxi t)
    {
        parkedTaxis.add(t);
    }
    
    /**
     * Called when a taxi reaches this {@link SAVZone}.
     * @param t the taxi to be added
     * @return the number of exiting travelers
     */
    public abstract int addTaxi(Taxi t);
    

    /**
     * Removes the taxi from the list of parked taxis.
     * @param t the taxi to be removed
     */
    public void removeTaxi(Taxi t)
    {
        parkedTaxis.remove(t);
    }
    
    /**
     * Resets this {@link SAVZone} to restart the simulation.
     */
    public void reset()
    {
        
        parkedTaxis.clear();
    }
    
}
