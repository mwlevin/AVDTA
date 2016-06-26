/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import avdta.network.Simulator;
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
 *
 * @author ut
 */
public class PriorityTBR extends TBR
{
    private IntersectionPolicy policy;

    
    public PriorityTBR()
    {
        this(null, IntersectionPolicy.FCFS);
    }
    
    public PriorityTBR(IntersectionPolicy policy)
    {
        this(null, policy);
    }
    
    public PriorityTBR(Intersection n)
    {
        this(n, IntersectionPolicy.FCFS);
    }
   
    public PriorityTBR(Intersection n, IntersectionPolicy policy)
    {
        super(n);
        
        this.policy = policy;
    }
    
    public void setPolicy(IntersectionPolicy policy)
    {
        this.policy = policy;
    }
    
    

    public int step()
    {
        Node node = getNode();
        
        policy.initialize(node);
        
        int exited = 0;
        
        int moved = 0;
        
        // if DLR update CR capacity because they shift number of lanes
        if(Simulator.isDLR())
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



                if(v.getDriver().isAV())
                {
                    TurningMovement movement = conflicts.get(i).get(j);

                    if(hasAvailableCapacity(i, j, equiv_flow))
                    {
                        
                        
                        j.R -= equiv_flow;
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
                        if(!hasAvailableCapacity(i, j2, equiv_flow))
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
                            j2.R -= 1;

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
    
    public void reset()
    {
        policy.reset();
        super.reset();
    }
    
    
}
