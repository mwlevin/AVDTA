/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.vehicle.Vehicle;
import java.util.List;
import avdta.network.node.Node;

/**
 *
 * @author Michael
 */
public class RandomPolicy extends AuctionPolicy
{
    /**
     * Initializes vehicle priority for the specified {@link Node} and {@link Vehicle}.
     * The vehicle priority is initialized to be a random value.
     * 
     * @param vehicle the {@link Vehicle} to be initialized
     */
    public void initialize(Vehicle vehicle)
    {
        vehicle.bid = (Math.random()*Integer.MAX_VALUE);
    }
    
    /**
     * Do nothing
     * @param v the {@link Vehicle} with an accepted reservation
     */
    public void onAccept(Vehicle v)
    {
    }

}
