/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.fuel;

import avdta.dta.ReadDTANetwork;
import static avdta.vehicle.fuel.ICV.g;

/**
 *
 * @author micha
 */
public class HEV extends VehicleClass
{
    public String toString()
    {
        return "HEV";
    }

    
    /**
     * Returns the power required for the given speed, acceleration, and grade
     * @param speed speed (mi/hr)
     * @param char_accel acceleration (mi/hr/s)
     * @param grad_angle road grade (radians)
     * @return power required
     */
    public double calcPower(double speed, double char_accel, double grad_angle)
    {

        return 0.0;
    }
    
    /**
     * Returns a type code for this {@link VehicleClass}
     * @return depends on subclass
     */
    public int getType()
    {
        return ReadDTANetwork.HEV;
    }
}
