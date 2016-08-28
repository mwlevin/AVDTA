/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.link.Link;
import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author micha
 */
public interface LinkRule extends Serializable
{
    public Color getColor(Link l);
    public int getWidth(Link l);
    
    public boolean matches(Link l);
    
    public String getName();
}
