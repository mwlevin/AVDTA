/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import java.io.Serializable;

/**
 * This class (a user-defined data type) stores the name ({@code AV} or {@code 
 * HV}), reaction time, and whether the type is {@code AV} or not.
 * @author ut
 */
public class DriverType implements Serializable
{
    
    public static final DriverType AV = new DriverType("AV", 1, true);
    public static final DriverType HV = new DriverType("HV", 1, false);
    
    public static DriverType getDriver(int type)
    {
        if(type >= Vehicle.AV && type < Vehicle.BUS)
        {
            return AV;
        }
        else
        {
            return HV;
        }
    }
    
    private double reactionTime;
    private boolean isAV;
    private String name;
    /**
     * Instantiates {@code DriverType} with the inputs (name, reaction time, 
     * and if it is an autonomous vehicle).
     * @param name Is either {@code AV} or {@code HV}.
     * @param reactionTime Is the reaction time for given name.
     * @param isAV Boolean indicating if the driver type is {@code AV}.
     */
    public DriverType(String name, double reactionTime, boolean isAV)
    {
        this.name = name;
        this.reactionTime = reactionTime;
        this.isAV = isAV;
    }
    /**
     * Integer mapping for {@code AV} and {@code HV}. 
     * @return Returns 0 if an AV.
     */
    public int typeIndex()
    {
        if(isAV)
        {
            return 0;
        }
        else
        {
            return 1;
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
    /**
     * Self-explanatory name.
     * @param name AV or HV.
     * @return Returns the type of driver or null if neither {@code AV} nor 
     * {@code HV}.
     */
    public static DriverType getType(String name)
    {
        if(name.equalsIgnoreCase("AV"))
        {
            return AV;
        }
        else if(name.equalsIgnoreCase("HV"))
        {
            return HV;
        }
        else
        {
            return null;
        }
    }
}
