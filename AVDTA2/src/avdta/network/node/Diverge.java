/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.DriverType;
import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import avdta.network.node.PhaseMovement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements Diverge intersection control at a node. <br>
 * A diverge is an intersection with an incoming link to a node and more than 
 * one outgoing links from the node. <br>
 * {@code movements} maps a link to a {@link PhaseMovement} through
 * {@link HashMap}. <br>
 * @see IntersectionControl
 * @author ut
 */

// only works if 1 incoming link
public class Diverge extends IntersectionControl
{
    private Map<Link, PhaseMovement> movements;
    /**
     * Instantiates {@code movements} with default values.
     */
    public Diverge()
    {
        movements = new HashMap<Link, PhaseMovement>();
    }
    /**
     * Calls each outgoing link from the diverge and puts their details in
     * {@code movements} map.
     */
    public void reset()
    {
        for(Link l : getNode().getOutgoing())
        {
            movements.put(l, new PhaseMovement());
        }
    }
    /**
     * Calls the {@code reset} method, which fills up {@code movements} with 
     * outgoing links from the diverge.
     */
    public void initialize()
    {
        reset();
    }
    /**
     * Calculates the flows to be moved. <br>
     * Starts a new time-step for each of the outgoing links which involves 
     * reseting flow variables to zero after calculating the {@code leftovers} and 
     * also notes the receiving flow for each of the outgoing links. <br>
     * Checks for each vehicle in {@code sendingFlow} if the current node 
     * is the destination, if not then vehicle count is added to sending FLow 
     * {@code S}. <br>
     * Find the proportion of the sending flow {@code phi} which is leaving the 
     * upstream link. <br>
     * Once the proportion is known the flows can be calculated. <br>
     * Adds leftovers (from the previous time-step) of the outgoing flows to 
     * their flows in the next step.
     * @param sendingFlow List of vehicles which can be sent in the current 
     * time-step.
     */
    public void calculateFlow(List<Vehicle> sendingFlow)
    {
        Node node = getNode();
        
        for(Link j : movements.keySet())
        {
            movements.get(j).newTimestep();
            j.R = j.getReceivingFlow();
        }

        for(Vehicle v : sendingFlow)
        {
            Link j = v.getNextLink();
            
            if(j != null)
            {
                movements.get(j).S ++;
            }
        }
        
        // separate queues
        /*
        for(Link j : movements.keySet())
        {
            PhaseMovement movement = movements.get(j);
            movement.q = Math.min(movement.S, j.R);
            movement.addLeftovers();
            
        }
        */
        
        // split proportions
        
        double phi = 1;
        
        for(Link j : movements.keySet())
        {
            if(movements.get(j).S > 0)
            {
                phi = Math.min(phi, j.R / movements.get(j).S);
            }
        }

        
        for(Link j : movements.keySet())
        {
            PhaseMovement movement = movements.get(j);
            movement.q = movement.S * phi;
            movement.addLeftovers();
        }
        
        
    }
    /**
     * Implement movements at a diverge for a time-step. <br>
     * {@code exited} is for counting the number of vehicles that have exited 
     * the link. <br>
     * Gets vehicles on the incoming link of the diverge to find the sending 
     * flow.
     * Calculates the flow variables for the {@code sending} array list using 
     * {@code calculateFlow(sending)}. <br>
     * For each of the vehicle in the array list check the next link for them 
     * in the route, if the current link is the last link exit them and update 
     * the exited count (also records the simulator time: exit_time = 
     * Simulator.time;}) else calculates the 
     * equivalent flow of the vehicle and if there is available capacity then 
     * move the vehicle to the next link (removing the vehicle from the 
     * incoming link to the outgoing link links of the diverge node) and 
     * update the left flow of the phase movement of the link and also the 
     * receiving flow of the next link.
     * @return Returns the number of vehicle which has exited the network.
     */
    public int step()
    {
        int exited = 0;
        
        Node node = getNode();
        
        Link i = node.getIncoming().iterator().next();
        
        
        List<Vehicle> sending = i.getSendingFlow();
        
        calculateFlow(sending);
        
        for(Vehicle v : sending)
        {
            Link j = v.getNextLink();
            if(j == null)
            {
                i.removeVehicle(v);
                v.exited();
                exited++;
            }
            else
            {
                PhaseMovement movement = movements.get(j);
                double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                
                if(movement.hasAvailableCapacity(equiv_flow))
                {
                    movement.update(equiv_flow);
                    i.removeVehicle(v);
                    j.addVehicle(v);
                    
                    j.R -= equiv_flow;
                }
            }
        }
        
        return exited;
    }
    /**
     * A function to determine if a {@link Vehicle} can move from {@code i} to 
     * {@code j}. <br>
     * {@link DriverType} is required because certain policies may allow only
     * certain type of vehicles to move.
     * @param i An incoming {@link Link} to the diverge.
     * @param j An outgoing {@link Link} from the diverge.
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
     * @return Returns {@code false} as diverges do not have conflict regions.
     */
    public boolean hasConflictRegions()
    {
        return false;
    }
}

