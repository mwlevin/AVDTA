/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.ArrayList;
import java.util.Arrays;
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
    
    private Map<Integer, List<Action>> actions;
    
    public MDP(Node node)
    {
        this.node = node;
        
        createStates();
        
        int count = 0;
        
        
    }
    
    public void createStates()
    {
        states = new HashMap<Integer, List<State>>();
        actions = new HashMap<Integer, List<Action>>();
        
        int[] queueLen = new int[node.getNumQueues()];
        
        createStates(0, queueLen);
        
        int count = 0;
        
        for(int i : states.keySet())
        {
            for(State s : states.get(i))
            {
                List<Action> tempU = node.createActions(s);
                
                for(Action tempu : tempU)
                {
                    Action u = addAction(tempu);
                    s.addAction(u);
                    
                    createTransitions(s, u);
                }
            }
            
            count += states.get(i).size();
        }
        
        actions = null;
        
        System.out.println("States: "+count);
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
    
    
    public void valueIteration(double epsilon)
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
        
        int iteration = 1;
        
        do
        {
            error = 0;
            for(int i : states.keySet())
            {
                for(State x : states.get(i))
                {  
                    double min = Integer.MAX_VALUE;
                    Action best = null;
                    
                    
                    for(Action u : x.getActions())
                    {
                        double temp = node.oneStepCost(x, u) + x.expJ(u);
                        if(temp < min)
                        {
                            min = temp;
                            best = u;
                        }
                    }
                    
                    //int[] nextX = x.copyQueueLengths();
                    
                    double newJ = min;
                    
                    error = Math.max(error, Math.abs(newJ -x.J));
                    
                    /*
                    if(newJ != x.J)
                    {
                        System.out.println(x+" "+x.J+" "+newJ);
                    }
                    
                    System.out.println(x+" "+expJ);
                    */
                    x.J = newJ;
                }
            }
            System.out.println(iteration+"\t"+error);
            
            /*
            for(int i : states.keySet())
            {
                for(State s : states.get(i))
                {
                    System.out.println(s+" "+s.J);
                }
            }
            */
            
            iteration++;
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
                    double temp = node.oneStepCost(x, u) + x.expJ(u);
                    
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
    
    public void createTransitions(State state, Action action)
    {
        int[] dem = new int[node.getNumQueues()];
        
        createTransitions(state, action, 0, dem);
    }
    
    private void createTransitions(State state, Action action, int idx, int[] dem)
    {
        if(idx == dem.length)
        {
            double prob = prob(state, action, dem);
            State next = findState(new State(dem));
            state.addTransition(action, next, prob);
        }
        else
        {
            int max = node.getQueues()[idx].getMax() - (state.getQueueLengths()[idx] - action.getQueueChanges()[idx]);

            for(int i = 0; i < max; i++)
            {
                dem[idx] = i;
                createTransitions(state, action, idx+1, dem);
            }
        }
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
    
    public Action findAction(Action copy)
    {
        int hash = copy.hashCode();
        
        for(Action u : actions.get(hash))
        {
            if(u.equals(copy))
            {
                return u;
            }
        }
        
        return null;
    }
    
    public State addState(State s)
    {
        int hash = s.hashCode();
        
        if(states.containsKey(hash))
        {
            List<State> list = states.get(hash);
            
            for(State x : list)
            {
                if(x.equals(s))
                {
                    return x;
                }
            }
            list.add(s);
            return s;
        }
        else
        {
            List<State> temp = new ArrayList<State>();
            temp.add(s);
            states.put(hash, temp);
            return s;
        }
    }
    
    public Action addAction(Action u)
    {
        int hash = u.hashCode();
        
        if(actions.containsKey(hash))
        {
            List<Action> list = actions.get(hash);
            
            for(Action v : list)
            {
                if(u.equals(v))
                {
                    return v;
                }
            }
            list.add(u);
            return u;
        }
        else
        {
            List<Action> temp = new ArrayList<Action>();
            temp.add(u);
            actions.put(hash, temp);
            return u;
        }
    }
}


