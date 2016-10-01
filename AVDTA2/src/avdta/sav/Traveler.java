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
    
    private int enter_time, exit_time, etd;
    
    
    private Taxi assigned;
    
    public static final int ALLOWED_DELAY = 300;
    
    public boolean unable;
   
    /**
     * Constructs the traveler with the specified parameters.
     * @param id the id
     * @param origin the origin
     * @param dest the destination
     * @param dtime the departure time
     */
    public Traveler(int id, SAVOrigin origin, SAVDest dest, int dtime)
    {
        this.id = id;
        this.origin = origin;
        this.dest = dest;
        this.dtime = dtime;
        
        etd = Integer.MAX_VALUE;
    }
    
    /**
     * Inform the traveler that a taxi has been assigned to him.
     * @param t the taxi
     */
    public void setAssignedTaxi(Taxi t)
    {
        assigned = t;
    }
    
    /**
     * Returns the taxi assigned to this traveler.
     * @return the assigned taxi, or null if none exists
     */
    public Taxi getAssignedTaxi()
    {
        return assigned;
    }
    
    public int getEtd()
    {
        return etd;
    }
    
    public void setEtd(int e)
    {
        etd = e;
    }
    
    /**
     * Orders travelers according to departure time, then id.
     * @param rhs the traveler to be compared against
     * @return traveler order
     */
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
    
    /**
     * Resets the traveler to restart the simulation.
     */
    public void reset()
    {
        enter_time = -1;
        exit_time = -1;
        etd = Integer.MAX_VALUE;
        unable = false;
    }
    
    /**
     * Returns the traveler's departure time.
     * @return the traveler's departure time
     */
    public int getDepTime()
    {
        return dtime;
    }
    
    /**
     * Returns the origin.
     * @return the origin
     */
    public SAVOrigin getOrigin()
    {
        return origin;
    }
    
    /**
     * Returns the destination.
     * @return the destination
     */
    public SAVDest getDest()
    {
        return dest;
    }
    
    /**
     * Returns the id.
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    /**
     * Checks whether this traveler has exited.
     * @return if this traveler has exited
     */
    public boolean isExited()
    {
        return exit_time > 0;
    }
    
    /**
     * This method is called when the traveler enters a taxi.
     * The current time is saved as the taxi enter time.
     */
    public void enteredTaxi()
    {
        enter_time = Simulator.time;
    }
    
    /**
     * This method is called when the traveler exits a taxi.
     * The exit time is saved as the traveler exit time.
     */
    public void exited()
    {
        exit_time = Simulator.time;
    }
    
    /**
     * This returns the traveler waiting time.
     * The waiting time is the difference between the departure time and taxi enter time.
     * @return traveler waiting time
     */
    public int getDelay()
    {
        return enter_time - dtime;
    }
    
    /**
     * This returns the total travel time.
     * The total travel time is the difference between the exit time and departure time.
     * @return the total travel time
     */
    public int getTT()
    {
        return exit_time - dtime;
    }
    
    /**
     * This returns the in-vehicle travel time.
     * The in-vehicle travel time is the difference between the exit time and taxi enter time.
     * @return the in-vehicle travel time.
     */
    public int getIVTT()
    {
        return exit_time - enter_time;
    }
}
