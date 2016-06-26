/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.cost;

import avdta.cost.TravelCost;
import avdta.network.link.Link;
import avdta.sav.SAVSimulator;
import avdta.vehicle.Vehicle;

/**
 *
 * @author ut
 */
public class DNLTime extends TravelCost
{
    public DNLTime()
    {
    }
    
    public double cost(Link l, double vot, int time)
    {
        int look_back = SAVSimulator.ast_duration - SAVSimulator.dt;

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
