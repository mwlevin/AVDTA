/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.ReadNetwork;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.network.Simulator;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;

/**
 * Prioritizes vehicles based on their reservation time (set on initialization, or when a vehicle reaches the front of its lane)
 * @author Michael
 */
public class FCFSPolicy extends IntersectionPolicy
{
    public int compare(Vehicle v1, Vehicle v2)
    {
        if(v1.reservation_time != v2.reservation_time)
        {
            return v1.reservation_time - v2.reservation_time;
        }
        else if(v1.getNetEnterTime()!= v2.getNetEnterTime())
        {
            return v1.getNetEnterTime() - v2.getNetEnterTime();
        }
        else
        {
            return v1.getId() - v2.getId();
        }
    }

    public void initialize(TBR node, Vehicle v)
    {
        if(v.reservation_time < 0)
        {
            v.reservation_time = Simulator.time;
        }
    }
    
    public int getType()
    {
        return ReadNetwork.FCFS;
    }
}
