/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.priority;

import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;

/**
 *
 * @author micha
 */
public class DepTime implements Priority
{
    public int compare(Vehicle v1, Vehicle v2)
    {
        int dtime1 = 0;
        int dtime2 = 0;
        
        if(v1 instanceof PersonalVehicle)
        {
            dtime1 = ((PersonalVehicle)v1).getDepTime();
        }
        
        if(v2 instanceof PersonalVehicle)
        {
            dtime2 = ((PersonalVehicle)v2).getDepTime();
        }
        
        if(dtime1 != dtime2)
        {
            return dtime1 - dtime2;
        }
        else
        {
            return v1.getId() - v2.getId();
        }
    }
}
