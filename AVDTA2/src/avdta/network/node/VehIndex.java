/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;

/**
 *
 * @author ut
 */
public class VehIndex 
{
    public Link link;
    public int order;
    
    public VehIndex(Link link, int order)
    {
        this.link = link;
        this.order = order;
    }
}
