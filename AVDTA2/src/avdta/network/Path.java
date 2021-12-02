/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.cost.TravelCost;
import avdta.traveler.Traveler;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A route, defined as a list of {@link Link}s with additional functionality.
 * @author Michael
 */
public class Path extends ArrayList<Link> implements Serializable
{
    public static int next_id = 1;
    
    private int id;
    
    private double cost;

    //public Traveler traveler;
    
    public double proportion;
    public double flow;
    
    /**
     * Constructs an empty {@link Path} with an id of -1.
     * {@link Path} ids are set by {@link PathList} after the {@link Path} is added.
     */
    public Path()
    {
        setId();
    }
    
    /**
     * Constructs an empty {@link Path} with the specified id.
     * It is not recommended to use this method as the specified id may infringe on the uniquely generated ids.
     * This method is used by {@link PathList} when reading from a file.
     * @param id the id of the {@link Path}
     */
    public Path(int id)
    {
        this.id = id;
    }
    
    /**
     * Updates the id to a uniquely generated id.
     */
    public void setId()
    {
        id = next_id++;
    }
    
    /**
     * Constructs the {@link Path} with the given {@link Link}s
     * @param links the {@link Link}s to be added
     */
    public Path(Link... links)
    {
        for(Link l : links)
        {
            add(l);
        }
    }
    
    /**
     * Checks whether this {@link Path} passes through the specified {@link Node}
     * @param n the {@link Node} to be checked
     * @return whether this {@link Path} passes through the specified {@link Node}
     */
    public boolean containsNode(Node n)
    {
        if(size() == 0)
        {
            return false;
        }
        
        if(get(0).getSource() == n)
        {
            return true;
        }
        
        for(Link l : this)
        {
            if(l.getDest() == n)
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Update the cost of this {@link Path}
     * @param cost the new cost
     */
    public void setCost(double cost)
    {
        this.cost = cost;
    }
    
    /**
     * Checks whether the given vehicle can use this path
     * @param v the vehicle
     * @return whether the vehicle can use this path
     */
    public boolean isValid(Vehicle v)
    {
        if(size() == 0)
        {
            return true;
        }
        return isValid(v.getDriver());
    }
    
    public boolean isValid(DriverType driver)
    {
        Link i = get(0);
        for(int idx = 1; idx < size(); idx++)
        {
            Link j = get(idx);
            
            if(!i.getDest().canMove(i, j, driver))
            {
                return false;
            }
        }
        
        return true;
    }
    
    
    /**
     * Returns the cost of this {@link Path}
     * @return the cost of this {@link Path}
     */
    public double getCost()
    {
        return cost;
    }
    
    
    /**
     * Checks whether this {@link Path} is identical to another in terms of the {@link Link}s contained.
     * @param o the other {@link Path} to compare with
     * @return whether this {@link Path} is identical to the other
     */
    public boolean equals(Object o)
    {
        Path rhs = (Path)o;
        
        if(rhs.size() != size())
        {
            return false;
        }
        
        for(int i = 0; i < size(); i++)
        {
            if(get(i) != rhs.get(i))
            {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns the {@link Path} id
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return size();
    }
    /**
     * Return the first {@link Node} this {@link Path} passes through
     * @return origin node for this path
     */
    public Node getOrigin()
    {
        return get(0).getSource();
    }
    
    /**
     * Return the last {@link Node} this {@link Path} passes through
     * @return destination node for this path
     */
    public Node getDest()
    {
        return get(size()-1).getDest();
    }
    
    /**
     * Returns the free flow travel time
     * @return total free flow travel time for all links (s)
     * @see Link#getFFTime()
     */
    public double getFFTime()
    {
    	double time = 0;
    	
    	for(Link j : this)
    	{
    		time += j.getFFTime();
    	}
    	
    	return time;
    }
    
    /**
     * Returns the estimated travel time
     * @param dep_time departure time
     * @return estimated travel time given the departure time (s)
     * @see Link#getAvgTT(int)
     */
    public double getAvgTT(int dep_time)
    {
        double time = dep_time;
        
        Link i = null;
        
        for(Link j : this)
        {
            time += j.getAvgTT((int)time);
            
            i = j;
        }
        
        return time - dep_time;
    }
    
    /**
     * Returns the estimated travel time
     * @param dep_time departure time
     * @return estimated travel time given the departure time (s)
     * @see Link#getAvgTT(int)
     */
    public double getStDevTT(int dep_time)
    {
        double time = dep_time;
        double output = 0.0;
        
        Link i = null;
        
        for(Link j : this)
        {
            time += j.getAvgTT((int)time);
            double stdev = j.getStDevTT((int)time);
            output += stdev*stdev;
            
            i = j;
        }
        
        return Math.sqrt(output);
    }
    
    /**
     * Returns the expected cost when using the specified cost function
     * @param dep_time departure time
     * @param vot the value of time
     * @param costFunc the cost function used for calculating the cost
     * @param v the current vehicle - for SPaT detection
     * @return estimated travel cost given departure time and cost function
     */
    public double getAvgCost(int dep_time, double vot, TravelCost costFunc, Vehicle v)
    {
        double time = dep_time;
        double output = 0.0;
        
        Link i = null;
        
        for(Link j : this)
        {
            output += costFunc.cost(j, vot, (int)time, v.getDriver());
            if(j.getDest().getSPaT() && (v.getDriver().isAV() || v.getDriver().isCV())){
                time += j.getAvgTT((int)time) * costFunc.getDiscount;
            } else {
                time += j.getAvgTT((int)time);
            }
            
            
            i = j;
        }
        
        return output;     
    }
    
        /**
     * Returns the expected cost when using the specified cost function
     * @param dep_time departure time
     * @param vot the value of time
     * @param costFunc the cost function used for calculating the cost
     * @return estimated travel cost given departure time and cost function
     */
    public double getAvgCost(int dep_time, double vot, TravelCost costFunc)
    {
        double time = dep_time;
        double output = 0.0;
        
        Link i = null;
        System.out.println("Using the Path.getAvgCost that does not include driver type (SPaT won't work).");
        
        for(Link j : this)
        {
            output += costFunc.cost(j, vot, (int)time);
            time += j.getAvgTT((int)time);
            
            i = j;
        }
        
        return output;     
    }
    
    /**
     * Returns the estimated fuel consumption
     * @param dep_time departure time
     * @return estimated fuel consumption given departure time
     */
    public double getFuel(int dep_time)
    {
        double time = dep_time;
        double output = 0;
        
        Link i = null;
        
        for(Link j : this)
        {
            time += j.getAvgTT((int)time);
            output += j.getAvgFuel((int)time);
            i = j;
        }
        
        return output;
    }
    
    /**
     * Returns the estimated energy consumption
     * @param dep_time departure time
     * @return estimated energy consumption given departure time
     */
    public double getEnergy(int dep_time)
    {
        double time = dep_time;
        double output = 0;
        
        Link i = null;
        
        for(Link j : this)
        {
            time += j.getAvgTT((int)time);
            output += j.getAvgEnergy((int)time);
            i = j;
        }
        
        return output;
    }
    
    /**
     * Returns the total distance traveled (mi)
     * @return total distance (mi)
     */
    public double getLength()
    {
        double output = 0;
        
        for(Link l : this)
        {
            if(!l.isCentroidConnector())
            {
                output += l.getLength();
            }
        }
        
        return output;
    }
    
    public String toString(){
        String s = " ( ";
        for(int i = 0; i < this.size(); i++){
            s += "(";
            s += this.get(i).getSource();
            s += ", ";
            s += this.get(i).getDest();
            s += " ), ";
        }
        s += " )";
        return s;
    }
}
