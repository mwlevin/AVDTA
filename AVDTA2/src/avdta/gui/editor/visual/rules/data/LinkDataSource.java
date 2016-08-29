/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;

/**
 *
 * @author ml26893
 */
public abstract class LinkDataSource 
{
    public static final LinkDataSource tt = new TTLinkData();
    
    public abstract double getData(Link l, int t);
    public abstract String getName();
    public abstract String getDescription();
    public String toString()
    {
        return getName();
    }
}
