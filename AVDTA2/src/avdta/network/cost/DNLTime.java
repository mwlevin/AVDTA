/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.cost;

import avdta.network.cost.TravelCost;
import avdta.dta.DTASimulator;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.Network;
import avdta.vehicle.Vehicle;


/**
 * This cost function is used during a one shot simulation/assignment, and calculates the travel cost based on recent travel times.
 * The look back time is defined as {@link DTASimulator#ast_duration}-{@link Network#dt}.
 * @author Michael
 */
public class DNLTime extends TravelCost
{
    
    /**
     * Returns the travel time {@link DTASimulator#ast_duration}-{@link Network#dt} ago.
     * @param l the {@link Link}
     * @param vot the value of time (irrelevant)
     * @param time the time entering the link (s)
     * @return {@link Link#getAvgTT(int)}, or {@link Link#getFFTime()} near the start of simulation
     */
    public double cost(Link l, double vot, int time)
    {
        int look_back = DTASimulator.ast_duration - Network.dt;

        if(time >= look_back)
        {
            return l.getAvgTT(time - look_back);
        }
        else
        {
            return l.getFFTime();
        }
    }
}
