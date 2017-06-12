/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.route;

import avdta.dta.DTASimulator;
import avdta.duer.Incident;
import avdta.vehicle.Vehicle;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import avdta.util.Pair;
import static avdta.vehicle.route.AdaptiveRoute.costFunc;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author hdx
 */
public class Hyperpath implements RouteChoice
{

    private Map<Link, Map<Incident,Link>> map;
    
    private Map<Node, Pair<Double, Link>> origins;

    
    public Hyperpath()
    {
        map = new HashMap<>();
        origins = new HashMap<>();
    }
    
    
    // implement this
    public double getAvgCost(Node origin, double dep_time)
    {
        return origins.get(origin).first();
    }
    
    public Link getNextLink(Link link, Incident information)
    {
        if(!map.containsKey(link))
        {
            return null;
        }
        else
        {
            return map.get(link).get(information);
        }
    }
    
    public Link getFirstLink(Node origin)
    {
        return origins.get(origin).second();
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
        
    public void setNextLink(Node origin, double cost, Link next)
    {
        origins.put(origin, new Pair(cost, next));
    }
    
    public void setNextLink(Link link, Incident incident, Link next)
    {
        Map<Incident, Link> temp;
        
        if(map.containsKey(link))
        {
            temp = map.get(link);
        }
        else
        {
            map.put(link, temp = new HashMap<>());
        }
        temp.put(incident, next);
    }

    @Override
    public void reset() {
       
    }
 
}
