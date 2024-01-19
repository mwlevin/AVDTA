/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import java.util.HashSet;

/**
 * This is a representation of a turning movement that passes through one or more {@link ConflictRegion}s. 
 * Therefore, it extends a {@link HashSet} of {@link ConflictRegion}s, and adds several utility methods.
 * @author Michael
 */
public class TurningMovement extends HashSet<ConflictRegion>
{
    public static final int LEFT = 0;
    public static final int STRAIGHT = 1;
    public static final int RIGHT = 2;
    
    private int lane;
    
    /**
     * Initializes this {@link TurningMovement} using the {@link TurningMovement#LEFT} lane.
     */
    public TurningMovement()
    {
        this(0);
    }
    
    /**
     * Initializes this {@link TurningMovement} with the specified lane used.
     * 
     * @param lane the lane of the incoming {@link Link}
     */
    public TurningMovement(int lane)
    {
        this.lane = lane;
    }
    
    /**
     * Sets the lane of the incoming {@link Link} associated with this {@link TurningMovement}.
     * @param inc the incoming {@link Link}
     * @param lane the new lane of the incoming {@link Link}
     */
    public void setLane(Link inc, int lane)
    {
        this.lane = (int)Math.min(lane, inc.getNumLanes()-1);
    }
    
    /**
     * the lane of the incoming {@link Link} associated with this {@link TurningMovement}.
     * @return the lane of the incoming {@link Link} associated with this {@link TurningMovement}
     */
    public int getLane()
    {
        return lane;
    }
}
