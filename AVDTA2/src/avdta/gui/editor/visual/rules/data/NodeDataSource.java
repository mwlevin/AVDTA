/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.gui.editor.visual.rules.NodeDataRule;
import avdta.network.node.Node;
import avdta.project.Project;

/**
 * The {@link NodeDataSource} class provides an input source for the {@link NodeDataRule}. 
 * The key method is {@link NodeDataSource#getData(avdta.network.node.Node, int)}, which is used for visualization purposes.
 * It also contains a name and description method for the user interface.
 * @author Michael
 */
public abstract class NodeDataSource 
{
    public static final NodeDataSource max_delay = new MaxDelayNodeData();
    
    /**
     * Returns the data used for visualization for the specified {@link Node} at the specified time. 
     * @param n the {@link Node}
     * @param t the time (s)
     * @return the data
     */
    public abstract double getData(Node n, int t);
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public abstract String getName();
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public abstract String getDescription();
    
    /**
     * Returns the name.
     * @return {@link LinkDataSource#getName()}
     */
    public String toString()
    {
        return getName();
    }
    
    /**
     * Initialize this rule with the given {@link Project}.
     * @param project the {@link Project}
     */
    public void initialize(Project project){}
}
