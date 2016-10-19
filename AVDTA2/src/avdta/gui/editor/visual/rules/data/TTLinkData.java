/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;

/**
 * This data source returns the ratio of travel time to free flow travel time.
 * @author Michael
 */
public class TTLinkData extends LinkDataSource
{
    /**
     * Returns the ratio of travel time to free flow travel time.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return {@link Link#getAvgTT(int)} / {@link Link#getFFTime()}
     */
    public double getData(Link l, int t)
    {
        return l.getAvgTT(t) / l.getFFTime();
    }
    
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public String getName()
    {
        return "Travel time ratio";
    }
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public String getDescription()
    {
        return "Ratio of observed travel time to free flow travel time";
    }
}
