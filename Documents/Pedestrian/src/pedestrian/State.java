/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class State 
{
    private int[] queueLen;
    
    private Map<Action, Map<State, Double>> U;
    
    public double J;
    public Action mu;
    
    public State(Node n)
    {
        queueLen = new int[n.getNumQueues()];
        U = new HashMap<Action, Map<State, Double>>();
    }
    
    public State(Queue[] queues)
    {
        queueLen = new int[queues.length];
        
        for(int i = 0; i < queues.length; i++)
        {
            queueLen[i] = queues[i].getSize();
        }
        U = new HashMap<Action, Map<State, Double>>();
    }
    
    public State(int[] copy)
    {
        this.queueLen = new int[copy.length];
        
        for(int i = 0; i < copy.length; i++)
        {
            queueLen[i] = copy[i];
        }
        U = new HashMap<Action, Map<State, Double>>();
    }
    
    
    public double expJ(Action u)
    {
        double output = 0.0;
        
        Map<State, Double> temp = U.get(u);
        
        for(State x : temp.keySet())
        {
            output += x.J * temp.get(x);
        }
        
        return output;
    }
    
    public Set<Action> getActions()
    {
        return U.keySet();
    }
    
    public void addAction(Action u)
    {
        U.put(u, new HashMap<State, Double>());
    }
    
    public void addTransition(Action u, State next, double prob)
    {
        U.get(u).put(next, prob);
    }
    
    
    
    public String toString()
    {
        String output = "(";
        
        for(int i = 0; i < queueLen.length; i++)
        {
            output += queueLen[i];
            
            if(i < queueLen.length-1)
            {
                output += ",";
            }
        }
        
        output += ")";
        return output;
    }
    public int[] getQueueLengths()
    {
        return queueLen;
    }
    
    public int[] copyQueueLengths()
    {
        int[] copy = new int[queueLen.length];
        
        for(int i = 0; i < copy.length; i++)
        {
            copy[i] = queueLen[i];
        }
        
        return copy;
    }
    
    public void apply(Queue[] queues)
    {
        for(int i = 0; i < queues.length; i++)
        {
            queues[i].setSize(queueLen[i]);
        }
    }
    
    public boolean equals(State rhs)
    {
        for(int i = 0; i < queueLen.length; i++)
        {
            if(queueLen[i] != rhs.queueLen[i])
            {
                return false;
            }
        }
        
        return true;
    }
    
 
    
    public int hashCode()
    {
        int output = 0;
        int mult = 1;
        
        for(int i : queueLen)
        {
            output += i * mult;
            mult *= 10;
        }
        
        return output;
    }
}
