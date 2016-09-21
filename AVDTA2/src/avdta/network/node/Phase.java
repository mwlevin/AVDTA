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
     * Instantiates the {@link Phase} with the list of turns it allows, the green time 
     * and duration of this {@link Phase}.
     * @param sequence the order this {@link Phase} appears in the signal cycle.
     * @param list List of {@link Turn}s allowed in this {@link Phase}.
     * @param green A double value indicating the green time of this {@link Phase}.
     * @param yellow A double value indicating the yellow time of this {@link Phase}.
     * @param red A double value indicating the all red time of this {@link Phase}.
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
    
    
    /**
     * Instantiates the {@link Phase} with the list of turns it allows, the green time 
     * and duration of this {@link Phase}.
     * @param sequence the order this {@link Phase} appears in the signal cycle
     * @param allowed Array of {@link Turn}s allowed in this {@link Phase}
     * @param green A double value indicating the green time of this {@link Phase}
     * @param yellow A double value indicating the yellow time of this {@link Phase}
     * @param red A double value indicating the all red time of this {@link Phase}
     */
    public Phase(int sequence, Turn[] allowed, double green, double yellow, double red)
    {
        this.sequence = sequence;
        this.allowed = allowed;
        this.green_time = green;
        this.yellow = yellow;
        this.red = red;
    }
    
    /**
     * Clones the specified {@link Phase}
     * @param rhs the {@link Phase} to be cloned
     */
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
    
    /**
     * Updates the {@link Turn}s allowed
     * @param t the new array of {@link Turn}s allowed
     */
    public void setTurns(Turn[] t)
    {
        allowed = t;
    }
    
    /**
     * Updates the sequence in which this {@link Phase} occurs in the signal cycle. {@link Phase}s for the same {@link Node} must have unique sequences.
     * @param seq the new sequence value
     */
    public void setSequence(int seq)
    {
        sequence = seq;
    }
    
    /**
     * Returns the sequence in which this {@link Phase} occurs in the signal cycle. {@link Phase}s for the same {@link Node} must have unique sequences.
     * @return the sequence in which this {@link Phase} occurs in the signal cycle
     */
    public int getSequence()
    {
        return sequence;
    }
    
    /**
     * Returns a clone of this {@link Phase}
     * @return a clone of this {@link Phase}
     */
    public Phase clone()
    {
        return new Phase(this);
    }
    
    /**
     * Returns the all red time
     * @return the all red time
     */
    public double getRedTime()
    {
        return red;
    }
    
    /**
     * Returns the yellow time
     * @return the yellow time
     */
    public double getYellowTime()
    {
        return yellow;
    }
    
    /**
     * Orders the phases depending on the sequence
     * @param rhs the {@link Phase} to be compared against
     * @return which {@link Phase} has a lower sequence
     */
    public int compareTo(Phase rhs)
    {
        return sequence - rhs.sequence;
    }
    
    /**
     * Returns an array of allowed {@link Turn}s
     * @return an array of allowed {@link Turn}s
     */
    public Turn[] getTurns()
    {
        return allowed;
    }
    
    /**
     * Gets the time left for the phase for the current time step
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
    

    /**
     * Updates the yellow time
     * @param y the new yellow time
     */
    public void setYellowTime(double y)
    {
        yellow = y;
    }
    
    public String toString()
    {
        return ""+hashCode();
    }
    
    /**
     * Updates the red time
     * @param r the new red time
     */
    public void setRedTime(double r)
    {
        red = r;
    }
    
    /**
     * Gets start time of the {@link Phase}.
     * @return Returns a double value indicating the start time of the {@link Phase}
     */
    public double getStartTime()
    {
        return start_time;
    }
    
    /**
     * Gets the duration of this {@link Phase}.
     * @return Returns {@link Phase#getGreenTime()}+{@link Phase#getYellowTime()}+{@link Phase#getRedTime()}
     */
    public double getDuration()
    {
        return getGreenTime() + getYellowTime() + getRedTime();
    }
    /**
     * Gets the list of {@link Turn}s allowed in this {@link Phase}.
     * @return Returns an array list of the turn movements allowed in this {@link Phase}.
     */
    public Turn[] getAllowed()
    {
        return allowed;
    }
}
