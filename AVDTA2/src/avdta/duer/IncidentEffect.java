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
public class IncidentEffect 
{
    private Link link;
    private int lanesOpen;
    private double capacityPerLane;
    
    public IncidentEffect(Link link, int lanesOpen, double capacityPerLane)
    {
        this.link = link;
        this.lanesOpen = lanesOpen;
        this.capacityPerLane = capacityPerLane;
    }
    
    public Link getLink()
    {
        return link;
    }
    
    public int getLanesOpen()
    {
        return lanesOpen;
    }
    
    public double getCapacityPerLane()
    {
        return capacityPerLane;
    }
    
    
}
