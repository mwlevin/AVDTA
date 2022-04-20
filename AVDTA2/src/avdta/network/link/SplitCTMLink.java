/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;

/**
 * This is a normal {@link CTMLink} that has a dedicated transit lane
 * @author Michael
 */
public class SplitCTMLink extends CTMLink implements AbstractSplitLink
{
    private TransitLane transitLane;
    
    /**
     * Constructs the link with the given parameters 
     * @param id the link id
     * @param source the source node
     * @param dest the destination node
     * @param capacity the capacity per lane (veh/hr)
     * @param ffspd the free flow speed (mi/hr)
     * @param wavespd the congested wave speed (mi/hr)
     * @param jamd the jam density (veh/mi)
     * @param length the length (mi)
     * @param numLanes the number of lanes (not including the transit lane)
     * @param transitLane the transit lane
     */
    public SplitCTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes, TransitLane transitLane)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);
        this.transitLane = transitLane;
    }
    
    /**
     * Returns the transit lane
     * @return the transit lane
     */
    public TransitLane getTransitLane()
    {
        return transitLane;
    }
}
