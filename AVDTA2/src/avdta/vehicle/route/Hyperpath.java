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

    private Map<Node,Map<Incident,Link>> outerMap;
    
    private Incident information;
    
    public Hyperpath()
    {
        outerMap = new HashMap<>();
    }
    
    
    // implement this
    public double getAvgCost(Node origin, double dep_time)
    {
        return 0;
    }
    
    public void setInformation(Incident incident)
    {
        information = incident; 
    }
    public Link getNextLink(Link link, Incident information)
    {
        Link next;
        Node node = link.getDest();
        Map<Incident, Link> innerMap;
        innerMap = outerMap.get(node);
        if(innerMap==null)
        {
            return null;
        }
        next = innerMap.get(information);
        return next;
    }
    
    public Link getFirstLink(Node origin)
    {
        return outerMap.get(origin).get(Incident.NULL);
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
        
    public void setNextLink(Node node, Incident incident, Link next)
    {
        Map<Incident,Link> innerMap = outerMap.get(node);
        if(innerMap==null)
        {
            innerMap = new HashMap();
            outerMap.put(node, innerMap);
        }
        innerMap.put(incident, next);
    }

    @Override
    public void reset() {
       
    }
 
}
