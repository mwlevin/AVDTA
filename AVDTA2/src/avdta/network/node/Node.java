/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.TransitLink;
import avdta.network.link.Link;
import avdta.vehicle.DriverType;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * An abstract node in the traffic network. Nodes can be {@link Intersection}s 
 * or {@link Zone}s. <br>
 * {@code CR} stands for conflict regions. <br>
 * {@code SIGNALS} whether an intersection has signal or not. <br>
 * {@code IP} integer program. <br>
 * {@code MCKS} heuristic for IP; multiple constrained knapsack problem. <br>
 * {@code STOP} for stop sign. <br>
 * {@code incoming} and {@code outgoing} for a set of incoming {@link Link}s
 * and outgoing {@link Link}s.
 * @author Michael
 */
public abstract class Node extends Location implements Serializable, Comparable<Node>
{
    
    private Set<Link> incoming, outgoing;
    private int id;
    
    
    
    public double label;
    public int arr_time;
    public Link prev;
    public TransitLink transit_prev;
    
    public double vc;
    
    private Set<TransitLink> transitInc, transitOut;
    
    /**
     * Instantiates a node with location (0, 0)
     * @param id A unique id for the node.
     */
    public Node(int id)
    {
        this(id, new Location(0, 0));
    }
    
    /**
     * 
     * @param id A unique id for the node
     * @param loc take {@link Location} as input
     */
    public Node(int id, Location loc)
    {
        super(loc);
        this.id = id;
        
        incoming = new HashSet<Link>();
        outgoing = new HashSet<Link>();

        transitInc = new HashSet<TransitLink>();
        transitOut = new HashSet<TransitLink>();
        
        
    }
    
    public void setIncoming(Set<Link> inc)
    {
        incoming = inc;
    }
    
    public void setOutgoing(Set<Link> out)
    {
        outgoing = out;
    }
    
    public int compareTo(Node rhs)
    {
        return id - rhs.id;
    }
    
    /**
     * 
     * @return if this node is a centroid
     */
    public boolean isZone()
    {
        return false;
    }
    
    /**
     * 
     * @return if this node uses conflict regions for reservations
     */
    public abstract boolean hasConflictRegions();
    
    /**
     * 
     * @param i An incoming {@link Link} to the intersection.
     * @param j An outgoing {@link Link} from the intersection.
     * @param driver For knowing the {@link DriverType}.
     * @return whether vehicles of type driver can move from i to j across 
     * this node.
     * @see IntersectionControl
     */
    public abstract boolean canMove(Link i, Link j, DriverType driver);
    
    /**
     * Resets this node for a new simulation
     */
    public void reset(){}
    
    /**
     * Initialize this node to start simulating
     */
    public void initialize(){}
    
    public int getId()
    {
        return id;
    }
    

    /**
     * 
     * @return id
     */
    public String toString()
    {
        return ""+id;
    }
    
    
    public int hashCode()
    {
        return id;
    }
    
    public abstract int step();
    
    /**
     * Updates incoming or outgoing links, appropriately
     * @param l {@link Link} to be updated.
     */
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
    
    /**
     * Updates incoming or outgoing links, appropriately
     * @param l {@link TransitLink} to be updated.
     */
    public void addLink(TransitLink l)
    {
        if(l.getSource() == this)
        {
            transitOut.add(l);
        }
        else if(l.getDest() == this)
        {
            transitInc.add(l);
        }
    }
    
    /**
     * 
     * @return incoming links
     */
    public Set<Link> getIncoming()
    {
        return incoming;
    }
    
    /**
     * 
     * @return outgoing links
     */
    public Set<Link> getOutgoing()
    {
        return outgoing;
    }
    
    /**
     * 
     * @return incoming transit links
     */
    public Set<TransitLink> getTransitInc()
    {
        return transitInc;
    }
    
    public abstract int getType();
    /**
     * 
     * @return outgoing transit links
     */
    public Set<TransitLink> getTransitOut()
    {
        return transitOut;
    }
}
