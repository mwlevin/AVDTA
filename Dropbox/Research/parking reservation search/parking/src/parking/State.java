/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parking;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author micha
 */
public class State 
{
    public static final State PARK = new State(null, 0, null)
    {
        public int hashCode()
        {
            return 0;
        }
        public String toString()
        {
            return "PARK";
        }
    };
    
    private Node next;
    private int tau;
    private Zone reserved;
    
    private boolean isOrigin;
    
    protected double J;
    protected Action mu_star;
    
    private Map<Action, Transition> actions;
    
    public State(Node next, int tau, Zone reserved)
    {
        this.next = next;
        this.tau = tau;
        this.reserved = reserved;
        
        actions = new HashMap<Action, Transition>();
        this.isOrigin = false;
    }
    
    public void setOrigin(boolean o)
    {
        isOrigin = o;
    }
    
    public boolean isOrigin()
    {
        return isOrigin;
    }
    
    public int hashCode()
    {
        return next.getId()*100*1000 + reserved.getId()*100 + tau;
    }
    
    public void setU(Map<Action, Transition> U)
    {
        actions = U;
    }
    
    public void add(Action u, Transition f)
    {
        actions.put(u, f);
    }
    
    public Map<Action, Transition> getU()
    {
        return actions;
    }
    
    public Node getNext()
    {
        return next;
    }
    
    public int getTimeRem()
    {
        return tau;
    }
    
    public Zone getReserved()
    {
        return reserved;
    }
    
    public String toString()
    {
        return "("+getNext()+", "+getTimeRem()+", "+getReserved()+")";
    }
}
