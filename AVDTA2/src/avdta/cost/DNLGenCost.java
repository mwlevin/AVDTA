/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.cost;

import avdta.cost.TravelCost;
import avdta.network.link.Link;
import avdta.network.Simulator;
import avdta.vehicle.Vehicle;

/**
 *
 * @author ut
 */
public class DNLGenCost extends TravelCost
{

    
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
