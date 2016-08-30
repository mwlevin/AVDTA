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
public class Phase implements Comparable<Phase>
{
    private double start_time;
    
    private double green_time, yellow, red;
    
    private int sequence;
    
    private Turn[] allowed;
    /**
     * Instantiates the phase with the list of turns it allows, the green time 
     * and duration of that phase.
     * @param list List of {@link Turn}s allowed in that phase.
     * @param green A double value indicating the green time of that phase.
     */
    public Phase(int sequence, List<Turn> list, double green, double yellow, double red)
    {
        this.sequence = sequence;
        this.allowed = new Turn[list.size()];
        
        for(int i = 0; i < allowed.length; i++)
        {
            allowed[i] = list.get(i);
        }
        
        this.yellow = yellow;
        this.red = red;
        this.green_time = green;
    }
    
    
    
    public Phase(int sequence, Turn[] allowed, double green, double yellow, double red)
    {
        this.sequence = sequence;
        this.allowed = allowed;
        this.green_time = green;
        this.yellow = yellow;
        this.red = red;
    }
    
    public Phase(Phase rhs)
    {
        this.sequence = rhs.sequence;
        allowed = new Turn[rhs.allowed.length];
        
        for(int i = 0; i < allowed.length; i++)
        {
            allowed[i] = rhs.allowed[i];
        }
        
        green_time = rhs.green_time;
        red = rhs.red;
        yellow = rhs.yellow;
        start_time = rhs.start_time;
    }
    
    public void setTurns(Turn[] t)
    {
        allowed = t;
    }
    
    public void setSequence(int seq)
    {
        sequence = seq;
    }
    
    public Phase clone()
    {
        return new Phase(this);
    }
    
    public double getRedTime()
    {
        return red;
    }
    
    public double getYellowTime()
    {
        return yellow;
    }
    
    public int compareTo(Phase rhs)
    {
        return sequence - rhs.sequence;
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
        return getDuration() - (curr_time - start_time);
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
    

    
    public void setYellowTime(double y)
    {
        yellow = y;
    }
    
    public String toString()
    {
        return ""+hashCode();
    }
    
    public void setRedTime(double r)
    {
        red = r;
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
        return green_time + yellow + red;
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
