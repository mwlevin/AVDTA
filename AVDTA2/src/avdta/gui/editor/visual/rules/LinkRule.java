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
public abstract class LinkRule implements Serializable
{
    public abstract Color getColor(Link l, int t);
    public abstract int getWidth(Link l, int t);
    
    public abstract boolean matches(Link l, int t);
    
    public abstract String getName();
    
    public String toString()
    {
        return getName();
    }
}
