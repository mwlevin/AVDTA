/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.vehicle.DriverType;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class State 
{
    private Link link;
    private Incident incident;
    
    // temporary variables for VI
    public double J;
    public Link mu;
    public Set<Link> U;
    
    public State(Link link, Incident incident)
    {
        this.link = link;
        this.incident = incident;
    }
    
    public Set<Link> getActionSpace(DriverType driver)
    {
        Set<Link> output = new HashSet<Link>();
        
        Node intersection = link.getDest();
        
        for(Link next : intersection.getOutgoing())
        {
            if(intersection.canMove(link, next, driver))
            {
                output.add(next);
            }
        }
        
        return output;
    }
    
    public Link getLink()
    {
        return link;
    }
    
    public Incident getIncident()
    {
        return incident;
    }
    
    public int hashCode()
    {
        return link.getId() + incident.getId();
    }
    
    public boolean equals(Object o)
    {
        State rhs = (State)o;
        
        return link == rhs.link && incident == rhs.incident;
    }
}
