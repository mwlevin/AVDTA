/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.node.Node;

/**
 *
 * @author ml26893
 */
public abstract class NodeDataSource 
{
    public static final NodeDataSource max_delay = new MaxDelayNodeData();
    
    public abstract double getData(Node n, int t);
    public abstract String getName();
    public abstract String getDescription();
    
    public String toString()
    {
        return getName();
    }
}
