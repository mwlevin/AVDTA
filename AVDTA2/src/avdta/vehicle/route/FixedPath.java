/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.route;

import avdta.vehicle.Vehicle;
import avdta.network.Path;
import avdta.network.link.Link;
import avdta.network.node.Node;

/**
 * This represents a fixed route choice: the {@link Vehicle} chooses a {@link Path} before entering the network, and follows the {@link Path} exactly until reaching the destination.
 * @author Michael
 */
public class FixedPath implements RouteChoice
{
    private Path path;
    
    public int path_idx;
    
    /**
     * Constructs this {@link FixedPath} around the given {@link Path}.
     * @param path the fixed {@link Path} to follow
     */
    public FixedPath(Path path)
    {
        path_idx = -1;
        this.path = path;
    }
    
    /**
     * Returns the fixed {@link Path}.
     * @return the path that was traversed
     */
    public Path getPath()
    {
        return path;
    }
    
    /**
     * Returns the total length of the fixed {@link Path}.
     * Calls {@link Path#getLength()}
     * @return the length traversed (mi)
     */
    public double getLength()
    {
        return path.getLength();
    }
    
    /**
     * Called to reset this {@link RouteChoice} for a new simulation.
     * Updates the path pointer.
     */
    public void exited()
    {
        path_idx++;
    }
    
    /**
     * Called to reset this {@link RouteChoice} for a new simulation.
     */
    public void reset()
    {
        path_idx = -1;
    }
    
    /**
     * Returns the number of remaining {@link Link}s to be traversed, according to the path pointer.
     * @return the number of remaining {@link Link}s to be traversed
     */
    public int getNumRemainingLinks()
    {
        return path.size() - path_idx;
    }
    
    /**
     * Returns the next {@link Link} to be traversed, according to the path pointer.
     * @param curr the current {@link Link}
     * @return the next {@link Link} to be traversed
     */
    public Link getNextLink(Link curr)
    {
        if(path_idx < path.size() - 1)
        {
            return path.get(path_idx+1);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Returns the {@link Link} pointed to by the current path index.
     * @return the current {@link Link}
     */
    public Link getCurrLink()
    {
        if(path_idx >= 0 && path_idx < path.size())
        {
            return path.get(path_idx);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Called when the associated {@link Vehicle} enters the given {@link Link}.
     * Increments the path pointer.
     * Note that the {@link Link} entered must be the next {@link Link} on the fixed {@link Path} to maintain proper tracking.
     * @param l the {@link Link} entered
     */
    public void enteredLink(Link l)
    {
        path_idx++;
    }
    
    /**
     * Returns the origin {@link Node}.
     * This is the source {@link Node} of the first {@link Link} on the {@link Path}
     * @return the origin {@link Node}
     */
    public Node getOrigin()
    {
        return path.get(0).getSource();
    }
    
    /**
     * Returns the destination {@link Node}.
     * This is the destination {@link Node} of the last {@link Link} on the {@link Path}
     * @return the destination {@link Node}
     */
    public Node getDest()
    {
        return path.get(path.size()-1).getDest();
    }
}
