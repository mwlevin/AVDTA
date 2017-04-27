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
    public static void test() throws IOException
    {
        Hyperpath test = new Hyperpath();
        
        Incident i1 = new Incident(1);
        Incident i2 = new Incident(2);
        
        DTAProject project = new DTAProject(new File("projects/coacongress2"));
        DTASimulator sim = project.getSimulator();
        
        Set<Node> nodes = sim.getNodes();
        Set<Link> links = sim.getLinks();
        
        Iterator<Link> iter = links.iterator();
        Link l1 = iter.next();
        Link l2 = iter.next();
        
        Iterator<Node> iter2 = nodes.iterator();
        Node n = iter2.next();
        
        test.setNextLink(n, i1, l1);
        test.setNextLink(n, i2, l2);
        test.setInformation(i1);
        System.out.println(test.getNextLink(n.getIncoming().iterator().next()) == l1);
        test.setInformation(i2);
        System.out.println(test.getNextLink(n.getIncoming().iterator().next()) == l2);
        
        n = iter2.next();
        
        test.setNextLink(n, i1, l1);
        test.setNextLink(n, i2, l2);
        test.setInformation(i1);
        System.out.println(test.getNextLink(n.getIncoming().iterator().next()) == l1);
        test.setInformation(i2);
        System.out.println(test.getNextLink(n.getIncoming().iterator().next()) == l2);
        
    }
    
    
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
