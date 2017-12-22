/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.gui.editor.EditLink;
import avdta.network.ReadNetwork;
import avdta.network.link.CACCLTMLink;
import avdta.network.link.CTMLink;
import avdta.network.link.CentroidConnector;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.link.SharedTransitCTMLink;
import avdta.network.type.Type;
import java.awt.Color;

/**
 * This matches and displays {@link Link}s of a specific type (e.g. LTM, CTM, etc.).
 * The type codes are specified by {@link EditLink}.
 * @author Michael
 */
public class LinkTypeRule extends LinkRule
{
    private Type type;
    private Color color;
    private int width;
    
    /**
     * Constructs an empty {@link LinkTypeRule} that matches centroid connectors.
     */
    public LinkTypeRule()
    {
        this(ReadNetwork.CENTROID, Color.black, 3);
    }
    
    /**
     * Returns the name of the flow model matched.
     * See {@link EditLink#FLOW_MODELS}.
     * @return the name of the flow model matched.
     */
    public String getName()
    {
        return type.getDescription();
        
    }
    
    /**
     * Constructs a {@link LinkTypeRule} that matches the specified type with the specified color and width.
     * @param type the type code (see {@link EditLink})
     * @param color the color
     * @param width the width (px)
     */
    public LinkTypeRule(Type type, Color color, int width)
    {
        this.type = type;
        this.color = color;
        this.width = width;
    }
    
    /**
     * Returns the type code matched.
     * @return the type code matched
     */
    public Type getType()
    {
        return type;
    }
    
    /**
     * Updates the width to draw matching {@link Link}s.
     * @param width the new width (px)
     */
    public void setWidth(int width)
    {
        this.width = width;
    }
    
    /**
     * Updates the color to draw matching {@link Link}s.
     * @param c the new color
     */
    public void setColor(Color c)
    {
        color = c;
    }
    
    /**
     * Updates the type matched.
     * @param t the new type code matched
     */
    public void setType(Type t)
    {
        type = t;
    }
    
    /**
     * Returns the color for the given {@link Link} at the given time.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return {@link LinkTypeRule#getColor()}
     */
    public Color getColor(Link l, int t)
    {
        return color;
    }
    
    /**
     * Compares the flow model of the {@link Link} to the type code.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return if the {@link Link} matches the specified flow model
     */
    public boolean matches(Link l, int t)
    {
        return l.getType() == type;
        /*
        switch(type)
        {
            case EditLink.CTM:
                return l.getClass().equals(CTMLink.class);
            case EditLink.SHARED_TRANSIT_CTM:
                return l.getClass().equals(SharedTransitCTMLink.class);
            case EditLink.DLR_CTM:
                return l.getClass().equals(DLRCTMLink.class);
            case EditLink.LTM:
                return l.getClass().equals(LTMLink.class);
            case EditLink.CACC_LTM:
                return l.getClass().equals(CACCLTMLink.class);
            case EditLink.CENTROID:
                return l.getClass().equals(CentroidConnector.class);
            default:
                 return false;
        }
        */
    }
    
    /**
     * Returns the width for the given {@link Link} at the given time.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return {@link LinkTypeRule#getWidth()}
     */
    public int getWidth(Link l, int t)
    {
        return width;
    }
    
    /**
     * Returns the color to draw matching {@link Link}s.
     * @return the color
     */
    public Color getColor()
    {
        return color;
    }
    
    /**
     * Returns the width to draw matching {@link Link}s.
     * @return the width (px)
     */
    public int getWidth()
    {
        return width;
    }
}
