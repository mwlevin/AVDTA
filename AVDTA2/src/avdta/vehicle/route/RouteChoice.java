/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.route;

import avdta.duer.Incident;
import avdta.vehicle.Vehicle;
import avdta.network.Path;
import avdta.network.link.Link;
import avdta.network.node.Node;

/**
 * This class represents a route choice behavior for a single vehicle. 
 * Each vehicle should have its own instance of {@link RouteChoice}, which is separate from the {@link Path} followed.
 * For following a fixed path, see the {@link FixedPath} class.
 * However, other implementations are possible as well.
 * @author Michael
 */
public interface RouteChoice 
{
    /**
     * Returns the total length of the links traversed.
     * @return the length traversed (mi)
     */
    //public abstract double getLength();
    
    /**
     * Returns the path that was traversed.
     * If the route choice is not fixed, the path traversed should be stored for postprocessing.
     * @return the path that was traversed
     */
    //public abstract Path getPath();
    
    /**
     * Called when the associated {@link Vehicle} enters the given {@link Link}.
     * @param l the {@link Link} entered
     */
    public abstract void enteredLink(Link l);
    
    /**
     * Called when the associated {@link Vehicle} exits the network.
     */
    public abstract void exited();
    
    /**
     * Called to reset this {@link RouteChoice} for a new simulation.
     */
    public abstract void reset();
    
    /**
     * Returns the next {@link Link} the {@link Vehicle} should take.
     * @param curr the current {@link Link}
     * @return the next {@link Link}
     */
    public abstract Link getNextLink(Link curr, Incident incident);
    
    public abstract Link getFirstLink(Node origin);
    
    
    /**
     * Returns the origin {@link Node}.
     * @return the origin {@link Node}
     */
    //public abstract Node getOrigin();
    
    /**
     * Returns the destination {@link Node}.
     * @return the destination {@link Node}
     */
    //public abstract Node getDest();
    
    /**
     * Called when the vehicle is ready to enter the network
     */
    public abstract void activate();
}
