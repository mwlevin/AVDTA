/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.link.Link;
import avdta.network.node.Node;
import java.awt.Color;
import java.awt.Graphics;

/**
 *
 * @author ml26893
 */
public interface DisplayManager 
{
    public Color getColor(Node n, Map m);
    public Color getColor(Link l, Map m);
    
    public int getWidth(Node n, Map m);
    public int getWidth(Link l, Map m);
}
