/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author micha
 */
public class MDP 
{
    private Node node;
    
    private Map<Integer, List<State>> states;
    
    public MDP(Node node)
    {
        this.node = node;
        states = new HashMap<Integer, List<State>>();
        
        createStates();
    }
    
    public void createStates()
    {
        int[] queueLen = new int[node.getNumQueues()];
        
        createStates(0, queueLen);
    }
    
    private void createStates(int idx, int[] queueLen)
    {
        if(idx == node.getNumQueues())
        {
            addState(new State(queueLen));
        }
        else
        {
        
            Queue queue = node.getQueues()[idx];

            for(int i = 0; i <= queue.getMax(); i++)
            {
                queueLen[idx] = i;
                createStates(idx+1, queueLen);
            }
        }
    }
    
    private static final double epsilon = 0.001;
    
    public void valueIteration()
    {
        for(int i : states.keySet())
        {
            for(State s : states.get(i))
            {
                s.J = 0;
                s.mu = null;
            }
        }
        
        double error = 0;
        
        do
        {
            for(int i : states.keySet())
            {
                for(State x : states.get(i))
                {
                    double newJ = 0;
                    
                    List<Action> actions = node.createActions(x);
                    
                    error = Math.max(error, Math.abs(newJ -x.J));
                    x.J = newJ;
                }
            }
        }
        while(error > epsilon);
        
        for(int i : states.keySet())
        {
            for(State x : states.get(i))
            {
                List<Action> actions = node.createActions(x);
            }
        }
    }
    
    public State findState(State copy)
    {
        int hash = copy.hashCode();
        
        for(State s : states.get(hash))
        {
            if(s.equals(copy))
            {
                return s;
            }
        }
        
        return null;
    }
    
    public void addState(State s)
    {
        int hash = s.hashCode();
        
        if(states.containsKey(hash))
        {
            states.get(hash).add(s);
        }
        else
        {
            List<State> temp = new ArrayList<State>();
            temp.add(s);
            states.put(hash, temp);
        }
    }
}


