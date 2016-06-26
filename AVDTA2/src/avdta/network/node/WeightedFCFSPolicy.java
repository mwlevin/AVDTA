/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.Vehicle;

/**
 *
 * @author ml26893
 */
public class WeightedFCFSPolicy extends FCFSPolicy
{
    private DelayWeights weights;
    
    public WeightedFCFSPolicy(DelayWeights w)
    {
        weights = w;
    }
    
    public int compare(Vehicle v1, Vehicle v2)
    {
        if(v1.reservation_time != v2.reservation_time)
        {
            return (int)(100*(getWeight(v1) * v1.reservation_time - getWeight(v2) * v2.reservation_time));
        }
        else if(v1.getNetEnterTime()!= v2.getNetEnterTime())
        {
            return v1.getNetEnterTime() - v2.getNetEnterTime();
        }
        else
        {
            return v1.getId() - v2.getId();
        }
    }
    
    public double getWeight(Vehicle v)
    {
        return weights.getWeight(v.getPrevLink(), v.getNextLink());
    }
}
