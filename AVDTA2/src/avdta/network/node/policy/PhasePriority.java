/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.ReadNetwork;
import avdta.network.node.PhasedTBR;
import avdta.network.node.TBR;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.vehicle.Vehicle;
import avdta.network.node.Node;

/**
 * This is the {@link IntersectionPolicy} used with the {@link PhasedTBR} policy. 
 * Vehicles are prioritized according to whether they have a green light during their reservation. 
 * If not, a FCFS policy is used.
 * @author Michael
 */
public class PhasePriority extends IntersectionPolicy
{
    private PhasedTBR control;
    
    /**
     * Constructs this {@link PhasePriority} for the specified {@link PhasedTBR}
     * @param control the {@link PhasedTBR} this {@link PhasePriority} applies to
     */
    public PhasePriority(PhasedTBR control)
    {
        this.control = control;
    }
    
    /**
     * Initialize the priority of the specified {@link Vehicle} for the specified {@link Node}.
     * The initialized priority depends on whether the {@link Vehicle} has a green light during the requested reservation.
     * @param node the {@link TBR} at which the {@link Vehicle} is trying to reserve space
     * @param v the {@link Vehicle} trying to reserve space
     */
    public void initialize(TBR node, Vehicle v)
    {
        if(control.reserveCapacity(v))
        {
            v.efficiency = 10;
        }
        else
        {
            v.efficiency = 1;
        }
    }
    
    /**
     * Compares two {@link Vehicle}s according to their reservation priority. {@link Vehicle}s are listed in order of decreasing priority.
     * Vehicles are sorted according to whether they have a green light, then their reservation priority.
     * @param v1 the first vehicle being compared
     * @param v2 the second vehicle being compared
     * @return sorting index for vehicles
     */
    public int compare(Vehicle v1, Vehicle v2)
    {
        if(v1.efficiency != v2.efficiency)
        {
            return (int)(v2.efficiency - v1.efficiency);  
        }
        else if(v1.reservation_time != v2.reservation_time)
        {
            return v1.reservation_time - v2.reservation_time;
        }
        else if(v1.arr_time != v2.arr_time)
        {
            return v1.arr_time - v2.arr_time;
        }
        else
        {
            return v1.getId() - v2.getId();
        }
    }
    
    /**
     * Returns the type code associated with this policy
     * @return {@link ReadNetwork#PHASED}
     */
    public int getType()
    {
        return ReadNetwork.PHASED;
    }
}
