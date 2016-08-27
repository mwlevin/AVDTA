/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.link.Link;
import avdta.network.node.Node;
import java.awt.Color;

/**
 *
 * @author micha
 */
public class DefaultDisplayManager implements DisplayManager
{
    private boolean displayLinks, displayNodes, displayCentroids;
    
    public DefaultDisplayManager()
    {
        displayLinks = true;
        displayNodes = true;
        displayCentroids = false;
    }
    
    public void setDisplayCentroids(boolean c)
    {
        displayCentroids = c;
    }
    
    public boolean isDisplayCentroids()
    {
        return displayCentroids;
    }
    
    public void setDisplayNodes(boolean n)
    {
        displayNodes = n;
    }
    
    public void setDisplayLinks(boolean l)
    {
        displayLinks = l;
    }
    
    public boolean isDisplayNodes()
    {
        return displayNodes;
    }
    
    public boolean isDisplayLinks()
    {
        return displayLinks;
    }
    
    public Color getColor(Link l)
    {
        return Color.black;
    }
    
    public int getWidth(Link l)
    {
        return 3;
    }
    
    public Color getColor(Node n)
    {
        return Color.black;
    }
    
    public Color getBackColor(Node n)
    {
        return Color.yellow;
    }
    
    public int getRadius(Node n)
    {
        return 0;
    }
}
