/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import avdta.network.link.Link;

/**
 *
 * @author ml26893
 */
public class State 
{
    private Link link;
    private Incident incident;
    
    public double J;
    public Link mu;
    
    public State(Link link, Incident incident)
    {
        this.link = link;
        this.incident = incident;
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
