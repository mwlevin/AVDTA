/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.route;

import avdta.duer.Incident;
import avdta.vehicle.Vehicle;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import static avdta.vehicle.route.AdaptiveRoute.costFunc;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author hdx
 */
public abstract class Hyperpath implements RouteChoice
{
    Map<Node,Map<Incident,Link>> Withinfo = new LinkedHashMap<>();
    
    private Node origin, dest;
    
    private final int Incident;
    
    private Path actual;
    
    private final Link Link;
    
    private Vehicle vehicle;
                   
    public int setInformation(){
         return Incident;
    }

    public Hyperpath(Node origin,Node dest, int Incident, Link Link)
    {
        this.origin = origin;
        this.dest = dest;
        this.Incident = Incident;
        this.Link = Link;
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
        // nothing to do here
    }
    
    public void exited()
    {
        // nothing to do here
    }
    
    public void enteredLink(Link link)
    {
        actual.add(link);
    }
    
    public double getLength()
    {
        return actual.getLength();
    }
    
    public void setNextLink(Node origin, Node dest, int Incident, Link Link)
    {
       origin = this.origin;
       dest = this.dest;
       Incident = this.Incident;
       Link = this.Link;
    }
   
}
