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
 * This is an abstract representation of reservation-based intersection control. 
 * TBR stands for tile-based reservation.
 * Vehicle movement is left to subclasses, but the work of generating {@link ConflictRegion}s is taken care of.
 * 
 * @author Michael
 */
public abstract class TBR extends IntersectionControl
{
    protected Map<Link, Map<Link, TurningMovement>> conflicts;
    protected Set<ConflictRegion> allConflicts;
    
    /**
     * Constructs this {@link TBR} with a null {@link Node}.
     */
    public TBR()
    {
        this(null);
    }
    
    /**
     * Constructs this {@link TBR} with the specified {@link Intersection}
     * @param n the {@link Intersection} this {@link TBR} controls
     */
    public TBR(Intersection n)
    {
        super(n);
    }

    /**
     * Returns the {@link TurningMovement} object associated with the specified turning movement
     * @param i the incoming {@link Link}
     * @param j the outgoing {@link Link}
     * @return the {@link TurningMovement} object associated with the specified turning movement 
     * @see TurningMovement
     */
    public TurningMovement getConflicts(Link i, Link j)
    {
        return conflicts.get(i).get(j);
    }
    
    /**
     * Returns whether this intersection control uses {@link ConflictRegion}s
     * @return whether any {@link ConflictRegion}s have been generated for this {@link TBR}
     */
    public boolean hasConflictRegions()
    {
        return allConflicts.size() > 0;
    }
    
    /**
     * Initializes this {@link TBR} after data has been read.
     * This method creates the {@link ConflictRegion}s that each turning movement passes through.
     * @see ConflictFactory
     */
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
    
    /**
     * Resets this {@link TBR} to restart simulation.
     * This resets all {@link ConflictRegion}s.
     */
    public void reset()
    {
        for(ConflictRegion cr : allConflicts)
        {
            cr.reset();
        }
    }
    
    /**
     * Returns the {@link Set} of {@link ConflictRegion}s that the specified {@link Vehicle} must pass through. 
     * This relies on {@link Vehicle#getPrevLink()} being an incoming {@link Link}, and {@link Vehicle#getNextLink()} being an outgoing {@link Link}.
     * @param v the {@link Vehicle} wanting to move
     * @return the {@link Set} of {@link ConflictRegion}s that the specified {@link Vehicle} must pass through
     */
    public Set<ConflictRegion> getConflicts(Vehicle v)
    {
        return conflicts.get(v.getPrevLink()).get(v.getNextLink());
    }
    
    /**
     * Returns a set containing all {@link ConflictRegion}s associated with this intersection.
     * @return a set containing all {@link ConflictRegion}s
     */
    public Set<ConflictRegion> getConflicts()
    {
        return allConflicts;
    }
    
    /**
     * Returns whether the specified turning movement is valid. 
     * A turning movement is valid if there is a set of associated {@link ConflictRegion}s. U-turns are not valid, but all other turns are.
     * The {@link DriverType} is not checked here. It is checked in the {@link Link} class.
     * 
     * @param i the incoming {@link Link}
     * @param j the outgoing {@link Link}
     * @param driver the {@link DriverType}
     * @return whether the specified turning movement is valid
     */
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return conflicts.get(i).containsKey(j);
    }
    
    /**
     * Updates the {@link ConflictRegion} capacities in response to changing numbers of lanes.
     * This is relevant to dynamic lane reversal and dynamic transit lanes.
     */
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
    
    /**
     * Checks whether there is available capacity to make the specified turning movement with the specified required flow.
     * @param i the incoming {@link Link}
     * @param j the outgoing {@link Link}
     * @param flow equivalent flow of the vehicle. This may be reduced for autonomous vehicles due to lower following headways.
     * @return if there is sufficient capacity to make the specified turn
     */
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
    
    /**
     * Returns the type code representing this intersection control.
     * @return depends on the subclass
     */
    public abstract int getType();
}
