/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.vehicle.DriverType;

/**
 *
 * @author micha
 */
public class TransitLane extends CTMLink
{
    public TransitLane(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length)
    {
        super(id, source, dest,capacity, ffspd, wavespd, jamd, length, 1);
    }
    
    public boolean canUseLink(DriverType driver)
    {
        return driver.isTransit();
    }
    
    public int getType()
    {
        return ReadNetwork.CTM + ReadNetwork.TRANSIT_LANE;
    }
}
