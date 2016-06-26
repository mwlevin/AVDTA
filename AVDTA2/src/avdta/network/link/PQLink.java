/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;


/**
 *
 * @author ut
 */
public class PQLink extends LTMLink
{
    public PQLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd = capacity / (jamd - capacity / ffspd), jamd, length, numLanes);
    }
    
    public double getReceivingFlow()
    {
        return getCurrentUpstreamCapacity();
    }
}
