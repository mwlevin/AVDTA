/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.fuel;

import avdta.vehicle.fuel.ICV;
import avdta.vehicle.fuel.BEV;
import avdta.vehicle.Vehicle;

/**
 * This class is used for calculating the energy consumption as vehicles travel through the network. 
 * Subclasses should override {@link VehicleClass#calcPower(double, double, double)}.
 * There is no need to instantiate a new {@link VehicleClass} for each {@link Vehicle}.
 * @author Michael
 */
public abstract class VehicleClass 
{
    public static final double E_PER_GALLON = 36.44;
    public static final ICV icv = new ICV();
    public static final BEV bev = new BEV();
    
    /**
     * Returns the {@link VehicleClass} subclass for the given type parameter
     * @param type the vehicle type
     * @return the corresponding {@link VehicleClass}
     */
    public static VehicleClass getVehClass(int type)
    {
        switch(type % 10)
        {
            case 1 : return icv;
            case 2 : return bev;
            default: return icv;
        }
    }
    
    /**
     * Calculates the energy consumption for the given duration with the given speed, acceleration, and road grade. 
     * This appeals to {@link VehicleClass#calcPower(double, double, double)}.
     * @param time the duration (s)
     * @param speed speed (mi/hr)
     * @param accel acceleration (mi/hr/s)
     * @param grade road grade (radians)
     * @return energy consumption
     */
    public double calcEnergy(double time, double speed, double accel, double grade)
    {
        return time / 3600.0 * calcPower(speed, accel, grade);
    }
    
    /**
     * Calculates the MPG for the given duration with the given speed, acceleration, and road grade. 
     * This appeals to {@link VehicleClass#calcPower(double, double, double)}.
     * @param time the duration (s)
     * @param speed speed (mi/hr)
     * @param accel acceleration (mi/hr/s)
     * @param grade road grade (radians)
     * @return energy consumption
     */
    public double calcMPG(double time, double speed, double accel, double grade)
    {

        double dist = speed * time / 3600.0;

        double energy = calcEnergy(time, speed, accel, grade);
        
        return dist / (energy / E_PER_GALLON);
    }
    
    public double testMPG(double speed, double accel, double grade)
    {
        return calcMPG(1.0 / speed * 3600.0, speed, accel, grade);
    }
    
    /**
     * Returns the power required for the given speed, acceleration, and grade
     * @param speed speed (mi/hr)
     * @param accel acceleration (mi/hr/s)
     * @param grade road grade (radians)
     * @return power required
     */
    public abstract double calcPower(double speed, double accel, double grade);
    
    /**
     * Returns a type code for this {@link VehicleClass}
     * @return depends on subclass
     */
    public abstract int getType();
}
