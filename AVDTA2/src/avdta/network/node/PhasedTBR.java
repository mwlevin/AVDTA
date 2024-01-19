/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.node.policy.PhasePriority;
import avdta.network.link.Link;
import avdta.network.Network;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a version of reservations in which vehicles with a green light get priority.
 * Other vehicles can still move if capacity is available.
 * 
 * @author Michael
 */


public class PhasedTBR extends PriorityTBR implements Signalized
{
    private List<Phase> phases;
    private int curr_idx;
    private double curr_time;
    private double total_time;
    private double offset;
    
    private Map<Link, Map<Link, PhaseMovement>> turns;
    
    /**
     * Constructs this {@link PhasedTBR} with empty {@link Phase}s for a null {@link Intersection}. A non-null {@link Intersection} is required for simulation.
     */
    public PhasedTBR()
    {
        phases = new ArrayList<Phase>();
        turns = new HashMap<Link, Map<Link, PhaseMovement>>();
        
        setPolicy(new PhasePriority(this));
    }
    
    /**
     * Constructs this {@link PhasedTBR} with empty {@link Phase}s for the specified {@link Intersection}
     * @param n the {@link Intersection} this {@link PhasedTBR} controls
     */
    public PhasedTBR(Intersection n)
    {
        super(n);
        turns = new HashMap<Link, Map<Link, PhaseMovement>>();
        phases = new ArrayList<Phase>();
        
        
        setPolicy(new PhasePriority(this));
    }
    
    /**
     * The {@link Signalized} form for adding signal data
     * @return this {@link PhasedTBR}
     */
    public Signalized getSignal()
    {
        return this;
    }
    
    /**
     * Returns the offset for the signal cycle
     * @return the offset for the signal cycle
     */
    public double getOffset()
    {
        return offset;
    }
    
    /**
     * Updates the offset for the signal cycle
     * @param offset the new offset
     */
    public void setOffset(double offset)
    {
        this.offset = offset;
    }
    
    /**
     * Returns the {@link List} of {@link Phase}s in the signal cycle
     * @return the {@link List} of {@link Phase}s
     */
    public List<Phase> getPhases()
    {
        return phases;
    }
    
    /**
     * Resets this {@link PhasedTBR} to restart simulation. This resets the current phase to start at the offset.
     */
    public void reset()
    {
        curr_idx = 0;
        
        curr_time = offset % getDuration();
        curr_idx = 0;
        
        double temp = curr_time;
        for(Phase p : phases)
        {
            if(temp >= p.getDuration())
            {
                temp -= p.getDuration();
                curr_idx++;
            }
            else
            {
                break;
            }
        }
    }
    
    /**
     * Adds a {@link Phase} to the cycle. {@link Phase}s do not need to be added in sequence order.
     * @param p the {@link Phase} to be added
     */
    public void addPhase(Phase p)
    {
        phases.add(p);
        
        total_time += p.getDuration();
    }
    
    /**
     * Returns the duration of the signal cycle
     * @return the duration of the signal cycle
     */
    public double getDuration()
    {
        return total_time;
    }
    
    /**
     * Initializes this {@link PhasedTBR} after the network is read. The list of {@link Phase}s is sorted according to sequence, and the current {@link Phase} is set based on the offset.
     * 
     */
    public void initialize()
    {
        super.initialize();
        
        Collections.sort(phases);
        
        curr_time = offset % getDuration();
        curr_idx = 0;
        
        double temp = curr_time;
        for(Phase p : phases)
        {
            if(temp >= p.getDuration())
            {
                temp -= p.getDuration();
                curr_idx++;
            }
            else
            {
                break;
            }
        }
        
        for(Phase p : phases)
        {
            for(Turn t : p.getAllowed())
            {
                if(!turns.containsKey(t.i))
                {
                    turns.put(t.i, new HashMap<Link, PhaseMovement>());
                }
                
                if(!turns.get(t.i).containsKey(t.j))
                {
                    turns.get(t.i).put(t.j, new PhaseMovement());
                }
            }
        }
        
        double time = 0;
        
        for(Phase p : phases)
        {
            p.setStartTime(time);
            time += p.getDuration();
        }
    }
    
    /**
     * Calculates the green time for the current time step, and updates the capacity for each {@link PhaseMovement}
     * @see PhaseMovement
     */
    public void calcGreenTime()
    {
        if(phases.size() == 0)
        {
            return;
        }
        
        double time_rem = Network.dt;
        
        for(Link i : turns.keySet())
        {
            for(Link j : turns.get(i).keySet())
            {
                PhaseMovement movement = turns.get(i).get(j);
                
                movement.newTimestep();
                movement.addLeftovers();
            }
        }
        
        for(Link j : getNode().getOutgoing())
        {
            j.R = j.getReceivingFlow();
        }
        
        //System.out.println(Simulator.time+"\t"+getId());
        
        while(time_rem > 0)
        {
            Phase curr_phase = phases.get(curr_idx);
            
            double green_time = Math.min(time_rem, curr_phase.getGreenTime(curr_time));
            
            
            
            for(Turn turn : curr_phase.getAllowed())
            {     
                try
                {
                    turns.get(turn.i).get(turn.j).q += (green_time / Network.dt) * turn.j.R;
                }
                catch(RuntimeException ex)
                {
                    System.out.println(turn.i+" "+turn.j);
                    throw ex;
                }

            }
            
            double rem_time = curr_phase.getRemainingTime(curr_time);
            double rem_time_dt = Math.min(time_rem, rem_time);
            
            if(rem_time <= time_rem)
            {
                curr_idx++;
            }
            
            
            time_rem -= rem_time_dt;
            curr_time += rem_time_dt;
            
            //System.out.println("\t"+curr_idx+"\t"+green_time+" "+dt+" "+curr_time+" "+rem_time);
            
            
            
            if(curr_idx == phases.size())
            {
                curr_idx = 0;
                curr_time -= total_time;
            }
        }

    }
    
    /**
     * Attempts to reserve capacity for the specified {@link Vehicle}. 
     * This relies on {@link Vehicle#getPrevLink()} being an incoming {@link Link}, and {@link Vehicle#getNextLink()} being an outgoing {@link Link}.
     * @param v the vehicle reserving capacity
     * @return whether the reservation was successful
     */
    public boolean reserveCapacity(Vehicle v)
    {
        if(v.getNextLink() == null)
        {
            return true;
        }
        
        if(phases.size() == 0)
        {
            return false;
        }
        
        Link i = v.getPrevLink();
        Link j = v.getNextLink();
        
        PhaseMovement turn = null;
        try
        {
            turn = turns.get(i).get(j); 
        }
        catch(Exception ex)
        {
            return false;
        }

        
        double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
        
        if(turn == null)
        {
            return false;
        }
        
        if(turn.q >= equiv_flow)
        {
            turn.q -= equiv_flow;
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Executes one time step of simulation
     * {@link Vehicle}s that do not have a next {@link Link} (some buses) are removed from the network.
     * {@link Vehicle}s that move are removed from their previous {@link Link} ({@link Link#removeVehicle(avdta.vehicle.Vehicle)} and added to their next {@link Link} ({@link Link#addVehicle(avdta.vehicle.Vehicle)}).
     * This requires that {@link Vehicle#getPrevLink()} is the incoming {@link Link} and {@link Vehicle#getNextLink()} is the outgoing {@link Link}.
     * * Human-driven vehicles traveling through a reservation must reserve all possible turning movements.
     * @return the number of exiting vehicles
     */
    public int step()
    {
        calcGreenTime();
        
        return super.step();
    }
}
