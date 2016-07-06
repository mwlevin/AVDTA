/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.node.policy.IntersectionPolicy;
import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.vehicle.DriverType;
import avdta.network.link.Link;
import avdta.network.Simulator;
import avdta.vehicle.Vehicle;
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

// new class for efficiency ~
public class StopSign extends IntersectionControl
{
    private static final IntersectionPolicy policy = IntersectionPolicy.FCFS;
    
    private Map<Link, Map<Link, Double>> capacities;
    
    private static final double braking_deceleration = 15; // feet per second
    
    private static final double alpha_straight = 1.883 * 3.2808399; 
    private static final double beta_straight = -0.021 * 3.2808399;
    private static final double alpha_turn = 1.646 * 3.2808399;
    private static final double beta_turn = -0.017 * 3.2808399;
    
    private ConflictRegion cr; // single conflict region
    
    public StopSign()
    {
        this(null);
    }
    
    public StopSign(Intersection n)
    {
        super(n);
        
        
        cr = new ConflictRegion(0)
        {
            public double adjustFlow(Link i, Link j)
            {
                if(capacities.get(i).containsKey(j))
                {
                    //return getCapacity() / capacities.get(i).get(j);
                    return Math.min(cr.getCapacity() * Network.dt / 3600.0, getCapacity() / capacities.get(i).get(j));
                }
                else
                {
                    return 1;
                }
            }
        };
    }
    
    public int getType()
    {
        return ReadNetwork.STOPSIGN;
    }
    
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return capacities.get(i).containsKey(j);
    }
    
    
    public static double findT(boolean straight, double disp)
    {
        // binary search
        double bot = 0;
        double top = 7;
        
        double mid = 0;
        
        double error = 0.01;
        
        while(top - bot > error)
        {
            mid = (top + bot)/2;
            
            double d = disp(straight, mid) - disp;
            // less than 0: more time
            // greater than 0: less time
            if(d < error)
            {
                bot = mid;
            }
            else if(d > error)
            {
                top = mid;
            }
            else
            {
                return mid;
            }
        }
        
        return mid;
        
    }
    
    public static double disp(boolean straight, double t)
    {
        if(straight)
        {
            return alpha_straight * (Math.pow(Math.E, beta_straight * t) - 1)/ (beta_straight * beta_straight) - alpha_straight * t / beta_straight;
        }
        else
        {
            return alpha_turn * (Math.pow(Math.E, beta_turn * t) - 1)/ (beta_turn * beta_turn) - alpha_turn * t / beta_turn;
        }
    }
    
    
    public void initialize()
    {
        capacities = new HashMap<Link, Map<Link, Double>>();

        double lane_width = 12;
        
        double max_capacity = 0;
        
        Node node = getNode();
        
        // all flow is bounded by maximum of outgoing links capacity
        for(Link i : node.getIncoming())
        {
            capacities.put(i, new HashMap<Link, Double>());
            
            for(Link j : node.getOutgoing())
            {
                if(i.getSource() == j.getDest())
                {
                    continue;
                }
                
                double theta = j.getDirection() - i.getDirection();
                double capacity = 0;
                double disp = 0;
                double crossingTime = 0;
                
                if(theta < 0)
                {
                    theta += 2*Math.PI;
                }
                
                // if less than 45 degree difference, straight
                if(theta < Math.PI/4.0 || theta > 7.0/4.0 * Math.PI)
                {
                    // displacement is sum of cross lane widths
                    double disp_inc = 0;
                    double disp_out = 0;
                    
                    for(Link i2 : node.getIncoming())
                    {
                        if(i == i2 || i.getSource() == j.getDest())
                        {
                            continue;
                        }
                        
                        disp_inc += i2.getNumLanes() * lane_width;
                    }
                    
                    for(Link j2 : node.getOutgoing())
                    {
                        if(j == j2 || j.getDest() == i.getSource())
                        {
                            continue;
                        }
                        
                        disp_out += j2.getNumLanes() * lane_width;
                    }
                    
                    disp = Math.max(disp_inc, disp_out);
                    
                    
                    capacity = 3600.0 / findT(true, disp);
                }
                // left turn
                else if(theta < Math.PI)
                {
                    // 1/4 circumference of ellipse with axes opposite direction links + 1/2 lane width
                    double a = lane_width / 2;
                    double b = lane_width / 2;
                    
                    for(Link i2 : node.getIncoming())
                    {
                        if(i2.getSource() == j.getDest())
                        {
                            a += i2.getNumLanes() * lane_width;
                            break;
                        }
                    }
                    
                    for(Link j2 : node.getOutgoing())
                    {
                        if(j2.getDest() == i.getSource())
                        {
                            b += j2.getNumLanes() * lane_width;
                            break;
                        }
                    }
                    
                    disp = 1.0/4 * Math.PI * (3 * (a+b) - Math.sqrt((3*a + b) * (a + 3*b)));
                    
                    capacity = 3600.0 / findT(false, disp);
                }
                // right turn
                else
                {
                    // 1/4 circumference of circle with radius 1/2 lane width
                    disp = Math.PI/2.0 * lane_width / 2;
                    
                    
                    capacity = 3600.0 / findT(false, disp);
                }
                
                
                
                capacity *= Math.min(i.getNumLanes(), j.getNumLanes());
                // capacity is not greater than link capacities
                capacity = Math.min(capacity, Math.min(i.getCapacity(), j.getCapacity()));
                
                capacities.get(i).put(j, capacity);
                max_capacity = Math.max(capacity, max_capacity);
            }
            
        }
        
        cr.setCapacity(max_capacity);
        
        //sanityCheck();
    }
    
    public void sanityCheck()
    {
        Node node = getNode();
        
        for(Link i : node.getIncoming())
        {
            for(Link j : node.getOutgoing())
            {
                if(!capacities.get(i).containsKey(j))
                {
                    continue;
                }
                double flow = cr.adjustFlow(i, j);
                
                if(flow > cr.getCapacity() * Network.dt / 3600.0)
                {
                    System.out.println(getNode().getId()+" "+flow+" "+(cr.getCapacity() * Network.dt / 3600.0));
                }
            }
        }
    }
    
    public void reset()
    {
        cr.reset();
    }
    
    public int step()
    {
        Node node = getNode();
        

        int exited = 0;
        TreeSet<Vehicle> vehicles = new TreeSet<Vehicle>(policy);

        Map<Link, Iterator<Vehicle>> inc_flow = new HashMap<Link, Iterator<Vehicle>>();

        for(Link l : node.getIncoming())
        {
            List<Vehicle> temp = l.getSendingFlow();

            l.q = 0;
            Iterator<Vehicle> iter = temp.iterator();

            inc_flow.put(l, iter);

            for(int i = 0; i < l.getNumLanes() && iter.hasNext(); i++)
            {
                Vehicle v = iter.next();
                iter.remove();

                vehicles.add(v); 
            }
        }


        // initializations

        // update receiving flows
        for(Link l : node.getOutgoing())
        {
            l.R = l.getReceivingFlow();
        }

        cr.reset();
        
        
        
        outer: while(!vehicles.isEmpty())
        {
            Iterator<Vehicle> iter = vehicles.iterator();

            while(iter.hasNext())
            {
                Vehicle v = iter.next();

                Link i = v.getPrevLink();
                
                
                if(Simulator.time - v.cell_enter < getMinimumDelay(i))
                {
                    continue;
                }
                
                Link j = v.getNextLink();

                if(j == null)
                {
                    i.S--;
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
                double receivingFlow = equiv_flow * j.scaleReceivingFlow(v);
                
                if((i.isCentroidConnector() || j.isCentroidConnector()) && j.R >= receivingFlow)
                {
                    j.R -= receivingFlow;
                    i.q += 1;
                    i.S--;
                    
                    i.removeVehicle(v);
                    iter.remove();
                    j.addVehicle(v);
                }
                else if(j.R >= receivingFlow && hasAvailableCapacity(i, j, equiv_flow))
                {
                    j.R -= receivingFlow;
                    i.q += 1;
                    i.S--;

                    cr.update(i, j, equiv_flow);
                    
                    i.removeVehicle(v);
                    iter.remove();
                    j.addVehicle(v);
                    

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
    
    public boolean requires2Timesteps(Link i, Link j)
    {
        return cr.adjustFlow(i, j) > cr.getCapacity() * Network.dt / 3600.0;
    }
    
    public boolean hasAvailableCapacity(Link i, Link j, double flow)
    {
        return cr.canMove(i, j, flow);
    }
        
    public boolean hasConflictRegions()
    {
        return true;
    }
    
    public int getMinimumDelay(Link i)
    {
        // minimum time from entering link to exiting
        // TT + FF speed / deceleration of 9m/s^2
        
        return (int)Math.ceil(Network.dt+ i.getFFSpeed() / (braking_deceleration / 5280 * 3600));
        //return 0;
    }
}
