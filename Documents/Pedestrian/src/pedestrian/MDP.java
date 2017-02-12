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
        
        int count = 0;
        
        for(int i : states.keySet())
        {
            count += states.get(i).size();
        }
        
        System.out.println("States: "+count);
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
                    List<Action> U = node.createActions(x);
                    
                    double min = Integer.MAX_VALUE;
                    Action best = null;
                    
                    
                    for(Action u : U)
                    {
                        double temp = node.oneStepCost(x, u) + expJ(x, u);
                        if(temp < min)
                        {
                            min = temp;
                            best = u;
                        }
                    }
                    
                    int[] nextX = x.copyQueueLengths();
                    
                    double newJ = min;
                    
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
                
                Action mu = null;
                double min = Integer.MAX_VALUE;
                
                for(Action u : actions)
                {
                    double temp = node.oneStepCost(x, u) + expJ(x, u);
                    
                    if(temp < min)
                    {
                        min = temp;
                        mu = u;
                    }
                }
                
                x.mu = mu;
            }
        }
    }
    
    public double expJ(State state, Action action)
    {
        // iterate through possible demands
        double output = 0;
        int[] dem = new int[node.getNumQueues()];
        
        return expJ(state, action, 0, dem);
    }
    
    private double expJ(State state, Action action, int idx, int[] dem)
    {
        if(idx == dem.length)
        {
            return prob(state, action, dem) * transition(state, action, dem).J;
        }
        
        double output = 0.0;
        
        int max = node.getQueues()[idx].getMax() - (state.getQueueLengths()[idx] - action.getQueueChanges()[idx]);
        
        for(int i = 0; i < max; i++)
        {
            dem[idx] = i;
            output += expJ(state, action, idx+1, dem);
        }

        return output;
    }
    
    public double prob(State state, Action action, int[] dem)
    {
        int[] x = state.getQueueLengths();
        int[] u = action.getQueueChanges();
        
        Queue[] queues = node.getQueues();
        
        double output = 1.0;
        
        double duration = action.getDuration();
        
        for(int i = 0; i < dem.length; i++)
        {
            output *= queues[i].getDemand().prob(dem[i], x[i] - u[i], duration, queues[i]);
        }
        
        return output;
    }
    
    public State transition(State state, Action action, int[] dem)
    {
        int[] newX = new int[dem.length];
        int[] x = state.getQueueLengths();
        int[] u = action.getQueueChanges();
        
        for(int i = 0; i < newX.length; i++)
        {
            newX[i] = x[i] - u[i] + dem[i];
        }
        
        return findState(new State(newX));
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


