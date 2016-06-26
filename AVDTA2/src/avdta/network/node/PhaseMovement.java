/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

/**
 * This class is an implementation of a movement in a {@link Phase}. A <b>movement</b> 
 * is basically movement of vehicles through a pair of links (incoming and 
 * outgoing at a node/intersection) in a phase which allows that movement. <br>
 * {@code Q} is the capacity, {@code R} is the receiving flow, {@code q} is the 
 * flow, and {@code S} is the sending flow of that phase movement. <br>
 * <b>It is to be noted that the sending flow is {@code int} while the 
 * receiving flow is {@code double}.</b><br>
 * {@code leftovers}: Since only integer number of vehicles can be made to move 
 * through the intersection while the receiving flows are {@code double}, there 
 * are leftovers from each time-step which needs to be added to the next 
 * time-step.
 * @author Michael
 */
public class PhaseMovement implements java.io.Serializable
{
    protected double Q, R, q;
    protected double leftovers;
    protected int S;
    /**
     * Instantiates a phase movement with zero flow and zero leftovers. 
     */
    public PhaseMovement()
    {
        q = 0;
        leftovers = 0;
    }
    /**
     * Calculates the flow left which needs to be pushed to the next cell/link 
     * in the next time-step.
     * @param flow Returns the flow which has not been pushed in this time-step.
     */
    public void update(double flow)
    {
        q -= flow;
    }
    /**
     * Sets the flow to the input value. Named so because this is the initial 
     * flow and while allocating flow through various movements of the phase, 
     * the value of this variable decreases. 
     * @param q Flow.
     */
    public void setMaxFlow(double q)
    {
        this.q = q;
    }
    /**
     * Add capacity used for the signal.
     * @param Q Capacity to be added to that signal.
     */
    public void addMaxFlow(double Q)
    {
        this.Q += Q;
    }
    /**
     * Checks if there is enough capacity to move that flow.
     * @param flow FLow to be checked if it can be moved.
     * @return Returns a boolean value indicating if {@code q} is enough to move 
     * {@code flow}.
     */
    public boolean hasAvailableCapacity(double flow)
    {
        //return q > 0;
        return q>= flow;
    }
    /**
     * Checks if the flow is restricted by the capacity or the sending flow.
     * @return Returns a boolean indicating whether the flow is being restricted 
     * by the sending flow or the capacity.
     */
    public boolean isLimitedByR()
    {
        return q < S && q < Q;
    }
    /**
     * Resets the flow and the capacity after calculating the leftovers which is
     *  {@code q}.
     */    
    public void newTimestep()
    {
        leftovers = q - (int)q;
        
        Q = 0;
        S = 0;
        R = 0;
        q = 0;
    }
    /**
     * Adds leftovers to the flow {@code q}. <br>
     * {@code leftovers}: Since only integer number of vehicles can be made to 
     * move through the intersection while the receiving flows are 
     * {@code double}, there are leftovers from each time-step which needs to 
     * be added to the next time-step.
     */
    public void addLeftovers()
    {
        q += leftovers;
    }
}

