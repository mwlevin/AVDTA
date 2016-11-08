/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parking;

import java.util.Set;
import java.util.HashSet;

/**
 *
 * @author micha
 */
public class Node 
{
    private Set<Link> incoming, outgoing;

    private int id;
    
    public int label;
    public Link prev;
    
    public Node(int id)
    {
        this.id = id;

        
        incoming = new HashSet<Link>();
        outgoing = new HashSet<Link>();
    }
    
    
    
    public int hashCode()
    {
        return id;
    }
    
    public String toString()
    {
        return ""+id;
    }
    
    public int getId()
    {
        return id;
    }
    
    public void addLink(Link l)
    {
        if(l.getSource() == this)
        {
            outgoing.add(l);
        }
        else if(l.getDest() == this)
        {
            incoming.add(l);
        }
    }
    
    public Set<Link> getIncoming()
    {
        return incoming;
    }
    
    public Set<Link> getOutgoing()
    {
        return outgoing;
    }
    
    public boolean isOrigin()
    {
        return id >= 100000 && id <= 200000;
    }
}
