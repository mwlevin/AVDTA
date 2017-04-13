/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.route;

import avdta.microtoll.MTSimulator;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;

/**
 *
 * @author ml26893
 */
public class AdaptiveRoute implements RouteChoice
{
    public static final TravelCost costFunc = TravelCost.dnlGenCost;
    
    private Path actual;
    
    private Node origin, dest;

    
    private Vehicle vehicle;
    
    public AdaptiveRoute(Vehicle v, Node origin, Node dest)
    {
        this.vehicle = v;
        this.origin = origin;
        this.dest = dest;
    }
    
    public Path getPath()
    {
        return actual;
    }
    
    public Node getOrigin()
    {
        return origin;
    }
    
    public Node getDest()
    {
        return dest;
    }
    
    public void reset()
    {
        actual = new Path();
    }
    
    public Link getNextLink(Link link)
    {
        Simulator sim = Simulator.active;
        
        if(link == null)
        {
            sim.dijkstras(origin, getDest(), sim.time, vehicle.getVOT(), vehicle.getDriver(), costFunc);    
        }
        else
        {
            sim.dijkstras(link, getDest(), sim.time, vehicle.getVOT(), vehicle.getDriver(), costFunc);    
        }
        Path newPath = sim.trace(origin, dest);
        
        Link next = newPath.get(0);
        
        return next;
    }
    
    public void activate()
    {

    }
    
    public void exited()
    {
        
    }
    
    public void enteredLink(Link link)
    {
        actual.add(link);

    }
    
    public double getLength()
    {
        return actual.getLength();
    }
}
