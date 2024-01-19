/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;
import avdta.network.type.Type;

/**
 * This data source returns the link capacity.
 * @author Michael
 */
public class CapacityLinkData extends LinkDataSource
{
    /**
     * Returns the total capacity (not per lane).
     * Note that if links are split (e.g. transit lanes), the capacity returned is NOT the sum of the two connected links' capacities.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return {@link Link#getCapacity()} (veh/hr)
     */
    public double getData(Link l, int t)
    {
        return l.getCapacity();
    }
    
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public String getName()
    {
        return "Capacity";
    }
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public String getDescription()
    {
        return "Link capacity (veh/hr)";
    }
}
