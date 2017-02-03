/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class IncomingLink extends Link
{
    private Map<OutgoingLink, TurningMovement> turns;
    
    public IncomingLink(Node node, double capacity, double width, OutgoingLink... out)
    {
        super(node, capacity, width);
        turns = new HashMap<OutgoingLink, TurningMovement>();
        
        
    }
    
    public void addTurningMovement(TurningMovement turn)
    {
        turns.put(turn.getJ(), turn);
    }
    
    public TurningMovement getQueue(Link out)
    {
        return turns.get(out);
    }
}
