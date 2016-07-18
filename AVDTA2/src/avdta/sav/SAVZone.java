/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Location;
import avdta.network.Zone;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 *
 * @author Michael
 */
public abstract class SAVZone extends Zone
{
    protected Set<Taxi> parkedTaxis;
    
    public SAVZone(int id)
    {
        super(id);
        
        parkedTaxis = new HashSet<Taxi>();
    }
    
    public SAVZone(int id, Location loc)
    {
        super(id, loc);
        
        parkedTaxis = new HashSet<Taxi>();
    }
    
    public boolean containsTaxi(Taxi t)
    {
        return parkedTaxis.contains(t);
    }
    
    public Set<Taxi> getParkedTaxis()
    {
        return parkedTaxis;
    }
    
    public void addParkedTaxi(Taxi t)
    {
        parkedTaxis.add(t);
    }
    
    // return # of passengers exited
    public abstract int addTaxi(Taxi t);
    

    
    public void removeTaxi(Taxi t)
    {
        parkedTaxis.remove(t);
    }
    
    public void reset()
    {
        
        parkedTaxis.clear();
    }
    
}
