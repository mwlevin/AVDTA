/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.project.Project;
import avdta.vehicle.Vehicle;
import java.util.HashMap;
import java.util.Map;

/**
 * This returns the volume/capacity ratio for each link.
 * @author Michael
 */
public class VolumeLinkData extends LinkDataSource
{
    private Map<Link, Integer> counts;
    
    /**
     * Initialize this {@link VolumeLinkData}.
     */
    public VolumeLinkData()
    {
        counts = new HashMap<Link, Integer>();
    }
    
    /**
     * Initialize this {@link VolumeLinkData} with the given {@link Project}.
     * This calculates the link volumes for all links using {@link Project#getSimulator()} and {@link Vehicle#getPath()}.
     * @param project the {@link Project}
     */
    public void initialize(Project project)
    {
        Simulator sim = project.getSimulator();
        counts.clear();
        
        for(Vehicle v : sim.getVehicles())
        {
            for(Link l : v.getPath())
            {
                if(counts.containsKey(l))
                {
                    counts.put(l, counts.get(l)+1);
                }
                else
                {
                    counts.put(l, 1);
                }
            }
        }
    }
    
    /**
     * Returns the volume/capacity ratio.
     * Note that if links are split (e.g. transit lanes), the capacity returned is NOT the sum of the two connected links' volumes/.capacities.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return volume / capacity 
     */
    public double getData(Link l, int t)
    {
        double count = counts.containsKey(l)? counts.get(l) : 0;
        
        return count/ l.getCapacity();
    }
    
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public String getName()
    {
        return "V/C";
    }
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public String getDescription()
    {
        return "Link volume/capacity";
    }
}
