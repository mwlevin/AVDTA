/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.cost.TravelCost;
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
    
    /**
     * Instantiates path with unique id
     */
    public Path()
    {
        this(next_id++);
    }
    
    public Path(int id)
    {
        this.id = id;
    }
    
    public Path(Link... links)
    {
        for(Link l : links)
        {
            add(l);
        }
    }
    
    
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
    
    public void setCost(double cost)
    {
        this.cost = cost;
    }
    
    public double getCost()
    {
        return cost;
    }
    
    
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
    
    public int getId()
    {
        return id;
    }
    
    /**
     * 
     * @return origin node for this path
     */
    public Node getOrigin()
    {
        return get(0).getSource();
    }
    
    /**
     * 
     * @return destination node for this path
     */
    public Node getDest()
    {
        return get(size()-1).getDest();
    }
    
    /**
     * 
     * @return total free flow travel time for all links
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
     * 
     * @param dep_time departure time
     * @return estimated travel time given the departure time
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
     * 
     * @param dep_time departure time
     * @param costFunc
     * @return estimated travel cost given departure time and cost function
     */
    public double getAvgCost(int dep_time, double vot, TravelCost costFunc)
    {
        double time = dep_time;
        double output = 0.0;
        
        Link i = null;
        
        for(Link j : this)
        {
            output += costFunc.cost(j, vot, (int)time);
            time += j.getAvgTT((int)time);
            
            i = j;
        }
        
        return output;     
    }
    
    /**
     * 
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
     * 
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
     * 
     * @return total distance 
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
}
