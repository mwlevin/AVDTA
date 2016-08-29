/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.link.Link;
import java.awt.Color;

/**
 *
 * @author ml26893
 */
public class LinkDataRule extends LinkRule
{
    private double minValue, maxValue;
    private LinkDataSource data;
    private int minWidth, maxWidth;
    private Color minColor, maxColor;
    
    public LinkDataRule()
    {
        
    }
    
    public String getName()
    {
        return data.getName();
    }
    
    public int getWidth(Link l, int t)
    {
        double val = Math.max(minValue, Math.min(maxValue, data.getData(l, t)));
        double scale = (val - minValue) / (maxValue - minValue);
        
        return (int)Math.round(minWidth + scale * (maxWidth - minWidth));
    }
    
    public Color getColor(Link l, int t)
    {
        double val = Math.max(minValue, Math.min(maxValue, data.getData(l, t)));
        
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
    
    public boolean matches(Link l, int t)
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
    
    public LinkDataSource getDataSource()
    {
        return data;
    }
    
    public void setDataSource(LinkDataSource d)
    {
        data = d;
    }
    
    public void setMinWidth(int m)
    {
        minWidth = m;
    }
    
    public void setMaxWidth(int m)
    {
        maxWidth = m;
    }
    
    public double getMinWidth()
    {
        return minWidth;
    }
    
    public double getMaxWidth()
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
