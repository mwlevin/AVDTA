/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.link.Link;

/**
 *
 * @author ml26893
 */
public interface LinkDataSource 
{
    public double getData(Link l, int t);
    public String getName();
    public double suggestMin();
    public double suggestMax();
}
