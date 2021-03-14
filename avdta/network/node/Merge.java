/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.ReadNetwork;
import avdta.vehicle.DriverType;
import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import avdta.network.node.PhaseMovement;
import avdta.network.type.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements Merge intersection control at a node. <br>
 * A merge is an intersection with more than one incoming links to a node and 
 * only one outgoing link. <br>
 * {@code movements} maps a link to a {@link PhaseMovement} through
 * {@link HashMap}. <br>
 * {@code Q_total} stores the sum of the capacities of the incoming links to
 *  the merge.
 * @see IntersectionControl
 * @author ut
 */

// only works if 1 outgoing link
public class Merge extends IntersectionControl
{
    private Map<Link, PhaseMovement> movements;
    private double Q_total;
    
    /**
     * Instantiates {@code movements} with default values.
     */
    public Merge()
    {
        movements = new HashMap<Link, PhaseMovement>();
    }
    
    /**
     * Returns an int specifying the type code of Signal. Intersections with only 1 outgoing link are automatically classified as {@link Merge}
     * @return {@link ReadNetwork#SIGNAL}
     */
    public Type getType()
    {
        return ReadNetwork.MERGE;
    }
    
    /**
     * Returns the {@link Signalized} version of this intersection. Always returns null, because this is never a {@link Signalized}
     * @return null
     * @see Signalized
     */
    public Signalized getSignal()
    {
        return null;
    }
    
    /**
     * Calls each incoming link to the merge and puts their details in 
     * {@code movements} map; also calculates {@code Q_total}, which is the sum 
     * of the capacities of the incoming links.
     */
    public void reset()
    {
        for(Link l : getNode().getIncoming())
        {
            movements.put(l, new PhaseMovement());
            Q_total += l.getCapacity();
        }
    }
    /**
     * Calls the {@code reset} method, which fills up {@code movements} with 
     * incoming links and calculates the sum of the capacities of the links 
     * entering the merge.
     */
    public void initialize()
    {
        reset();
    }
    /**
     * Calculates the flows to be moved.
     * Starts a new time-step for each of the incoming links which involves 
     * reseting flow variables to zero after setting the {@code leftovers} to 
     * the fractional part of the current flow {@code q} i.e. left flow of the 
     * link from the last time-step. <br>
     * {@code leftovers} is the fractional part of the flow which couldn't move 
     * through the intersection because sending flow is an integer (one cannot 
     * move half a vehicle, for instance). <br>
     * Checks for each vehicle in {@code sendingFlow} if the current node is the 
     * destination, if not then vehicle count is added to sending flow {@code S} 
     * and also to {@code S_rem} (remaining sending flow of all the links 
     * combined) i.e load vehicles into sending flow. <br>
     * In the loop ({@code while(R_rem > 0 && S_rem > 0)}) flow from each 
     * incoming link is moved while the outgoing link can still receive flows 
     * and while there are flow still left to send. <b>Note</b> that the function 
     * {@code calculateFlow} only calculates the flows to be moved, the actual 
     * movement happens in {@code step()}. <br>
     * Adds leftovers (from the previous time-step) of the incoming flows to 
     * their flows in next step.
     * @param sendingFlow List of vehicles which can be sent in the current 
     * time-step.
     */
    public void calculateFlow(List<Vehicle> sendingFlow)
    {
        Node node = getNode();
        
        for(Link i : movements.keySet())
        {
            movements.get(i).newTimestep();
        }
        
        Link j = node.getOutgoing().iterator().next();
        
        j.R = j.getReceivingFlow();
        double myR = j.R;
        double R_rem = j.R;
        double S_rem = 0;
        
        for(Vehicle v : sendingFlow)
        {
            if(v.getNextLink() != null)
            {
                movements.get(v.getPrevLink()).S ++;
                S_rem ++;
            }
        }
        
        while(R_rem > 0.01 && S_rem > 0.01)
        {

            Q_total = 0;
           
            for(Link i : getNode().getIncoming())
            {
                PhaseMovement movement = movements.get(i);

                if(movement.q < movement.S)
                {
                    Q_total += i.getCapacity();
                }
            }
            
            for(Link i : movements.keySet())
            {
                PhaseMovement movement = movements.get(i);
                
                if(movement.q < movement.S)
                {
                    double q_add = Math.min(movement.S - movement.q, i.getCapacity() / Q_total * R_rem); 
                    //double q_add = Math.min(movement.S - movement.q, i.getCapacity() / Q_total * myR);
                    movement.q += q_add;    //negative q coming in the result; needs to be checked
                    R_rem -= q_add;
                    S_rem -= q_add;
                    
                }
            }
        }
        
        
        for(Link i : movements.keySet())
        {
            movements.get(i).addLeftovers();
            
        }
    }
    /**
     * Implement movements at a merge for a time-step.
     * {@code exited} is for counting the number of vehicles that have exited 
     *  the link. <br>
     * Get vehicles on each incoming link of the merge and then add them to 
     * {@code sending} array list. <br>
     * Calculate the flow variables for this {@code sending} array list using 
     * {@code  calculateFlow(sending)}. <br>
     * For each of the vehicle in the array list check the next link for them 
     * in the route, if the current link is the last link exit them and update 
     * the exited count (also records the simulator time: {@code exit_time = 
     * Simulator.time;}) else calculate the equivalent flow of the vehicle and 
     * if there is available capacity then move the vehicle to the next link 
     * (removing the vehicle from the incoming links to the outgoing link of the 
     * merge node) and update the left flow of the phase movement of the link 
     * and also the receiving flow of the next link.
     * @return Returns the number of vehicle which has exited the network.
     */
    public int step()
    {
        //int count = 0;
        int exited = 0;
        
        Node node = getNode();
       
        List<Vehicle> sending = new ArrayList<Vehicle>();
        
        for(Link i : node.getIncoming())
        {
            for(Vehicle v : i.getSendingFlow())
            {
                sending.add(v);
            }
        }
        
        calculateFlow(sending);
        //Link k = null;
        for(Vehicle v : sending)
        {
            Link i = v.getPrevLink();

            Link j = v.getNextLink();

            if(j == null)
            {
                i.removeVehicle(v);
                v.exited();
                exited++;
            }
            else
            {
                PhaseMovement movement = movements.get(i);
                double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                
                if(movement.hasAvailableCapacity(equiv_flow))
                {
                    movement.update(equiv_flow);
                    i.removeVehicle(v);
                    j.addVehicle(v);
                    
                    j.R -= equiv_flow;
                    //if(i.getId() == 7629)
                    //     count++;
                }
            }
        }
        //if(k!=null)
        //    System.out.println("total vehicles moved="+count);
        return exited;
    }
    /**
     * A function to determine if a {@link Vehicle} can move from {@code i} to 
     * {@code j}. <br>
     * {@link DriverType} is required because certain policies may allow only
     * certain type of vehicles to move.
     * @param i An incoming {@link Link} to the merge.
     * @param j An outgoing {@link Link} from the merge.
     * @param driver For knowing the {@link DriverType}.
     * @return A boolean indicating if the vehicle can move from link i to link 
     * j.
     */
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return true;
    }
    /**
     * A function to determine if there is a conflict region (for 
     * <b>TBR</b> based policy; TBR stands for Tile Based Reservation).
     * @return Returns {@code false} as merges do not have conflict regions.
     */
    public boolean hasConflictRegions()
    {
        return false;
    }
}

