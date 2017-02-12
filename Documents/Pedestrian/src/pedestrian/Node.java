/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class Node 
{
    public static final int MAX_QUEUE = 5;
    public static final int V_DT = 10;
    
    private Set<IncomingLink> incoming;
    private Set<OutgoingLink> outgoing;
    
    private Set<Crosswalk> crosswalks;
    
    private Queue[] queues;
    
    private Set<ConflictRegion> conflictRegions;
    
    public Node()
    {

        incoming = new HashSet<IncomingLink>();
        outgoing = new HashSet<OutgoingLink>();
        conflictRegions = new HashSet<ConflictRegion>();
        crosswalks = new HashSet<Crosswalk>();
        
        queues = new Queue[0];
    }
    
    public void addCrosswalk(Crosswalk c)
    {
        crosswalks.add(c);
    }
    
    public void addLink(IncomingLink l)
    {
        incoming.add(l);
    }
    
    public void addLink(OutgoingLink l)
    {
        outgoing.add(l);
    }
    
    public void setConflictRegions(Set<ConflictRegion> cr)
    {
        conflictRegions = cr;
    }
    
    public double oneStepCost(State state, Action action)
    {
        int[] x = state.getQueueLengths();
        int[] y = action.getQueueChanges();
        
        double output = 0.0;
        
        for(int i = 0; i < queues.length; i++)
        {
            output += (x[i] - y[i]);
        }
        
        return output * action.getDuration();
    }
    
    public List<Action> createActions(State state)
    {
        List<Action> output = new ArrayList<Action>();
        
        // crosswalk combinations
        outer: for(int com = 0; com < Math.pow(2, crosswalks.size()); com++)
        {
            // update crosswalk activation
            for(int i = 0; i < crosswalks.size(); i++)
            {
                ((Crosswalk)queues[i]).active = ((com >> i) & 1) == 1;
            }
            
            for(Crosswalk c : crosswalks)
            {
                if(c.active && state.getQueueLengths()[c.getIndex()] == 0)
                {
                    continue outer;
                }
            }
            
            double duration = V_DT;
            
            for(Crosswalk c : crosswalks)
            {
                if(c.active)
                {
                    duration = Math.max(duration, c.getMinDuration());
                }
            }
            
            
            calcVehicleFlow(state, duration);

            int[] queueLen = state.getQueueLengths();

            int[] y = new int[queues.length];
            for(int i = 0; i < queues.length; i++)
            {
                Queue queue = queues[i];

                if(queue instanceof Crosswalk)
                {
                    y[i] = ((Crosswalk)queue).active? queueLen[i] : 0;
                }
                else
                {
                    y[i] = ((TurningMovement)queue).y;
                }
            }

            boolean[] active = new boolean[crosswalks.size()];

            for(Crosswalk c : crosswalks)
            {
                active[c.getIndex()] = c.active;
            }

            Action action = new Action(active, y, duration);

            output.add(action);
        }
        
        
        return output;
    }
    
    public void calcVehicleFlow(State state, double duration)
    {
        int[] len = state.getQueueLengths();
        
        for(int i = crosswalks.size(); i < queues.length; i++)
        {
            ((TurningMovement)queues[i]).y = 0;
        }
        
        for(ConflictRegion cr : conflictRegions)
        {
            cr.y = 0;
        }

        
        for(OutgoingLink j : outgoing)
        {
            j.R = j.getReceivingFlow(duration);
            j.y = 0;
        }
        
        List<TurningMovement> turns = new ArrayList<TurningMovement>();
        
        for(IncomingLink i : incoming)
        {
            for(OutgoingLink j : outgoing)
            {
                TurningMovement mvt = i.getQueue(j);
                
                if(mvt == null)
                {
                    continue;
                }

                double weight = len[mvt.getIndex()] * Math.min(i.getCapacity(), j.getCapacity()) / 3600.0;
                
                
                
                double denom = 1.0 / j.getReceivingFlow(duration);
                
                for(ConflictRegion cr : mvt.getConflictRegions())
                {
                    denom += 1.0 / (duration * i.getCapacity() / 3600.0);
                }

                
                mvt.efficiency = weight / denom;
                
                turns.add(mvt);
            }
        }
        
        Collections.sort(turns);
        

        outer:for(TurningMovement mvt : turns)
        {
            OutgoingLink j = mvt.getJ();
            IncomingLink i = mvt.getI();
            
            for(Crosswalk c : crosswalks)
            {
                if(c.active && (c.crosses(i) || c.crosses(j)))
                {
                    continue outer;
                }
            }
                
            int newY = len[mvt.getIndex()] - mvt.y;
            newY = (int)Math.min(newY, Math.floor(j.R - j.y));

            
            for(ConflictRegion cr : mvt.getConflictRegions())
            {
                newY = (int)Math.min(newY, (1.0 - cr.y) * (duration * i.getCapacity() / 3600.0) );
            }

            mvt.y = newY;
                
            j.y += newY;
            
            for(ConflictRegion cr : mvt.getConflictRegions())
            {
                cr.y += newY/(duration * i.getCapacity()/3600.0);
            }
        }
    }
    
    
    public Set<IncomingLink> getIncoming()
    {
        return incoming;
    }
    
    public Set<OutgoingLink> getOutgoing()
    {
        return outgoing;
    }
    
    public void addLink(Link l)
    {
        if(l instanceof IncomingLink)
        {
            incoming.add((IncomingLink)l);
        }
        else if(l instanceof OutgoingLink)
        {
            outgoing.add((OutgoingLink)l);
        }
    }
    
    public void initialize()
    {

        
        for(ConflictRegion cr : conflictRegions)
        {
            cr.initialize();
        }
        
        int count = crosswalks.size();
        
        for(IncomingLink l : incoming)
        {
            count += l.getNumQueues();
        }
        
        queues = new Queue[count];
        
        int idx = 0;
        
        for(Crosswalk c : crosswalks)
        {
            queues[idx] = c;
            queues[idx].setIndex(idx);
            idx++;
        }
        
        for(IncomingLink l : incoming)
        {
            for(OutgoingLink v : outgoing)
            {
                Queue temp = l.getQueue(v);
                
                if(temp != null)
                {
                    queues[idx] = l.getQueue(v);
                    queues[idx].setIndex(idx);
                    idx++;
                }
            }
        }
    }
    
    public int getNumQueues()
    {
        return queues.length;
    }
    
    public Queue[] getQueues()
    {
        return queues;
    }
}
