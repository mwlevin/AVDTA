/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.link.Link;
import avdta.network.node.Node;
import java.awt.Color;

/**
 *
 * @author ml26893
 */
public class DefaultDisplayManager implements DisplayManager
{
    public Color getColor(Node n, Map m)
    {
        return Color.black;
    }
    public Color getColor(Link l, Map m)
    {
        return Color.black;
    }
    
    public int getWidth(Node n, Map m)
    {
        return 5;
    }
    public int getWidth(Link l, Map m)
    {
        return 3;
    }
}
