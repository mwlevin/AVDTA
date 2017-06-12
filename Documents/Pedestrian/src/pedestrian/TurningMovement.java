/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.Set;

/**
 *
 * @author ml26893
 */
public class TurningMovement extends Queue implements Comparable<TurningMovement>
{
    private IncomingLink i;
    private OutgoingLink j;
    private Set<ConflictRegion> cr;
    
    public double efficiency;
    
    public TurningMovement(IncomingLink i, OutgoingLink j, Set<ConflictRegion> cr, int max, double rate)
    {
        super(max, rate);
        this.i = i;
        this.j = j;
        this.cr = cr;
        
    }
    
    public String toString()
    {
        return "("+i+","+j+")";
    }
    
    public int compareTo(TurningMovement rhs)
    {
        if(efficiency > rhs.efficiency)
        {
            return -1;
        }
        else if(efficiency < rhs.efficiency)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }
    

    public IncomingLink getI()
    {
        return i;
    }
    
    public OutgoingLink getJ()
    {
        return j;
    }
    
    public Set<ConflictRegion> getConflictRegions()
    {
        return cr;
    }
}
