/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.transit;

import avdta.network.node.Node;
import java.io.Serializable;

/**
 * A transit link is a virtual link that represents the flow of travelers on non-personal vehicle modes. 
 * Transit links cannot be traversed by vehicles.
 * Extensions include {@link WalkingLink}, and {@link BusLink} (which is a direct connection between two nodes that may not be connected in the real network).
 * @author Michael
 */
public abstract class TransitLink implements Serializable
{
    private Node source, dest;
    
    /**
     * Constructs the {@link TransitLink} with the specified upstream and downstream {@link Node}s
     * @param source the upstream {@link Node}
     * @param dest the downstream {@link Node}
     */
    public TransitLink(Node source, Node dest)
    {
        this.source = source;
        this.dest = dest;
        
        source.addLink(this);
        dest.addLink(this);
    }
    
    /**
     * Returns the expected travel time for the given departure time
     * @param dtime the specified departure time (s)
     * @return the expected travel time
     */
    public abstract double getTT(int dtime);
    
    /**
     * Resets this link to restart the simulation.
     */
    public void reset(){}
    
    /**
     * Returns the upstream {@link Node}
     * @return the upstream {@link Node}
     */
    public Node getSource()
    {
        return source;
    }
    
    /**
     * Returns the downstream {@link Node}
     * @return the downstream {@link Node}
     */
    public Node getDest()
    {
        return dest;
    }
    
    
}
