/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.fuel;

import avdta.vehicle.fuel.ICV;
import avdta.vehicle.fuel.BEV;

/**
 *
 * @author ut
 */
public abstract class VehicleClass 
{
    public static final double E_PER_GALLON = 36.44;
    public static final ICV icv = new ICV();
    public static final BEV bev = new BEV();
    
    public static VehicleClass getVehClass(int type)
    {
        switch(type % 10)
        {
            case 1 : return icv;
            case 2 : return bev;
            default: return icv;
        }
    }
    
    public double calcEnergy(double time, double speed, double accel, double grade)
    {
        return time / 3600.0 * calcPower(speed, accel, grade);
    }
    
    public abstract double calcPower(double speed, double accel, double grade);
    
    public static VehicleClass getType(String name)
    {
        if(name.equalsIgnoreCase("icv"))
        {
            return icv;
        }
        else if(name.equalsIgnoreCase("bev"))
        {
            return bev;
        }
        else
        {
            return null;
        }
    }
}
