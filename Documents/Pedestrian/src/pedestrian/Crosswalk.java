/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class Crosswalk extends Queue
{
    private double MIN_WALK_SPEED = 1.5 * 5280 / 3600;
    
    private Set<Link> crossed;
    
    public boolean active;
    
    private double min_duration;

    public Crosswalk(int max, Link... c)
    {
        super(max);
        crossed = new HashSet<Link>();
        
        for(Link l : c)
        {
            crossed.add(l);
        }
        
        min_duration = getWidth() / MIN_WALK_SPEED + 5;
    }
    
    public double getWidth()
    {
        double output = 0.0;
        
        for(Link l : crossed)
        {
            output += l.getWidth();
        }
        
        return output;
    }
    
    public double getMinDuration()
    {
        return min_duration;
    }
        
    public Set<Link> getCrossed()
    {
        return crossed;
    }
    
    public boolean crosses(Link l)
    {
        return crossed.contains(l);
    }
}
