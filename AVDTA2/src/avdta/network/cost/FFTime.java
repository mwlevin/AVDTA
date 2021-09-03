/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.cost;

import avdta.network.link.Link;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;

/**
 * This cost function always returns the free flow travel time, and is used to calculate free flow travel times for origin-destination pairs.
 * @author Michael
 */
public class FFTime extends TravelCost
{
    /**
     * Returns the free flow travel time of the {@link Link}.
     * @param l the {@link Link}
     * @param vot the value of time (irrelevant)
     * @param enter the time entering the link (irrelevant)
     * @return {@link Link#getFFTime()}
     */
    public double cost(Link l, double vot, int enter, DriverType driver)
    {
        return l.getFFTime();
    }
     /**
     * Returns the free flow travel time of the {@link Link}.
     * @param l the {@link Link}
     * @param vot the value of time (irrelevant)
     * @param enter the time entering the link (irrelevant)
     * @return {@link Link#getFFTime()}
     */
    public double cost(Link l, double vot, int enter)
    {
        return l.getFFTime();
    }
}
