/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual;

import avdta.gui.editor.visual.DisplayManager;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.Project;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author micha
 */
public class DefaultDisplayManager implements DisplayManager
{
    private boolean displayLinks, displayNodes, displayCentroids, displayNonCentroids;
    private boolean enabled;
    
    public DefaultDisplayManager()
    {
        displayLinks = true;
        displayNodes = false;
        displayCentroids = false;
        displayNonCentroids = true;
        enabled = true;
    }
    
    public DefaultDisplayManager clone()
    {
        DefaultDisplayManager output = new DefaultDisplayManager();
        
        clone_setOptions(output);
        
        return output;
    }
    
    protected void clone_setOptions(DefaultDisplayManager disp)
    {
        disp.displayLinks = displayLinks;
        disp.displayNodes = displayNodes;
        disp.displayCentroids = displayCentroids;
        disp.displayNonCentroids = displayNonCentroids;
        disp.enabled = enabled;
    }
    
    public void setEnabled(boolean e)
    {
        enabled = e;
    }
    
    public boolean isEnabled()
    {
        return enabled;
    }
    
    public void setDisplayCentroids(boolean c)
    {
        displayCentroids = c;
    }
    
    public void setDisplayNonCentroids(boolean c)
    {
        displayNonCentroids = c;
    }
    
    public boolean isDisplayCentroids()
    {
        return displayCentroids;
    }
    
    public boolean isDisplayNonCentroids()
    {
        return displayNonCentroids;
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
    
    public Color getColor(Link l, int t)
    {
        return l.isSelected()? Color.red : Color.black;
    }
    
    public int getWidth(Link l, int t)
    {
        return 3;
    }
    
    public Color getColor(Node n, int t)
    {
        return Color.black;
    }
    
    public Color getBackColor(Node n, int t)
    {
        return Color.yellow;
    }
    
    public int getRadius(Node n, int t)
    {
        return 0;
    }
    
    public boolean hasSpecialDisplay(Link l, int t)
    {
        return false;
    }
    
    public void save(File file) throws IOException {}
    public void open(File file, Project project) throws IOException {}
    
    public void initialize(Project project){}
}
