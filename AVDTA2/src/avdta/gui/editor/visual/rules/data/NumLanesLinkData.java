/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;

/**
 * This data source returns the number of lanes.
 * @author Michael
 */
public class NumLanesLinkData extends LinkDataSource
{
    /**
     * Returns the number of lanes.
     * Note that if links are split (e.g. transit lanes), the capacity returned is NOT the sum of the two connected links' numbers of lanes.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return {@link Link#getNumLanes()} 
     */
    public double getData(Link l, int t)
    {
        return l.getNumLanes();
    }
    
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public String getName()
    {
        return "Number of lanes";
    }
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public String getDescription()
    {
        return "Number of lanes on the link";
    }
}
