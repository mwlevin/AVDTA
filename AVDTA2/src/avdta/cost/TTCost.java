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
public class TTCost extends TravelCost
{
    public double cost(Link l, double vot, int enter)
    {
        return l.getAvgTT(enter);
    }
}
