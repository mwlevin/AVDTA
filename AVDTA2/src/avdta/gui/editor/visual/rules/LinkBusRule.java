/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.project.DTAProject;
import avdta.vehicle.Vehicle;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class LinkBusRule extends LinkRule
{
    private Set<Link> links;
    
    public LinkBusRule(DTAProject project)
    {
        links = new HashSet<Link>();
        
        Simulator sim = project.getSimulator();
        
        for(Vehicle v : sim.getVehicles())
        {
            if(v.isTransit())
            {
                for(Link l : v.getPath())
                {
                    links.add(l);
                }
            }
        }
    }
    
    public String getName()
    {
        return "Bus links";
    }
    
    public int getWidth(Link l, int t)
    {
        return 5;
    }
    
    public Color getColor(Link l, int t)
    {
        return Color.red;
    }
    public boolean matches(Link l, int t)
    {
        return links.contains(l);
    }
}
