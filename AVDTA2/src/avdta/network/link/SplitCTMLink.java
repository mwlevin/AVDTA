/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;

/**
 *
 * @author ml26893
 */
public class SplitCTMLink extends CTMLink implements AbstractSplitLink
{
    private TransitLane transitLane;
    
    public SplitCTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes, TransitLane transitLane)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);
        this.transitLane = transitLane;
    }
    
    public TransitLane getTransitLane()
    {
        return transitLane;
    }
}
