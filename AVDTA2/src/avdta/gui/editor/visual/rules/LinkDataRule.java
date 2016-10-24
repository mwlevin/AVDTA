/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.gui.editor.visual.rules.data.LinkDataSource;
import avdta.network.link.Link;
import avdta.project.Project;
import java.awt.Color;

/**
 * The {@link LinkDataRule} draws {@link Link}s based on an associated {@link LinkDataSource}.
 * The {@link LinkDataSource} provides some data, which is compared with user-specified minimum and maximum values.
 * Using this data, corresponding width and fill colors are selected.
 * 
 * Note that the {@link LinkDataRule} matches every {@link Link}.
 * @author Michael
 */
public class LinkDataRule extends LinkRule
{
    
    private double minValue, maxValue;
    private LinkDataSource data;
    private int minWidth, maxWidth;
    private Color minColor, maxColor;
    
    /**
     * Constructs an empty {@link LinkDataRule}.
     */
    public LinkDataRule()
    {
        
    }
    
    /**
     * Returns the name of the data source.
     * @return {@link LinkDataSource#getName()}.
     */
    public String getName()
    {
        return data.getName()+" - "+String.format("%.0f to %.0f", minValue, maxValue);
    }
    
    /**
     * Returns the width for the given {@link Link} at the given time, based on comparing {@link LinkDataSource#getData(avdta.network.link.Link, int)} with the minimum and maximum values.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return the width (px)
     */
    public int getWidth(Link l, int t)
    {
        double val = data.getData(l, t);
        
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
     * Returns the color for the given {@link Link} at the given time.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return the color
     */
    public Color getColor(Link l, int t)
    {
        double val = data.getData(l, t);
        
        Color minColor = getMinColor();
        Color maxColor = getMaxColor();
        
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
     * Returns whether this {@link LinkRule} applies to the given {@link Link} at the specified time
     * @param l the {@link Link}
     * @param t the time (s)
     * @return true
     */
    public boolean matches(Link l, int t)
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
    public LinkDataSource getDataSource()
    {
        return data;
    }
    
    /**
     * Updates the data source.
     * @param d the new data source
     */
    public void setDataSource(LinkDataSource d)
    {
        data = d;
    }
    
    /**
     * Updates the minimum width.
     * @param m the new minimum width (px)
     */
    public void setMinWidth(int m)
    {
        minWidth = m;
    }
    
    /**
     * Updates the maximum width.
     * @param m the new maximum width (px)
     */
    public void setMaxWidth(int m)
    {
        maxWidth = m;
    }
    
    /**
     * Updates the minimum width.
     * @return the minimum width
     */
    public int getMinWidth()
    {
        return minWidth;
    }
    
    /**
     * Updates the maximum width.
     * @return the maximum width
     */
    public int getMaxWidth()
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
     * Calls {@link LinkDataSource#initialize(avdta.project.Project)}.
     */
    public void initialize(Project project)
    {
        data.initialize(project);
    }
}
