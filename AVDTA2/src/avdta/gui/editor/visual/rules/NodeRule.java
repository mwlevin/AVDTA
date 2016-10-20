/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.node.Node;
import java.awt.Color;
import java.io.Serializable;
import avdta.gui.editor.MapViewer;
import avdta.project.Project;

/**
 * This is an abstract rule used for visualization purposes.
 * The {@link NodeRule} works as follows: for each {@link Node}, {@link MapViewer} will scan through a list of {@link NodeRule}s until it finds a rule that matches (see {@link NodeRule#matches(avdta.network.node.Node, int)}).
 * Each {@link Node} is displayed as a filled circle. 
 * The radius is specified by {@link NodeRule#getRadius(avdta.network.node.Node, int)}.
 * The border color is specified by {@link NodeRule#getColor(avdta.network.node.Node, int)}, and the fill color is specified by {@link NodeRule#getBackColor(avdta.network.node.Node, int)}.
 * 
 * @author Michael
 */
public abstract class NodeRule implements Serializable
{
    /**
     * Returns the border color for the given {@link Node} at the given time.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return the border color
     */
    public abstract Color getColor(Node n, int t);
    
    /**
     * Returns the fill color for the given {@link Node} at the given time.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return {@link NodeRule#getColor(avdta.network.node.Node, int)}
     */
    public Color getBackColor(Node n, int t)
    {
        return getColor(n, t);
    }
    
    /**
     * Returns the radius for the given {@link Node} at the given time.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return the radius (px)
     */
    public abstract int getRadius(Node n, int t);
    
    /**
     * Returns whether this {@link NodeRule} applies to the given {@link Node} at the specified time
     * @param n the {@link Node}
     * @param t the time (s)
     * @return if this {@link NodeRule} applies
     */
    public abstract boolean matches(Node n, int t);
    
    /**
     * Returns the name for this {@link NodeRule}. 
     * This is used for display purposes.
     * @return the name
     */
    public abstract String getName();
    
    /**
     * Returns a {@link String} representing this {@link NodeRule}
     * @return {@link NodeRule#getName()}
     */
    public String toString()
    {
        return getName();
    }
    
    /**
     * Initialize this rule with the given {@link Project}.
     */
    public void initialize(Project project){}
}
