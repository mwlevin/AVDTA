/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.transit;

import avdta.network.link.Link;
import avdta.network.node.Node;

/**
 * This link represents a traveler walking along a main link. 
 * Its purpose is to increase connectivity between bus stops and traveler origins/destinations. 
 * Walking has a high travel time, though, so it has a high disutility.
 * @author Michael
 */
public class WalkingLink extends TransitLink
{
    public static final double WALKING_SPEED = 3.0;
    
    private double tt;
    
    /**
     * Constructs the walking link with the given parameters
     * @param source the upstream node
     * @param dest the downstream node
     * @param length the length (mi)
     */
    public WalkingLink(Node source, Node dest, double length)
    {
        super(source, dest);
        
        tt = length / WALKING_SPEED * 3600;
    }
    
    
    /**
     * Constructs a walking link for the given regular link
     * @param rhs the regular link
     */
    public WalkingLink(Link rhs)
    {
        this(rhs.getSource(), rhs.getDest(), rhs.getLength());
    }
    
    /**
     * Returns the expected travel time. Travel times are constant and based on the {@link WalkingLink#WALKING_SPEED}.
     * @param dtime the departure time
     * @return length / {@link WalkingLink#WALKING_SPEED}
     */
    public double getTT(int dtime)
    {
        return tt;
    }
}
