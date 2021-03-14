/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.node.policy.IntersectionPolicy;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;
import java.util.Collection;
import java.util.List;
import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.network.type.Type;

/**
 * This is an auction policy for reservations. 
 * After reaching an intersection, vehicles place a bid. 
 * Reservations are granted in order of highest bid, and vehicles pay their bid after moving.
 * @author Michael
 */
public class AuctionPolicy extends IntersectionPolicy
{
    /**
     * Initializes {@link Vehicle} priority for the specified {@link Node} and {@link Vehicle}
     * The {@link Vehicle} priority is initialized to be the {@link Vehicle}'s bid.
     * @param node the intersection at which the {@link Vehicle} is waiting.
     * @param vehicle the {@link Vehicle} to be initialized
     */
    public void initialize(TBR node, Vehicle vehicle)
    {
        vehicle.bid = vehicle.getWallet().bid(vehicle);
    }
    
    /**
     * Compares two {@link Vehicle}s according to their reservation priority. {@link Vehicle}s are listed in order of decreasing priority.
     * Priority depends on {@link Vehicle} bids.
     * @param v1 the first vehicle being compared
     * @param v2 the second vehicle being compared
     * @return sorting index for vehicles
     */
    public int compare(Vehicle v1, Vehicle v2)
    {
        if(v1.bid != v2.bid)
        {
           return (int)( (v2.bid - v1.bid)*10000); 
        }
        else if(v1.getNetEnterTime() != v2.getNetEnterTime())
        {
            return v1.getNetEnterTime() - v2.getNetEnterTime();
        }
        else
        {
            return v1.getId() - v2.getId();
        }
    }
    
    /**
     * {@link Vehicle}s pay their bid when their reservation is accepted.
     * @param v the {@link Vehicle} with an accepted reservation
     */
    public void onAccept(Vehicle v)
    {
        v.getWallet().pay(v.bid);
    }
    
    /**
     * Returns the type code associated with this policy
     * @return {@link ReadNetwork#AUCTION}
     */
    public Type getType()
    {
        return ReadNetwork.AUCTION;
    }
}
