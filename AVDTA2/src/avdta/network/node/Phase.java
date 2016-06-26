/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import java.util.List;

/**
 * This class implements a phase of a traffic signal. <br>
 * It has a start time {@code start_time} to store the time when the phase 
 * starts and then also {@code duration} and {@code green_time} of the phase. 
 * The array {@code Turn[] allowed} defines the link pairs which are allowed to 
 * move during that phase.
 * @author Michael
 */
public class Phase 
{
    private double start_time;
    
    private double duration;
    private double green_time;
    
    private Turn[] allowed;
    /**
     * Instantiates the phase with the list of turns it allows, the green time 
     * and duration of that phase.
     * @param list List of {@link Turn}s allowed in that phase.
     * @param green A double value indicating the green time of that phase.
     * @param duration A double value indicating the duration of that phase.
     */
    public Phase(List<Turn> list, double green, double duration)
    {
        this.allowed = new Turn[list.size()];
        
        for(int i = 0; i < allowed.length; i++)
        {
            allowed[i] = list.get(i);
        }
        
        this.duration = duration;
        this.green_time = green;
    }
    
    public Phase(Turn[] allowed, double green, double duration)
    {
        this.allowed = allowed;
        this.duration = duration;
        this.green_time = green;
    }
    
    public Turn[] getTurns()
    {
        return allowed;
    }
    
    /**
     * Gets the time left for the phase to get over.
     * @param curr_time Is a double value indicating the current time
     * @return Returns the time left for the phase to get over.
     */
    public double getRemainingTime(double curr_time)
    {
        return duration - (curr_time - start_time);
    }
    /**
     * Sets the green time of the phase.
     * @param g Is a double value indicating the green time.
     */
    public void setGreenTime(double g)
    {
        green_time = g;
    }
    /**
     * Sets the start time of the phase.
     * @param d Is a double value indicating the start time.
     */
    public void setStartTime(double d)
    {
        start_time = d;
    }
    /**
     * (Function Overloader) Accepts the current time as input and returns 
     * the amount of green time left in the phase.
     * @param curr_time Current time.
     * @return Returns a double value indicating the amount of green time left 
     * in the phase.
     */
    public double getGreenTime(double curr_time)
    {
        return Math.max(0, green_time - (curr_time - start_time));
    }
    /**
     * Gets the green time. There is a function overloader for this method.
     * @return Returns a double value indicating the green time.
     */
    public double getGreenTime()
    {
        return green_time;
    }
    /**
     * Gets start time of the phase.
     * @return Returns a double value indicating the start time of the phase.
     */
    public double getStartTime()
    {
        return start_time;
    }
    /**
     * Gets the duration of the phase.
     * @return Returns a double value indicating the duration of the phase.
     */
    public double getDuration()
    {
        return duration;
    }
    /**
     * Gets the list of {@link Turn}s allowed in that phase.
     * @return Returns an array list of the turn movements allowed in that phase.
     */
    public Turn[] getAllowed()
    {
        return allowed;
    }
}
