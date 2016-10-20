/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;
import avdta.network.node.Node;

/**
 * This data source returns the maximum travel time ratio on any incoming links to the node.
 * @author Michael
 */
public class MaxDelayNodeData extends NodeDataSource
{
    /**
     * Returns the maximum ratio of travel time to free flow travel time on any incoming links.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return max of {@link Link#getAvgTT(int)} / {@link Link#getFFTime()}
     */
    public double getData(Node n, int t)
    {
        double output = 0.0;
        
        for(Link i : n.getIncoming())
        {
            double temp = i.getAvgTT(t)/i.getFFTime();
            
            if(temp > output)
            {
                output = temp;
            }
        }
        
        return output;
    }
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public String getName()
    {
        return "Max. delay";
    }
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public String getDescription()
    {
        return "Maximum ratio of observed travel time to free flow travel time among incoming links.";
    }
}
