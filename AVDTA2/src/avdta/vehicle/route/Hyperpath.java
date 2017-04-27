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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author hdx
 */
public class Hyperpath implements RouteChoice
{
    private Map<Node,Map<Incident,Link>> outerMap = new HashMap<>();
    
    private Incident information;
    
    public void setInformation(Incident incident)
    {
        information = incident; 
    }
    public Link getNextLink(Link link)
    {
        Link next;
        Node node = link.getDest();
        Map<Incident, Link> innerMap;
        innerMap = outerMap.get(node);
        next = innerMap.get(information);
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
        
    }
        
    public void setNextLink(Node node, Incident incident, Link next)
    {
        Map<Incident,Link> innerMap;
        innerMap = outerMap.get(node);
        innerMap.put(incident, next);
        outerMap.put(node,innerMap);
    }

    @Override
    public void reset() {
       
    }
 
}
