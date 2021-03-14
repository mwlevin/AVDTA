/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.network.type.Type;

/**
 * This class implements the max-pressure type link network. 
 * Links are separated into segments (cells) that can be traversed in 1 time step. 
 * There are no jam density constraints.
 * 
 * @author mlevin
 */
public class MPLink extends CTMLink
{
    /*
        Ignore the congested wave speed and jam density constraints
    */
    public MPLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, ffspd, Integer.MAX_VALUE, length, numLanes);
    }
    
    public Type getType()
    {
        return ReadNetwork.MP_LINK;
    }
}
