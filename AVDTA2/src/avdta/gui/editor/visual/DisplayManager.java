/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual;

import avdta.network.link.Link;
import avdta.network.node.Node;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author micha
 */
public interface DisplayManager 
{
    public boolean isDisplayNodes();
    public boolean isDisplayLinks();
    public void setDisplayLinks(boolean l);
    public void setDisplayNodes(boolean l);
    public void setDisplayCentroids(boolean c);
    public boolean isDisplayCentroids();
    
    public Color getColor(Link l);
    public int getWidth(Link l);
    
    public Color getBackColor(Node n);
    public Color getColor(Node n);
    public int getRadius(Node n);
    
    public void save(File file) throws IOException;
    public void open(File file) throws IOException;
}
