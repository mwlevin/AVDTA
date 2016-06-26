/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.cost;

import avdta.network.link.Link;
import avdta.vehicle.Vehicle;

/**
 *
 * @author ml26893
 */
public class FFTime extends TravelCost
{
    public double cost(Link l, double vot, int enter)
    {
        return l.getFFTime();
    }
}
