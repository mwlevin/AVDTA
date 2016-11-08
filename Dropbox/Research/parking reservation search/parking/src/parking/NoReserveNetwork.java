/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parking;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author micha
 */
public class NoReserveNetwork extends Network
{
    public NoReserveNetwork(String name)
    {
        super(name);
    }
    
    public List<Action> createU(State x)
    {
        List<Action> output = new ArrayList<Action>();
        
        if(x == State.PARK)
        {
            output.add(Action.PARK);
            return output;
        }
        
        if(x.getNext() == x.getReserved() && x.getTimeRem() == 0)
        {
            output.add(Action.PARK);
            return output;
        }
        
        
        
        if(x.getTimeRem() > 0)
        {
            if(x.getTimeRem() == 1 && (x.getNext() instanceof Zone))
            {
                output.add(new Action(null, (Zone)x.getNext()));
            }
            
            output.add(new Action(null, Zone.NULL));
        }
        else if(x.getTimeRem() == 0)
        {
            for(Link l : x.getNext().getOutgoing())
            {
                output.add(new Action(l, Zone.NULL));
                
                if(l.getTT() == 1 && (l.getDest() instanceof Zone))
                {
                    output.add(new Action(l, (Zone)l.getDest()));
                }
            }
        }
        
        return output;
    }
}
