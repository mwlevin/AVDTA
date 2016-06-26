/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.Vehicle;
import java.util.List;

/**
 *
 * @author Michael
 */
public class RandomPolicy extends AuctionPolicy
{
    public RandomPolicy()
    {
        
    }
    
    public void initialize(Vehicle vehicle)
    {
        vehicle.bid = (Math.random()*Integer.MAX_VALUE);
    }

}
