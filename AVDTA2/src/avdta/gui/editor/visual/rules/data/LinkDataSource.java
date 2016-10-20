/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;
import avdta.gui.editor.visual.rules.LinkDataRule;
import avdta.project.Project;

/**
 * The {@link LinkDataSource} class provides an input source for the {@link LinkDataRule}. 
 * The key method is {@link LinkDataSource#getData(avdta.network.link.Link, int)}, which is used for visualization purposes.
 * It also contains a name and description method for the user interface.
 * @author Michael
 */
public abstract class LinkDataSource 
{
    
    public static final LinkDataSource tt = new TTLinkData();
    
    public static final LinkDataSource numLanes = new NumLanesLinkData();
    public static final LinkDataSource capacity = new CapacityLinkData();
    public static final LinkDataSource ffspd = new FFSpdLinkData();
    public static final LinkDataSource volume = new VolumeLinkData();
    
    /**
     * Returns the data used for visualization for the specified {@link Link} at the specified time. 
     * @param l the {@link Link}
     * @param t the time (s)
     * @return the data
     */
    public abstract double getData(Link l, int t);
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public abstract String getName();
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public abstract String getDescription();
    
    /**
     * Returns the name.
     * @return {@link LinkDataSource#getName()}
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
