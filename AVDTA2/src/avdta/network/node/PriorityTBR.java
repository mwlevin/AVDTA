/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.node.policy.IntersectionPolicy;
import avdta.network.link.Link;
import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.vehicle.Vehicle;
import avdta.network.node.TurningMovement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * This class represents a reservation-based intersection control in which {@link Vehicle} movement is determined by their priority. 
 * The priority is specified by a {@link IntersectionPolicy} instance variable.
 * 
 * @author Michael
 */
public class PriorityTBR extends TBR
{
    private IntersectionPolicy policy;

    /**
     * Constructs this {@link PriorityTBR} with {@link IntersectionPolicy#FCFS} and a null intersection. A non-null intersection is required for simulation.
     */
    public PriorityTBR()
    {
        this(null, IntersectionPolicy.FCFS);
    }
    
    /**
     * Constructs this {@link PriorityTBR} with the specified {@link IntersectionPolicy} and a null intersection. A non-null intersection is required for simulation.
     * @param policy the {@link IntersectionPolicy} that determines vehicle order
     */
    public PriorityTBR(IntersectionPolicy policy)
    {
        this(null, policy);
    }
    
    /**
     * Constructs this {@link PriorityTBR} with {@link IntersectionPolicy#FCFS} and the specified {@link Intersection}.
     * @param n the {@link Intersection} controlled by this {@link PriorityTBR}
     */
    public PriorityTBR(Intersection n)
    {
        this(n, IntersectionPolicy.FCFS);
    }
   
    /**
     * Constructs this {@link PriorityTBR} with the specified {@link IntersectionPolicy} and the specified {@link Intersection}.
     * @param n the {@link Intersection} controlled by this {@link PriorityTBR}
     * @param policy the {@link IntersectionPolicy} that determines vehicle order
     */
    public PriorityTBR(Intersection n, IntersectionPolicy policy)
    {
        super(n);
        
        this.policy = policy;
    }
    
    /**
     * Returns the {@link Signalized} form for adding signal data.
     * @return the {@link IntersectionPolicy} if it is a {@link Signalized}, or null otherwise
     * @see Signalized
     */
    public Signalized getSignal()
    {
        return (policy instanceof Signalized)? (Signalized)policy : null;
    }
    

    /**
     * Returns the type code for {@link PriorityTBR}
     * @return {@link ReadNetwork#RESERVATION}+ the type of the {@link IntersectionPolicy}
     */
    public int getType()
    {
        return ReadNetwork.RESERVATION + policy.getType();
    }
    
    /**
     * Updates the {@link IntersectionPolicy} used for ordering vehicles
     * @param policy the new {@link IntersectionPolicy} used for ordering vehicles
     */
    public void setPolicy(IntersectionPolicy policy)
    {
        this.policy = policy;
    }
    

    /**
     * Returns the {@link IntersectionPolicy} used for ordering vehicles
     * @return the {@link IntersectionPolicy} used for ordering vehicles
     */
    public IntersectionPolicy getPolicy()
    {
        return policy;
    }
    
    /**
     * Adds a signal {@link Phase} to the {@link IntersectionPolicy} if the policy is a {@link Signalized}
     * @param p the {@link Phase} to be added
     * @see Signalized
     */
    public void addPhase(Phase p)
    {
        if(policy instanceof Signalized)
        {
            ((Signalized)policy).addPhase(p);
        }
    }
    
    /**
     * Updates the cycle offset of the {@link IntersectionPolicy} if the policy is a {@link Signalized}
     * @param o the new offset
     * @see Signalized
     */
    public void setOffset(double o)
    {
        if(policy instanceof Signalized)
        {
            ((Signalized)policy).setOffset(o);
        }
    }
    
    /**
     * Executes one time step of simulation. See the conflict region algorithm.
     * @return the number of exiting vehicles
     */
    public int step()
    {
        Node node = getNode();
        
        policy.initialize(node);
        
        int exited = 0;
        
        int moved = 0;
        
        // if DLR update CR capacity because they shift number of lanes
        if(Network.isDLR())
        {
            updateCRCapacity();
        }
        
        // split to improve performance
        
        Set<Vehicle> vehicles = new TreeSet<Vehicle>(policy);

        Map<Link, Iterator<Vehicle>> inc_flow = new HashMap<Link, Iterator<Vehicle>>();

        for(Link l : node.getIncoming())
        {
            List<Vehicle> temp = l.getSendingFlow();

            l.q = 0;
            l.Q = Math.max(l.getCapacityPerTimestep(), temp.size());
            l.lanes_blocked = 0;
            
            
            Iterator<Vehicle> iter = temp.iterator();

            inc_flow.put(l, iter);

            for(int i = 0; i < l.getDsLanes() && iter.hasNext(); i++)
            {
                Vehicle v = iter.next();

                iter.remove();
                policy.initialize(this, v);
                vehicles.add(v); 
            }
        }

        // initializations

        // update receiving flows
        for(Link l : node.getOutgoing())
        {
            l.R = l.getReceivingFlow();
        }

        for(ConflictRegion cr : allConflicts)
        {
            cr.newTimestep();
        }


        outer: while(!vehicles.isEmpty())
        {
            Iterator<Vehicle> iter = vehicles.iterator();

            while(iter.hasNext())
            {
                Vehicle v = iter.next();
                

                Link i = v.getPrevLink();
                Link j = v.getNextLink();

                if(j == null)
                {
                    i.removeVehicle(v);
                    iter.remove();
                    v.exited();
                    exited++;

                    Iterator<Vehicle> queue = inc_flow.get(i);

                    if(queue.hasNext())
                    {
                        Vehicle v2 = queue.next();
                        queue.remove();

                        policy.initialize(this, v2);
                        vehicles.add(v2); 
                    }

                    continue outer;
                }

                double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                

                double receivingFlow =  i.scaleReceivingFlow(v);

                if(v.getDriver().isAV())
                {
                    TurningMovement movement = conflicts.get(i).get(j);

                    if(j.R >= receivingFlow && hasAvailableCapacity(i, j, equiv_flow))
                    {
                        
                        
                        j.R -= receivingFlow;
                        i.q += equiv_flow;
                        moved++;

                        for(ConflictRegion cr : movement)
                        {
                            cr.update(i, j, equiv_flow);
                        }

                        i.removeVehicle(v);
                        iter.remove();
                        j.addVehicle(v);


                        policy.onAccept(v);

                        Iterator<Vehicle> queue = inc_flow.get(i);

                        if(queue.hasNext())
                        {
                            Vehicle v2 = queue.next();
                            queue.remove();

                            policy.initialize(this, v2);

                            vehicles.add(v2);


                        }

                        continue outer;
                    }
                    else
                    {
                        i.lanes_blocked ++;
                    }
                }
                else
                {
                    boolean reserveAll = true;

                    inner: for(Link j2 : conflicts.get(i).keySet())
                    {
                        if(!(j2.R >= equiv_flow * j2.scaleReceivingFlow(v) && hasAvailableCapacity(i, j2, equiv_flow)))
                        {
                            reserveAll = false;
                            break inner;
                        }
                    }

                    if(reserveAll)
                    {
                        i.q += equiv_flow;
                        moved++;

                        Set<ConflictRegion> updated = new HashSet<ConflictRegion>();


                        for(Link j2 : conflicts.get(i).keySet())
                        {
                            TurningMovement movement = conflicts.get(i).get(j2);
                            j2.R -= equiv_flow * j2.scaleReceivingFlow(v);

                            for(ConflictRegion cr : movement)
                            {
                                if(updated.add(cr))
                                {
                                    cr.update(i, j, equiv_flow);

                                }
                            }
                        }


                        updated = null;

                        i.removeVehicle(v);
                        iter.remove();
                        j.addVehicle(v);

                        policy.onAccept(v);

                        Iterator<Vehicle> queue = inc_flow.get(i);

                        if(queue.hasNext())
                        {
                            Vehicle v2 = queue.next();
                            queue.remove();

                            policy.initialize(this, v2);

                            vehicles.add(v2);
                        }

                        continue outer;

                    }

                }
            }
            break;
        }
        
        return exited;
        
    }
    
    /**
     * Resets this {@link PriorityTBR} to restart simulation.
     */
    public void reset()
    {
        policy.reset();
        super.reset();
    }
    
    
}
