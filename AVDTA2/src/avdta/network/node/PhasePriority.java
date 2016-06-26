/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.Vehicle;

/**
 *
 * @author Michael
 */
public class PhasePriority extends IntersectionPolicy
{
    private PhasedTBR control;
    
    public PhasePriority(PhasedTBR control)
    {
        this.control = control;
    }
    
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
    
    // borrow vehicle efficiency from MCKS
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
}
