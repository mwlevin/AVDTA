/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.multiclassnewell;

import avdta.network.link.MulticlassLTMLink;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;


/**
 * a class-proportion region
 * @author micha
 */
public class Region
{
    
    private int lower_b, upper_b;
    
    private double p_av;
    
    private int start_time, end_time;

    
    public int veh_count;
    
    public Region(int start_time, int end_time, int lower_b, int upper_b, double p_av)
    {
        this.upper_b = upper_b;
        this.lower_b = lower_b;
        this.p_av = p_av;
        this.start_time = start_time;
        this.end_time = end_time;
        veh_count = 0;
    }
    
    
    
    
    
    public int getStartTime()
    {
        return start_time;
    }
    public int getEndTime()
    {
        return end_time;
    }
    
    public void setStartTime(int s)
    {
        start_time = s;
    }
    
    public void setEndTime(int e)
    {
        end_time = e;
    }
    
    public void setLowerB(int lower_b)
    {
        this.lower_b = lower_b;
    }
    
    public double getAVProp()
    {
        return p_av;
    }
    
    
    public void setUpperB(int upper_b)
    {
        this.upper_b = upper_b;
    }
    
    public void setAVProp(double p)
    {
        p_av = p;
    }
    
    public int getCCDiff()
    {
        return upper_b - lower_b;
    }
    
    public int getUpperB()
    {
        return upper_b;
    }
    
    public int getLowerB()
    {
        return lower_b;
    }
    
    public double getCapacity(MulticlassLTMLink link)
    {
        return getCapacity(link, p_av);
    }
    
    public static double getCapacity(MulticlassLTMLink link, double p_av)
    {
        double v = link.getFFSpeed();

        double Q = v * 1 / (v * p_av * DriverType.AV.getReactionTime() / 3600.0 + v*(1-p_av) * DriverType.HV.getReactionTime() / 3600.0 + Vehicle.vehicle_length  / 5280);
    
        return Q * link.getQFactor();
    }
    
    public double getCriticalDensity(MulticlassLTMLink link)
    {
        double w = getW(link);
        return (w*link.getJamDensity())/(link.getFFSpeed()+w);
    }
    
    public double getW(MulticlassLTMLink link)
    {
        return getW(link, p_av);
    }
    
    public static double getW(MulticlassLTMLink link, double p_av)
    {
        /*
        return Vehicle.vehicle_length  / 5280 / (
                p_av * DriverType.AV.getReactionTime() / 3600.0 + (1-p_av) * DriverType.HV.getReactionTime() / 3600.0);
                */
        
        double Q = getCapacity(link, p_av);
        double v = link.getFFSpeed();
        double K = link.getJamDensity();
        
        double kc = Q/v;
        double w = Q/(K-kc);
        return w;
    }
    
    public int compareTo(Region rhs)
    {
        if(upper_b < rhs.upper_b)
        {
            return -1;
        }
        else if(upper_b > rhs.upper_b)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
}
