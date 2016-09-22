/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.cost;

import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.vehicle.Vehicle;

/**
 * This class defines a generalized cost, which is a weighted combination of the link travel time and the toll.
 * @author Michael
 */
public class GenCost extends TravelCost
{

    /**
     * Returns a weighted combination of link travel time and toll.
     * @param l the {@link Link}
     * @param vot the value of time ($/hr)
     * @param enter the time entering the link (s)
     * @return {@link Link#getAvgTT(int)} * vot/3600 + {@link Link#getToll(int)}
     */
    public double cost(Link l, double vot, int enter)
    {
        return l.getAvgTT(enter)*vot/3600.0 + l.getToll(enter);
    }
    
}
