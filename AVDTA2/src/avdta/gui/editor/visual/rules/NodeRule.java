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
public interface NodeRule extends Serializable
{
    public Color getColor(Node n);
    public Color getBackColor(Node n);
    public int getRadius(Node n);
    
    public boolean matches(Node n);
    
    public String getName();
}
