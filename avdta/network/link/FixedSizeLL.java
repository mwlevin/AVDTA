/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Network;
import avdta.network.Simulator;
import java.util.LinkedList;


/**
 *
 * @author Michael
 */
public class FixedSizeLL extends LinkedList<Integer> implements CumulativeCountStorage
{
    private int max_size;

    
    public FixedSizeLL(int size)
    {
        this.max_size = size+1;
        
        
        clear();
        
    }
    
    public void clear()
    {
        super.clear();
        
        for(int i = 0; i < max_size; i++)
        {
            add(0);
        }
    }
    
    // t should be equal to size
    public int getCC(int t)
    {

        int difference = Simulator.indexTime(Simulator.time) - t;
        int idx = size() - difference-2;
        
        if(idx >= size())
        {
            throw new RuntimeException("Looking for time "+(t*Simulator.dt)+" time range is ["+(
                    t-(size()-1)*Simulator.dt)+","+(Simulator.time+Simulator.dt)
                    +"]");
        }

        
        return super.get(idx);
    }
    
    
    public void nextTimeStep()
    {
        removeFirst();
        add(0);
    }
    
    
    public void addCC(int t, int value)
    {
        int difference = Simulator.indexTime(Simulator.time) - t;
        int idx = size() - difference-2;

        set(idx, get(idx)+value);
        
    }
    
}
