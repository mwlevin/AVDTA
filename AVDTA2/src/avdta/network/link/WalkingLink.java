/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;
import avdta.network.link.TransitLink;

/**
 *
 * @author Michael
 */
public class WalkingLink extends TransitLink
{
    public static final double WALKING_SPEED = 3.0;
    
    private double tt;
    
    public WalkingLink(Node source, Node dest, double length)
    {
        super(source, dest);
        
        tt = length / WALKING_SPEED;
    }
    
    public WalkingLink(Link rhs)
    {
        this(rhs.getSource(), rhs.getDest(), rhs.getLength());
    }
    
    public double getTT(int dtime)
    {
        return tt;
    }
    
    public void reset(){}
}
