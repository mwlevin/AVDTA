/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.gui.editor.visual.rules.data.NodeDataSource;
import avdta.network.link.Link;
import avdta.network.node.Node;
import java.awt.Color;

/**
 *
 * @author ml26893
 */
public class NodeDataRule extends NodeRule
{
    private double minValue, maxValue;
    private NodeDataSource data;
    private int minWidth, maxWidth;
    private Color minColor, maxColor;
    
    public NodeDataRule()
    {
        
    }
    
    public String getName()
    {
        return data.getName();
    }
    
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
    
    public Color getColor(Node n, int t)
    {
        return Color.black;
    }
    
    public Color getBackColor(Node n, int t)
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
    
    public boolean matches(Node n, int t)
    {
        return true;
    }
    
    public void setMinColor(Color m)
    {
        minColor = m;
    }
    
    public void setMaxColor(Color m)
    {
        maxColor = m;
    }
    
    public Color getMinColor()
    {
        return minColor;
    }
    
    public Color getMaxColor()
    {
        return maxColor;
    }
    
    public NodeDataSource getDataSource()
    {
        return data;
    }
    
    public void setDataSource(NodeDataSource d)
    {
        data = d;
    }
    
    public void setMinRadius(int m)
    {
        minWidth = m;
    }
    
    public void setMaxRadius(int m)
    {
        maxWidth = m;
    }
    
    public double getMinRadius()
    {
        return minWidth;
    }
    
    public double getMaxRadius()
    {
        return maxWidth;
    }
    
    public void setMinValue(double m)
    {
        minValue = m;
    }
    
    public void setMaxValue(double m)
    {
        maxValue = m;
    }
    
    public double getMinValue()
    {
        return minValue;
    }
    
    public double getMaxValue()
    {
        return maxValue;
    }
}
