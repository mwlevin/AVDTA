/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parking;

/**
 *
 * @author micha
 */
public class Action 
{
    public static final Action PARK = new Action(null, null)
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
    
    private Link next;
    private Zone reserved;
    
    public Action(Link next, Zone reserved)
    {
        this.next = next;
        this.reserved = reserved;
    }
    
    public Link getNext()
    {
        return next;
    }
    
    public Zone getReserved()
    {
        return reserved;
    }
    
    public String toString()
    {
        if(next == null)
        {
           return "(null, "+reserved+")";
        }
        else
        {
            return "("+next.getDest()+", "+reserved+")";
        }
    }
}
