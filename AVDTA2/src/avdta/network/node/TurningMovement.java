/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import java.util.HashSet;

/**
 *
 * @author Michael
 */
public class TurningMovement extends HashSet<ConflictRegion>
{
    public static final int LEFT = 0;
    public static final int STRAIGHT = 1;
    public static final int RIGHT = 2;
    
    private int lane;
    
    public TurningMovement()
    {
        this(0);
    }
    
    public TurningMovement(int lane)
    {
        this.lane = lane;
    }
    
    public void setLane(Link inc, int lane)
    {
        this.lane = (int)Math.min(lane, inc.getNumLanes()-1);
    }
    public int getLane()
    {
        return lane;
    }
}
