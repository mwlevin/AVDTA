/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;

/**
 *
 * @author micha
 */
public class TTLinkData extends LinkDataSource
{
    public double getData(Link l, int t)
    {
        return l.getAvgTT(t) / l.getFFTime();
    }
    
    public String getName()
    {
        return "Travel time";
    }
    
    public String getDescription()
    {
        return "Ratio of observed travel time to free flow travel time";
    }
}
