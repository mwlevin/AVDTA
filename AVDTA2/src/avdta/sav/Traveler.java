/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Simulator;
import avdta.sav.Taxi;
import avdta.sav.SAVDest;
import avdta.sav.SAVOrigin;
import java.io.Serializable;

/**
 *
 * @author Michael
 */
public class Traveler implements Serializable, Comparable<Traveler>
{
    private int id;
    private SAVOrigin origin;
    private SAVDest dest;
    private int dtime;
    
    protected int enter_time, exit_time, etd;
    
    
    private Taxi assigned;
    
    public static final int ALLOWED_DELAY = 300;
    
    public boolean unable;
    
    public Traveler(int id, SAVOrigin origin, SAVDest dest, int dtime)
    {
        this.id = id;
        this.origin = origin;
        this.dest = dest;
        this.dtime = dtime;
        
        etd = Integer.MAX_VALUE;
    }
    
    public void setAssignedTaxi(Taxi t)
    {
        assigned = t;
    }
    
    public Taxi getAssignedTaxi()
    {
        return assigned;
    }
    
    public int compareTo(Traveler rhs)
    {
        if(rhs.dtime != dtime)
        {
            return dtime - rhs.dtime;
        }
        else
        {
            return id - rhs.id;
        }
    }
    
    public void reset()
    {
        enter_time = -1;
        exit_time = -1;
        etd = Integer.MAX_VALUE;
        unable = false;
    }
    
    public int getDepTime()
    {
        return dtime;
    }
    
    public SAVOrigin getOrigin()
    {
        return origin;
    }
    
    public SAVDest getDest()
    {
        return dest;
    }
    
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public boolean isExited()
    {
        return exit_time > 0;
    }
    
    public void enteredTaxi()
    {
        enter_time = Simulator.time;
    }
    
    public void exited()
    {
        exit_time = Simulator.time;
    }
    
    public int getDelay()
    {
        return enter_time - dtime;
    }
    
    public int getTT()
    {
        return exit_time - dtime;
    }
    
    public int getIVTT()
    {
        return exit_time - enter_time;
    }
}
