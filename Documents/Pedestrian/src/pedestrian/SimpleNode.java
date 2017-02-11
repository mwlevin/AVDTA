/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author micha
 */
public class SimpleNode extends Node
{
    private IncomingLink l1, l2;
    private OutgoingLink l3, l4;
    
    private Crosswalk c1, c2;
    
    private static final double lane_width = 12;
    
    public SimpleNode(double Q1, double Q2, double dem1, double dem2, double dem_c1, double dem_c2)
    {
        l3 = new OutgoingLink(this, Q1, lane_width);
        l4 = new OutgoingLink(this, Q2, lane_width);
        
        l1 = new IncomingLink(this, Q1, lane_width);
        l2 = new IncomingLink(this, Q2, lane_width);
        
        c1 = new Crosswalk(Node.MAX_QUEUE, dem_c1, l1, l3);
        c2 = new Crosswalk(Node.MAX_QUEUE, dem_c2, l2, l4);
        
        ConflictRegion cr = new ConflictRegion();
        Set<ConflictRegion> conflicts = new HashSet<ConflictRegion>();
        conflicts.add(cr);
        l1.addTurningMovement(new TurningMovement(l1, l3, conflicts, Node.MAX_QUEUE, dem1));
        l2.addTurningMovement(new TurningMovement(l2, l4, conflicts, Node.MAX_QUEUE, dem2));
        
    }
}
