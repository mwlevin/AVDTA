/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.link.Link;

/**
 * This class is used in a modified FCFS policy ({@link WeightedFCFSPolicy}) in which intersection delay is weighted by the requested turning movement.
 * This interface determines the weights associated with a specified turning movement.
 * @author Michael
 */
public interface DelayWeights 
{
    /**
     * Returns the weight for making the specified turning movement
     * @param i the incoming {@link Link}
     * @param j the outgoing {@link Link}
     * @return the weight for making the specified turning movement
     */
    public abstract double getWeight(Link i, Link j);
}
