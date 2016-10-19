/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;

/**
 * This data source returns the link free flow speed.
 * @author Michael
 */
public class FFSpdLinkData extends LinkDataSource
{
    /**
     * Returns the free flow speed.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return {@link Link#getFFSpeed()} (mi/hr)
     */
    public double getData(Link l, int t)
    {
        return l.getFFSpeed();
    }
    
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public String getName()
    {
        return "Free flow speed";
    }
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public String getDescription()
    {
        return "Free flow speed (mi/hr)";
    }
}
