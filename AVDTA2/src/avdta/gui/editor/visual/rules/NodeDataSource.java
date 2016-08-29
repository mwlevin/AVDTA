/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.node.Node;

/**
 *
 * @author ml26893
 */
public interface NodeDataSource 
{
    public double getData(Node n, int t);
    public String getName();
    public double suggestMin();
    public double suggestMax();
}
