/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.cost;

import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;

/**
 * The {@link TTCost} class uses average travel times from the previous simulation as its estimate for the travel cost.
 * @author Michael
 */
public class TTCost extends TravelCost
{
    /**
     * Returns the average travel time at the specified enter time from the previous simulation.
     * @param l the {@link Link}
     * @param vot the value of time (irrelevant)
     * @param enter the time entering the link (s)
     * @return {@link Link#getAvgTT(int)}
     */
    public double cost(Link l, double vot, int enter, DriverType driver)
    {
        return l.getAvgTT(enter);
    }
    /**
     * Returns the average travel time at the specified enter time from the previous simulation.
     * @param l the {@link Link}
     * @param vot the value of time (irrelevant)
     * @param enter the time entering the link (s)
     * @return {@link Link#getAvgTT(int)}
     */
    public double cost(Link l, double vot, int enter)
    {
        return l.getAvgTT(enter);
    }
}
