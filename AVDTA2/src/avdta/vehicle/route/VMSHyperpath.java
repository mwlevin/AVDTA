/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.route;

import avdta.dta.DTASimulator;
import avdta.duer.Incident;
import avdta.duer.State;
import avdta.vehicle.Vehicle;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import static avdta.vehicle.route.AdaptiveRoute.costFunc;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class VMSHyperpath implements RouteChoice
{

    private Map<State, Link> map;
    private Map<Node, Link> originMap;
    
    
    public VMSHyperpath()
    {
        map = new HashMap<State, Link>();
        originMap = new HashMap<Node, Link>();
    }
    
    
    // implement this
    public double getAvgCost(double dep_time)
    {
        return 0;
    }
    
   
    public Link getNextLink(Link link, Incident incident)
    {
        State s = new State(link, incident);
        
        return map.get(s);
    }
    
    public Link getFirstLink(Node origin)
    {
        return originMap.get(origin);
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
        
    }
        
    public void setNextLink(Link curr, Incident incident, Link next)
    {
        map.put(new State(curr, incident), next);
    }

    @Override
    public void reset() {
       
    }
 
}
