/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.node.Node;
import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author micha
 */
public abstract class NodeRule implements Serializable
{
    public abstract Color getColor(Node n, int t);
    public abstract Color getBackColor(Node n, int t);
    public abstract int getRadius(Node n, int t);
    
    public abstract boolean matches(Node n, int t);
    
    public abstract String getName();
    
    public String toString()
    {
        return getName();
    }
}
