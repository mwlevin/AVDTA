/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.moves;

import avdta.network.RunningAvg;
import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import java.util.List;

/**
 *
 * @author ml26893
 */
public class LinkData 
{
    private int volume;
    private RunningAvg speed;
    
    public LinkData()
    {
        speed = new RunningAvg();
    }
    
    public double getVolume()
    {
        return volume *3600.0 / (EvaluateLinks.END_TIME - EvaluateLinks.START_TIME );
    }
    
    public double getAvgSpeed()
    {
        return speed.getAverage();
    }
    
    public void addVehicle(Link l, int enter, int exit)
    {
        if(exit < EvaluateLinks.START_TIME || enter > EvaluateLinks.END_TIME)
        {
            return;
        }
        
        double time = (exit - enter)/3600.0;
        double v = l.getLength()/time;
        speed.add(v, time);
        volume++;
    }
}
