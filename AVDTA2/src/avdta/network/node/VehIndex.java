/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import avdta.vehicle.Vehicle;

/**
 * This class is used to track {@link Vehicle} orders on a {@link Link}.
 * 
 * @author Michael
 */
public class VehIndex 
{
    public Link link;
    public int order;
    
    /**
     * Constructs this {@link VehIndex} with the specified {@link Link} and order
     * @param link the {@link Link} this {@link VehIndex} applies to
     * @param order the {@link Vehicle} position in the queue
     */
    public VehIndex(Link link, int order)
    {
        this.link = link;
        this.order = order;
    }
}
