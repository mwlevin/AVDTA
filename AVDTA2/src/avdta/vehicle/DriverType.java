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
    
    public static final DriverType AV = new DriverType("AV", 1, true, true, false);
    public static final DriverType HV = new DriverType("HV", 1, false, false, false);
    public static final DriverType BUS_AV = new DriverType("AV", 1, true, true, true);
    public static final DriverType BUS_HV = new DriverType("HV", 1, false, false, true);
    
    public static final int AV_T = 2;
    public static final int HV_T = 0;
    public static final int CV_T = 1;
    
    
    private double reactionTime;
    private boolean isAV, isCV;
    private String name;
    private boolean isTransit;
    
    /**
     * Instantiates {@code DriverType} with the inputs (name, reaction time, 
     * and if it is an autonomous vehicle).
     * @param name Is either {@code AV} or {@code HV}.
     * @param reactionTime Is the reaction time for given name.
     * @param isAV Boolean indicating if the driver type is an autonomous vehicle
     * @param isCV Boolean indicating if the driver type is a connected vehicle
     * @param isTransit Boolean indicating if the driver is transit
     */
    public DriverType(String name, double reactionTime, boolean isAV, boolean isCV, boolean isTransit)
    {
        this.name = name;
        this.reactionTime = reactionTime;
        this.isAV = isAV;
        this.isTransit = isTransit;
        this.isCV = isCV;
    }
    
    /**
     * Returns whether this driver is a connected vehicle
     * @return whether this driver is a connected vehicle
     */
    public boolean isCV()
    {
        return isCV;
    }
    
    /**
     * Checks whether this class matches the given type code
     * @param type type code
     * @return whether this class matches the given type code
     */
    public boolean matches(int type)
    {
        switch(type)
        {
            case HV_T: return !isAV() && !isCV();
            case AV_T: return isAV();
            case CV_T: return isCV();
            default: return false;
        }
    }
    
    /**
     * Returns the type code
     * @return {@link ReadDTANetwork#AV} or {@link ReadDTANetwork#HV}
     */
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
    
    /**
     * Returns whether this driver is driving transit
     * @return whether this driver is driving transit
     */
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
     * Returns if the driver type is {@code AV}.
     * @return if the driver type is {@code AV}.
     */
    public boolean isAV()
    {
        return isAV;
    }
    /**
     * Returns the reaction time of this driver type
     * @return Returns the reaction time of this driver type (s)
     */
    public double getReactionTime()
    {
        return reactionTime;
    }
    
    /**
     * Updates the reaction time
     * @param t the new reaction time (s)
     */
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
