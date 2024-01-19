/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;


/**
 * This is a point queue flow model. It is implemented as a LTMLink with receiving flow set to the upstream capacity.
 * @author Michael
 */
public class PQLink extends LTMLink
{
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
     * @param numLanes the number of lanes
     */
    public PQLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd = capacity / (jamd - capacity / ffspd), jamd, length, numLanes);
    }
    
    /**
     * Returns the receiving flow for this time step
     * @return upstream capacity
     */
    public double getReceivingFlow()
    {
        return getCurrentUpstreamCapacity();
    }
}
