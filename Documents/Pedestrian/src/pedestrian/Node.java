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
public class Node 
{
    private Set<IncomingLink> incoming;
    private Set<OutgoingLink> outgoing;
    
    private Set<Crosswalk> crosswalks;
    
    private Queue[] queues;
    
    private Set<ConflictRegion> conflictRegions;
    
    public Node()
    {
        incoming = new HashSet<IncomingLink>();
        outgoing = new HashSet<OutgoingLink>();
        conflictRegions = new HashSet<ConflictRegion>();
        
        queues = new Queue[0];
    }
    
    public void setConflictRegions(Set<ConflictRegion> cr)
    {
        conflictRegions = cr;
    }
    
    
    public Set<IncomingLink> getIncoming()
    {
        return incoming;
    }
    
    public Set<OutgoingLink> getOutgoing()
    {
        return outgoing;
    }
    
    public void addLink(Link l)
    {
        if(l instanceof IncomingLink)
        {
            incoming.add((IncomingLink)l);
        }
        else if(l instanceof OutgoingLink)
        {
            outgoing.add((OutgoingLink)l);
        }
    }
    
    public void initialize()
    {

        
        for(ConflictRegion cr : conflictRegions)
        {
            cr.initialize();
        }
        
        queues = new Queue[crosswalks.size() + incoming.size() * outgoing.size()];
        
        int idx = 0;
        
        for(Crosswalk c : crosswalks)
        {
            queues[idx++] = c.getQueue();
        }
        
        for(IncomingLink l : incoming)
        {
            for(OutgoingLink v : outgoing)
            {
                queues[idx++] = l.getQueue(v);
            }
        }
    }
    
    public int getNumQueues()
    {
        return queues.length;
    }
    
    public Queue[] getQueues()
    {
        return queues;
    }
}
