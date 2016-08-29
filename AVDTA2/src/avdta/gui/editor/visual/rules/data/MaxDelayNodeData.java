/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;
import avdta.network.node.Node;

/**
 *
 * @author micha
 */
public class MaxDelayNodeData extends NodeDataSource
{
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
    
    public String getName()
    {
        return "Max. delay";
    }
    
    public String getDescription()
    {
        return "Maximum ratio of observed travel time to free flow travel time among incoming links.";
    }
}
