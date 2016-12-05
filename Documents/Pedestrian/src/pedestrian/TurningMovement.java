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
public class TurningMovement 
{
    private IncomingLink i;
    private OutgoingLink j;
    private Set<ConflictRegion> cr;
    private Queue queue;
    
    public TurningMovement(IncomingLink i, OutgoingLink j, Set<ConflictRegion> cr)
    {
        this.i = i;
        this.j = j;
        this.cr = cr;
        queue = new Queue();
    }
    
    public Queue getQueue()
    {
        return queue;
    }
    
    public IncomingLink getI()
    {
        return i;
    }
    
    public OutgoingLink getJ()
    {
        return j;
    }
    
    public Set<ConflictRegion> getCR()
    {
        return cr;
    }
}
