/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.ReadNetwork;
import avdta.network.link.Link;
import avdta.network.node.policy.FIFOPolicy;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author micha
 */
public class Highway extends IntersectionControl
{
    private IntersectionPolicy policy;
    
    public Highway()
    {
        policy = new FIFOPolicy();
    }
    
    public void initialize(){}
    public void reset(){}
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return true;
    }
    
    public boolean hasConflictRegions()
    {
        return false;
    }
    
    public int getType()
    {
        return ReadNetwork.HIGHWAY;
    }
    
    public Signalized getSignal()
    {
        return null;
    }
    
    public int step()
    {
        Node node = getNode();
        
        policy.initialize(node);
        
        int exited = 0;
        
        int moved = 0;
        

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
                policy.initialize(null, v);
                vehicles.add(v); 
            }
        }

        // initializations

        // update receiving flows
        for(Link l : node.getOutgoing())
        {
            l.R = l.getReceivingFlow();
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

                        policy.initialize(null, v2);
                        vehicles.add(v2); 
                    }

                    continue outer;
                }

                double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                

                double receivingFlow =  i.scaleReceivingFlow(v);


                if(j.R >= receivingFlow)
                {


                    j.R -= receivingFlow;
                    i.q += equiv_flow;
                    moved++;



                    i.removeVehicle(v);
                    iter.remove();
                    j.addVehicle(v);


                    policy.onAccept(v);

                    Iterator<Vehicle> queue = inc_flow.get(i);

                    if(queue.hasNext())
                    {
                        Vehicle v2 = queue.next();
                        queue.remove();

                        policy.initialize(null, v2);

                        vehicles.add(v2);


                    }

                    continue outer;
                }

                
                
            }
            break;
        }
        
        return exited;
        
    }
}
