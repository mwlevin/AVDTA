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
 * This gives priority to vehicles making turning movements within a phase, for intersection coordination.
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
    
    public PhasedTBR()
    {
        phases = new ArrayList<Phase>();
        turns = new HashMap<Link, Map<Link, PhaseMovement>>();
        
        setPolicy(new PhasePriority(this));
    }
    
    public PhasedTBR(Intersection n)
    {
        super(n);
        turns = new HashMap<Link, Map<Link, PhaseMovement>>();
        phases = new ArrayList<Phase>();
        
        
        setPolicy(new PhasePriority(this));
    }
    
    public Signalized getSignal()
    {
        return this;
    }
    
    public double getOffset()
    {
        return offset;
    }
    
    public void setOffset(double offset)
    {
        this.offset = offset;
    }
    
    public List<Phase> getPhases()
    {
        return phases;
    }
    
    public void reset()
    {
        curr_idx = 0;
        curr_time = 0;
    }
    
    public void addPhase(Phase p)
    {
        phases.add(p);
        
        total_time += p.getDuration();
    }
    
    public void initialize()
    {
        super.initialize();
        
        Collections.sort(phases);
        
        curr_time = offset;
        curr_idx = 0;
        
        double temp = curr_time;
        for(Phase p : phases)
        {
            if(p.getDuration() >= temp)
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
    
    public int step()
    {
        calcGreenTime();
        
        return super.step();
    }
}
