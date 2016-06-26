/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

/**
 *
 * @author Michael
 */
public class Results 
{
    private double mintt, ttt;
    private int num_veh;
    private int exiting;
    
    public Results(double mintt, double ttt, int num_veh, int exiting)
    {
        this.mintt = mintt;
        this.ttt = ttt;
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
    
    // hours
    public double getTSTT()
    {
        return ttt/3600.0;
    }
    
    public double getAEC()
    {
        return (ttt - mintt) / num_veh;
    }
    
    public double getGapPercent()
    {
        return (ttt - mintt) / ttt * 100.0;
    }
    
    public int getTrips()
    {
        return num_veh;
    }
}
