package avdta.network.node;


//import ilog.concert.IloLinearNumExpr;
import avdta.network.link.Link;
import avdta.network.Simulator;
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
    //protected IloLinearNumExpr capacityUseSum;

    public ConflictRegion(double capacity)
    {
        this.capacity = capacity;
        this.R = capacity;

        this.id = id_count++;

        blocked = false;
        
    }
    
    public double getRemainingTimestepFlow()
    {
        return R;
    }

    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }

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

    public void newTimestep()
    {

        if(blocked && R>=0)
        {
            R += capacity * Simulator.dt / 3600.0;
        }
        else
        {
            R -= (int)R;
            R += capacity * Simulator.dt / 3600.0;
        }

        blocked = false;
    }
    
    public void reset()
    {
        R = capacity * Simulator.dt / 3600.0;
        blocked = false;
    }

    public double getRemainingFlow()
    {
        return R;
    }

    public boolean canMove(Link inc, Link out, double flow)
    {
        boolean output = R >= flow*adjustFlow(inc, out);
        blocked = blocked || !output;
        return output;
    }

    public void update(Link inc, Link out, double flow)
    {
        R -= flow*adjustFlow(inc, out);
    }

    public double adjustFlow(Link inc, Link out)
    {
        return capacity / (inc.getCapacityPerLane() * inc.getDsLanes());
    }

    public double getCapacity()
    {
        return capacity;
    }
    
    public void setBlocked()
    {
        blocked = true;
    }
    
    public void setMaxScale(Link i){}
}
