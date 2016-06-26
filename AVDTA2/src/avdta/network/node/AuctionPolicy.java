/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Michael
 */
public class AuctionPolicy extends IntersectionPolicy
{
    public AuctionPolicy()
    {
    }
    
    
    
    public void initialize(TBR node, Vehicle vehicle, List<Vehicle> queue)
    {
        vehicle.bid = vehicle.getWallet().bid(vehicle);
       
        
    }
    
    public void initialize(TBR node, Vehicle vehicle)
    {
        vehicle.bid = vehicle.getWallet().bid(vehicle);
    }
    
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
    
    public void onAccept(Vehicle v)
    {
        v.getWallet().pay(v.bid);
    }
}
