/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.project.Project;
import avdta.vehicle.Vehicle;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 * This class color-codes links used by buses.
 * This class must be constructed around a project with buses. 
 * If no buses are found, no links will match.
 * 
 * Links that are used by buses will be marked in red.
 * Links that are used by buses, that only have a single lane, may be optionally marked in blue.
 * @author Michael
 */
public class LinkBusRule extends LinkRule
{
    private boolean singleLane;
    private Set<Integer> links;
    
    /**
     * Calls {@link LinkBusRule#LinkBusRule(avdta.project.Project, boolean)} without drawing single lanes in blue.
     * @param project the {@link Project}
     */
    public LinkBusRule(Project project)
    {
        this(project, false);
    }
    
    /**
     * Constructs this {@link LinkBusRule}.
     * @param project the {@link Project}
     * @param singleLane if this is true, {@link Link}s used by buses with a single lane will be drawn in blue
     */
    public LinkBusRule(Project project, boolean singleLane)
    {
        this.singleLane = singleLane;
        links = new HashSet<Integer>();
        
        Simulator sim = project.getSimulator();
        
        for(Vehicle v : sim.getVehicles())
        {
            if(v.isTransit())
            {
                for(Link l : v.getPath())
                {
                    links.add(l.getId());
                }
            }
        }
    }
    
    /**
     * Returns the name describing this rule.
     * @return "Bus links"
     */
    public String getName()
    {
        return "Bus links";
    }
    
    /**
     * Returns the width for the given {@link Link} at the given time.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return 5
     */
    public int getWidth(Link l, int t)
    {
        return 5;
    }
    
    /**
     * Returns the color for the given {@link Link} at the given time.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return {@link Color#RED} or {@link Color#BLUE}, depending on the number of lanes
     */
    public Color getColor(Link l, int t)
    {
        
        if(singleLane && l.getNumLanes() == 1)
        {
            return Color.blue;
        }
        return Color.red;
    }
    
    /**
     * Returns whether this {@link LinkRule} applies to the given {@link Link} at the specified time
     * @param l the {@link Link}
     * @param t the time (s)
     * @return if the {@link Link} is used by buses
     */
    public boolean matches(Link l, int t)
    {
        return links.contains(l.getId());
    }
    
    /**
     * Checks whether the {@link Link} id is stored as one that is used by buses.
     * @param id the id to be checked
     * @return if the {@link Link} is used by buses.
     */
    public boolean contains(int id)
    {
        return links.contains(id);
    }
}
