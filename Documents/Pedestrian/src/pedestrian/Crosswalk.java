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
public class Crosswalk 
{
    private Set<Link> crossed;
    private Queue queue;
    
    public Crosswalk(Link... c)
    {
        queue = new Queue();
        crossed = new HashSet<Link>();
        
        for(Link l : c)
        {
            crossed.add(l);
        }
    }
    
    public Queue getQueue()
    {
        return queue;
    }
        
    public Set<Link> getCrossed()
    {
        return crossed;
    }
}
