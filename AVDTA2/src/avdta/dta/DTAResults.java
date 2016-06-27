/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

/**
 *
 * @author Michael
 */
public class DTAResults 
{
    private double mintt, tstt;
    private int num_veh;
    private int exiting;
    
    public DTAResults(double mintt, double tstt, int num_veh, int exiting)
    {
        this.mintt = mintt;
        this.tstt = tstt;
        this.num_veh = num_veh;
        this.exiting = exiting;
    }
    
    public double getAvgTT()
    {
        return getTSTT() / num_veh;
    }
    
    public int getNonExiting()
    {
        return num_veh - exiting;
    }
    
    public int getExiting()
    {
        return exiting;
    }
    
    public int getNumExiting()
    {
        return exiting;
    }
    
    public double getMinTT()
    {
        return mintt;
    }
    
    // hours
    public double getTSTT()
    {
        return tstt/3600.0;
    }
    
    public double getAEC()
    {
        return (tstt - mintt) / num_veh;
    }
    
    public double getGapPercent()
    {
        return (tstt - mintt) / tstt * 100.0;
    }
    
    public int getTrips()
    {
        return num_veh;
    }
    
    public int getNumVeh()
    {
        return num_veh;
    }
}
