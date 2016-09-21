package avdta.network.node;


import ilog.concert.IloLinearNumExpr;
import avdta.network.link.Link;
import avdta.network.Network;
import java.io.Serializable;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Michael
 */
public class ConflictRegion implements Serializable
{
    
    private double capacity;
    public double R;
    
    public double tempR;

    public boolean blocked;


    // hidden ID for hashing
    private int id;
    private static int id_count = 0;
    
    // IP
    public IloLinearNumExpr capacityUseSum;
    
    /**
     * Constructs this ConflictRegion with the specified capacity
     * @param capacity 
     */
    public ConflictRegion(double capacity)
    {
        this.capacity = capacity;
        this.R = capacity;

        this.id = id_count++;

        blocked = false;
        
    }
    
    /**
     * 
     * @return the remaining flow for this time step
     */
    public double getRemainingTimestepFlow()
    {
        return R;
    }

    /**
     * Updates the capacity of this ConflictRegion
     * @param capacity 
     */
    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }

    /**
     * 
     * @return a String containing the id 
     */
    public String toString()
    {
        return ""+id;
    }

    public int hashCode()
    {
        return id;
    }
    
    public int getId()
    {
        return id;
    }

    /**
     * Resets this ConflictRegion for the next time step
     * This updates the capacity, and, if the ConflictRegion was blocked the last time step, saves some of the previous time step's capacity.
     * This prevents situations in which vehicles are blocked by lack of ConflictRegion capacity and lack of Link receiving flow on alternative time steps.
     * The blocked check is reset every time step and activated when a vehicle cannot move through this ConflictRegion.
     */
    public void newTimestep()
    {

        if(blocked && R>=0)
        {
            R += capacity * Network.dt / 3600.0;
        }
        else
        {
            R -= Math.max(0, (int)R);
            R += capacity * Network.dt / 3600.0;
        }

        blocked = false;
    }
    
    /**
     * Resets this ConflictRegion to restart the simulation
     */
    public void reset()
    {
        R = capacity * Network.dt / 3600.0;
        blocked = false;
    }
    
    /**
     * 
     * @return the remaining time step flow
     */
    public double getRemainingFlow()
    {
        return R;
    }

    /**
     * This checks whether there is sufficient remaining flow to move a vehicle which requires the specified flow. If not, this ConflictRegion is marked as blocked.
     * 
     * @param inc the incoming Link
     * @param out the outgoing Link
     * @param flow equivalent flow of the vehicle. This may be reduced for autonomous vehicles due to lower following headways.
     * @return whether a vehicle with the specified equivalent flow can move from the incoming link to the outgoing link
     */
    public boolean canMove(Link inc, Link out, double flow)
    {
        boolean output = R >= flow*adjustFlow(inc, out);
        blocked = blocked || !output;
        return output;
    }

    /**
     * Moves a vehicle requiring the specified flow
     * @param inc the incoming link. This is used for adjusting the flow required.
     * @param out the outgoing link
     * @param flow equivalent flow of the vehicle. This may be reduced for autonomous vehicles due to lower following headways.
     */
    public void update(Link inc, Link out, double flow)
    {
        R -= flow*adjustFlow(inc, out);
    }

    /**
     * 
     * @param inc the incoming link
     * @param out the outgoing link
     * @return adjust the required flow for vehicles turning from the specified incoming and outgoing links.
     */
    public double adjustFlow(Link inc, Link out)
    {
        return capacity / (inc.getCapacityPerLane() * inc.getDsLanes());
    }

    /**
     * 
     * @return the capacity of this ConflictRegion
     */
    public double getCapacity()
    {
        return capacity;
    }
    
    /**
     * Marked this ConflictRegion as blocked for the current time step
     */
    public void setBlocked()
    {
        blocked = true;
    }
    
    public void setMaxScale(Link i){}
}
