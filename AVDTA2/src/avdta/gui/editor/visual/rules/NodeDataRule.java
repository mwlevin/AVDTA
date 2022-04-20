/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.gui.editor.visual.rules.data.NodeDataSource;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.Project;
import java.awt.Color;

/**
 * The {@link NodeDataRule} draws {@link Node}s based on an associated {@link NodeDataSource}.
 * The {@link NodeDataSource} provides some data, which is compared with user-specified minimum and maximum values.
 * Using this data, corresponding radius and fill colors are selected.
 * 
 * Note that the {@link NodeDataRule} matches every {@link Node}.
 * @author Michael
 */
public class NodeDataRule extends NodeRule
{
    private double minValue, maxValue;
    private NodeDataSource data;
    private int minWidth, maxWidth;
    private Color minColor, maxColor;
    
    /**
     * Constructs an empty {@link NodeDataRule}
     */
    public NodeDataRule()
    {
        
    }
    
    /**
     * Returns a name describing the data source.
     * @return {@link NodeDataSource#getName()}
     */
    public String getName()
    {
        return data.getName()+" - "+String.format("%.0f to %.0f", minValue, maxValue);
    }
    
    /**
     * Returns the radius based on calling {@link NodeDataSource#getData(avdta.network.node.Node, int)} and comparing to the minimum and maximum values.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return the scaled radius
     */
    public int getRadius(Node n, int t)
    {
        double val = data.getData(n, t);
        
        if(val < minValue)
        {
            return minWidth;
        }
        else if(val >= maxValue)
        {
            return maxWidth;
        }
        
        
        double scale = (val - minValue) / (maxValue - minValue);
        
        return (int)Math.round(minWidth + scale * (maxWidth - minWidth));
    }
    
    
    /**
     * Returns the color based on calling {@link NodeDataSource#getData(avdta.network.node.Node, int)} and comparing to the minimum and maximum values.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return the scaled color
     */
    public Color getColor(Node n, int t)
    {
        double val = data.getData(n, t);
        
        if(val < minValue)
        {
            return minColor;
        }
        else if(val >= maxValue)
        {
            return maxColor;
        }
        
        int r1 = minColor.getRed();
        int r2 = maxColor.getRed();
        int g1 = minColor.getGreen();
        int g2 = maxColor.getGreen();
        int b1 = minColor.getBlue();
        int b2 = maxColor.getBlue();
        
        double scale = (val - minValue) / (maxValue - minValue);
        
        return new Color((int)Math.round(r1 + scale * (r2 - r1)), 
                (int)Math.round(g1 + scale * (g2 - g1)),
                (int)Math.round(b1 + scale * (b2 - b1)));
    }
    
    /**
     * The {@link NodeDataRule} matches every node.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return true
     */
    public boolean matches(Node n, int t)
    {
        return true;
    }
    
    /**
     * Updates the minimum color.
     * @param m the new minimum color
     */
    public void setMinColor(Color m)
    {
        minColor = m;
    }
    
    /**
     * Updates the maximum color.
     * @param m the new maximum color
     */
    public void setMaxColor(Color m)
    {
        maxColor = m;
    }
    
    /**
     * Returns the minimum color.
     * @return the minimum color
     */
    public Color getMinColor()
    {
        return minColor;
    }
    
    /**
     * Returns the maximum color.
     * @return the maximum color
     */
    public Color getMaxColor()
    {
        return maxColor;
    }
    
    /**
     * Returns the data source.
     * @return the data source
     */
    public NodeDataSource getDataSource()
    {
        return data;
    }
    
    /**
     * Updates the data source.
     * @param d the new data source
     */
    public void setDataSource(NodeDataSource d)
    {
        data = d;
    }
    
    /**
     * Updates the minimum radius.
     * @param m the new minimum radius (px)
     */
    public void setMinRadius(int m)
    {
        minWidth = m;
    }
    
    /**
     * Updates the maximum radius.
     * @param m the new maximum radius (px)
     */
    public void setMaxRadius(int m)
    {
        maxWidth = m;
    }
    
    /**
     * Returns the minimum radius.
     * @return the minimum radius (px)
     */
    public int getMinRadius()
    {
        return minWidth;
    }
    
    /**
     * Returns the maximum radius.
     * @return the maximum radius (px)
     */
    public int getMaxRadius()
    {
        return maxWidth;
    }
    
    /**
     * Updates the minimum value.
     * @param m the new minimum value
     */
    public void setMinValue(double m)
    {
        minValue = m;
    }
    
    /**
     * Updates the maximum value.
     * @param m the new maximum value
     */
    public void setMaxValue(double m)
    {
        maxValue = m;
    }
    
    /**
     * Returns the minimum value.
     * @return the minimum value
     */
    public double getMinValue()
    {
        return minValue;
    }
    
    /**
     * Returns the maximum value.
     * @return the maximum value
     */
    public double getMaxValue()
    {
        return maxValue;
    }
    
    /**
     * Initialize this rule with the given {@link Project}.
     * Calls {@link NodeDataSource#initialize(avdta.project.Project)}.
     */
    public void initialize(Project project)
    {
        data.initialize(project);
    }
}
