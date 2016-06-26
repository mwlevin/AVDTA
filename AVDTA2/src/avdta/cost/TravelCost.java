/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.cost;

import avdta.network.link.Link;
import avdta.sav.SAVSimulator;
import avdta.vehicle.Vehicle;

/**
 *
 * @author ut
 */
public abstract class TravelCost 
{
    public static final TTCost ttCost = new TTCost();
    
    public static final TravelCost generalizedCost = new GenCost();
    
    public static final TravelCost ffTime = new FFTime();
    
    public static final TravelCost dnlTime = new DNLTime();
    public static final TravelCost dnlGenCost = new DNLGenCost();
    
    
    public abstract double cost(Link l, double vot, int time);
}
