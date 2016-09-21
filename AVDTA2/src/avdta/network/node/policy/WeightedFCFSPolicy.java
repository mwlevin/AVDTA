/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.ReadNetwork;
import avdta.vehicle.Vehicle;
import avdta.network.link.Link;

/**
 * This is a modified FCFS policy ({@link WeightedFCFSPolicy}) in which intersection delay is weighted by the requested turning movement.
 * This is used in {@link SignalWeightedTBR}, which weights turning movements by the capacity allotted by traffic signal timings.
 * @author Michael
 */
public class WeightedFCFSPolicy extends FCFSPolicy
{
    private DelayWeights weights;
    
    /**
     * Constructs the {@link WeightedFCFSPolicy} with the given {@link DelayWeights} function
     * @param w the {@link DelayWeights} function used to weight waiting times
     */
    public WeightedFCFSPolicy(DelayWeights w)
    {
        weights = w;
    }
    
    /**
     * Compares two {@link Vehicle}s according to their reservation priority. {@link Vehicle}s are listed in order of decreasing priority.
     * Vehicles are prioritized according to waiting time, weighted by the {@link DelayWeights} function.
     * @param v1 the first vehicle being compared
     * @param v2 the second vehicle being compared
     * @return sorting index for vehicles
     */
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
    
    /**
     * Returns the weight given to a vehicle according to the {@link DelayWeights} function. 
     * This requires that the {@link Vehicle#getPrevLink()} is the incoming {@link Link} and the {@link Vehicle#getNextLink()} is the outgoing {@link Link}.
     * @param v the {@link Vehicle} to be weighted
     * @return the weight associated with the {@link Vehicle}'s turning movement
     */
    public double getWeight(Vehicle v)
    {
        return weights.getWeight(v.getPrevLink(), v.getNextLink());
    }
    
    /**
     * Returns the type code associated with this policy
     * @return {@link ReadNetwork#WEIGHTED}
     */
    public int getType()
    {
        return ReadNetwork.WEIGHTED;
    }
}
