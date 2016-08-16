/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.DriverType;
import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import avdta.network.node.TurningMovement;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ut
 */
public abstract class TBR extends IntersectionControl
{
    protected Map<Link, Map<Link, TurningMovement>> conflicts;
    protected Set<ConflictRegion> allConflicts;
    
    public TBR()
    {
        this(null);
    }
    
    public TBR(Intersection n)
    {
        super(n);
    }

    
    public TurningMovement getConflicts(Link i, Link j)
    {
        return conflicts.get(i).get(j);
    }
    
    public boolean hasConflictRegions()
    {
        return allConflicts.size() > 0;
    }
    
    public void initialize()
    {
        conflicts = ConflictFactory.generate(getNode());

        allConflicts = new HashSet<ConflictRegion>();
        
        for(Link i : conflicts.keySet())
        {
            for(Link j : conflicts.get(i).keySet())
            {
                for(ConflictRegion cr : conflicts.get(i).get(j))
                {
                    allConflicts.add(cr);
                    
                    cr.setMaxScale(i);
                }
            }
        }
    }
    
    public void reset()
    {
        for(ConflictRegion cr : allConflicts)
        {
            cr.reset();
        }
    }
    
    public Set<ConflictRegion> getConflicts(Vehicle v)
    {
        return conflicts.get(v.getPrevLink()).get(v.getNextLink());
    }
    
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return conflicts.get(i).containsKey(j);
    }
    
    public void updateCRCapacity()
    {
        for(ConflictRegion cr : allConflicts)
        {
            cr.setCapacity(0);
        }
        
        for(Link i : conflicts.keySet())
        {
            Map<Link, TurningMovement> temp = conflicts.get(i);
            
            for(Link j : temp.keySet())
            {
                for(ConflictRegion cr : temp.get(j))
                {
                    cr.setCapacity(Math.max(cr.getCapacity(), i.getCapacityPerLane() * i.getDsLanes()));
                }
            }
        }
    }
    
    public boolean hasAvailableCapacity(Link i, Link j, double flow)
    {
        
        boolean output = (i.Q - i.q) * (1 - (i.lanes_blocked) / ((double)i.getDsLanes())) >= flow;

        for(ConflictRegion cr : conflicts.get(i).get(j))
        {
            if(!cr.canMove(i, j, flow))
            {
                output = false;
                //System.out.println(cr.R);
            }

        }
        
        return output;
        
        
        /*
        if(!(j.R >= flow && (i.Q - i.q) * (1 - (i.lanes_blocked) / ((double)i.getNumLanes())) >= flow))
        {
            return false;
        }
        
        for(ConflictRegion cr : conflicts.get(i).get(j))
        {
            if(!cr.canMove(i, flow))
            {
                return false;
            }
        }
        return true;
        */
    }
    
    public abstract int getType();
}
