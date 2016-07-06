/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.dta.ReadDTANetwork;
import java.io.Serializable;

/**
 * This class (a user-defined data type) stores the name ({@code AV} or {@code 
 * HV}), reaction time, and whether the type is {@code AV} or not.
 * @author ut
 */
public class DriverType implements Serializable
{
    
    public static final DriverType AV = new DriverType("AV", 1, true, false);
    public static final DriverType HV = new DriverType("HV", 1, false, false);
    public static final DriverType BUS_AV = new DriverType("AV", 1, true, true);
    public static final DriverType BUS_HV = new DriverType("HV", 1, false, true);
    
    
    
    private double reactionTime;
    private boolean isAV;
    private String name;
    private boolean isTransit;
    
    /**
     * Instantiates {@code DriverType} with the inputs (name, reaction time, 
     * and if it is an autonomous vehicle).
     * @param name Is either {@code AV} or {@code HV}.
     * @param reactionTime Is the reaction time for given name.
     * @param isAV Boolean indicating if the driver type is {@code AV}.
     */
    public DriverType(String name, double reactionTime, boolean isAV, boolean isTransit)
    {
        this.name = name;
        this.reactionTime = reactionTime;
        this.isAV = isAV;
        this.isTransit = isTransit;
    }
    
    public int getType()
    {
        if(isAV)
        {
            return ReadDTANetwork.AV;
        }
        else
        {
            return ReadDTANetwork.HV;
        }
    }
    
    public boolean isTransit()
    {
        return isTransit;
    }
    /**
     * Integer mapping for {@code AV} and {@code HV}. 
     * @return Returns 0 if an AV.
     */
    public int typeIndex()
    {
        if(isAV)
        {
            if(isTransit)
            {
                return 2;
            }
            else
            {
                return 0;
            }
        }
        else
        {
            if(isTransit)
            {
                return 3;
            }
            else
            {
                return 1;
            }
        }
    }
    /**
     * Getting the name of the driver type.
     * @return Returns a String indicating the name of the driver type.
     */
    public String toString()
    {
        return name;
    }
    /**
     * Self-explanatory name.
     * @return Returns a boolean if the driver type is {@code AV}.
     */
    public boolean isAV()
    {
        return isAV;
    }
    /**
     * Self-explanatory name.
     * @return Returns a double value indicating the reaction time of that driver 
     * type.
     */
    public double getReactionTime()
    {
        return reactionTime;
    }
    
    public void setReactionTime(double t)
    {
        reactionTime = t;
    }
    
    /**
     * Get equivalent flow based on speed of that vehicle.
     * @param speed A double value indicating the speed of the vehicle.
     * @return Returns the equivalent flow based on speed.
     */
    public double getEquivFlow(double speed)
    {
        return (speed * reactionTime + Vehicle.vehicle_length) / (speed * HV.getReactionTime() + Vehicle.vehicle_length);
    }
}
