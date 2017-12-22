/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.link.Link;
import java.awt.Color;
import java.io.Serializable;
import avdta.gui.editor.MapViewer;
import avdta.network.type.Type;
import avdta.project.Project;

/**
 * This is an abstract rule used for visualization purposes.
 * The {@link LinkRule} works as follows: for each {@link Link}, {@link MapViewer} will scan through a list of {@link LinkRule}s until it finds a rule that matches (see {@link LinkRule#matches(avdta.network.link.Link, int)}).
 * Each {@link Link} is displayed as set of line segments. 
 * They are offset right and left to represent directed links.
 * The width is specified by {@link LinkRule#getWidth(avdta.network.link.Link, int)}.
 * The color is specified by {@link LinkRule#getColor(avdta.network.link.Link, int)}.
 * @author Michael
 */
public abstract class LinkRule implements Serializable
{
    /**
     * Returns the color for the given {@link Link} at the given time.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return the color
     */
    public abstract Color getColor(Link l, int t);
    
    /**
     * Returns the width for the given {@link Link} at the given time.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return the width (px)
     */
    public abstract int getWidth(Link l, int t);
    
    /**
     * Returns whether this {@link LinkRule} applies to the given {@link Link} at the specified time
     * @param l the {@link Link}
     * @param t the time (s)
     * @return if this {@link LinkRule} applies
     */
    public abstract boolean matches(Link l, int t);
    
    /**
     * Returns the name for this {@link LinkRule}. 
     * This is used for display purposes.
     * @return the name
     */
    public abstract String getName();
    
    /**
     * Returns a {@link String} representing this {@link LinkRule}
     * @return {@link LinkRule#getName()}
     */
    public String toString()
    {
        return getName();
    }
    
    /**
     * Initialize this rule with the given {@link Project}.
     * @param project the {@link Project}
     */
    public void initialize(Project project){}
}
