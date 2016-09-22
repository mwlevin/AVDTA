/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.cost;

import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.network.Simulator;
import avdta.vehicle.Vehicle;

/**
 * This cost function is used during a one shot simulation/assignment, and calculates the generalized cost based on recent travel times.
 * The look back time is defined as {@link DTASimulator#ast_duration}-{@link Network#dt}.
 * @author Michael
 */
public class DNLGenCost extends TravelCost
{

    /**
     * Returns the generalized cost {@link DTASimulator#ast_duration}-{@link Network#dt} ago.
     * @param l the {@link Link}
     * @param vot the value of time ($/hr)
     * @param time the time entering the link (s)
     * @return {@link Link#getAvgTT(int)} * vot/3600 + {@link Link#getToll(int)}, or {@link Link#getFFTime()} * vot/3600 + {@link LInk#getToll(int)} near the start of simulation
     */
    public double cost(Link l, double vot, int time)
    {
        int look_back = 60*5;

        double avg_time;
        double toll;
        
        if(time >= look_back)
        {
            avg_time = l.getAvgTT(time - look_back);
            toll = l.getToll(time - look_back);
        }
        else
        {
            avg_time = l.getFFTime();
            toll = l.getToll(0);
        }
        
        return avg_time*vot/3600.0 + toll;
    }
}
