/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.gui.editor.EditLink;
import avdta.network.link.CACCLTMLink;
import avdta.network.link.CTMLink;
import avdta.network.link.CentroidConnector;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.link.SharedTransitCTMLink;
import java.awt.Color;

/**
 *
 * @author ml26893
 */
public class LinkTypeRule extends LinkRule
{
    private int type;
    private Color color;
    private int width;
    
    public LinkTypeRule()
    {
        this(EditLink.CENTROID, Color.black, 3);
    }
    
    public String getName()
    {
        return EditLink.FLOW_MODELS[type];
    }
    
    
    public LinkTypeRule(int type, Color color, int width)
    {
        this.type = type;
        this.color = color;
        this.width = width;
    }
    
    public void setWidth(int width)
    {
        this.width = width;
    }
    
    public void setColor(Color c)
    {
        color = c;
    }
    
    public void setType(int t)
    {
        type = t;
    }
    
    public Color getColor(Link l, int t)
    {
        return color;
    }
    
    public boolean matches(Link l, int t)
    {
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
    }
    
    public int getWidth(Link l, int t)
    {
        return width;
    }
    
    public Color getColor()
    {
        return color;
    }
    
    public int getWidth()
    {
        return width;
    }
}
