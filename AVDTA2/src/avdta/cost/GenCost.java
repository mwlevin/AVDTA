/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.cost;

import avdta.cost.TravelCost;
import avdta.network.link.Link;
import avdta.vehicle.Vehicle;

/**
 *
 * @author ut
 */
public class GenCost extends TravelCost
{

    
    public double cost(Link l, double vot, int enter)
    {
        return l.getAvgTT(enter)*vot/3600.0 + l.getToll(enter);
    }
    
}
