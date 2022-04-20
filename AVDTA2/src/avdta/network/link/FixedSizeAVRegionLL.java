/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Network;
import avdta.network.Simulator;
import avdta.network.link.multiclassnewell.BoundaryCondition;
import avdta.network.link.multiclassnewell.Region;
import java.util.LinkedList;


/**
 *
 * @author Michael
 */
public class FixedSizeAVRegionLL extends LinkedList<BoundaryCondition> implements CumulativeCountStorage
{
    private int max_size;

    
    public FixedSizeAVRegionLL(int size)
    {
        this.max_size = size+1;
        
        
        clear();
        
    }
    
    public void clear()
    {
        super.clear();
        
        
        for(int i = 0; i < max_size; i++)
        {
            add(new BoundaryCondition(i*Simulator.dt, (i+1)*Simulator.dt, 0, 0));
        }
    }
    
    // t should be equal to size
    public int getCC(int t)
    {

        int difference = Simulator.indexTime(Simulator.time) - t;
        int idx = size() - difference-1;
        
        if(idx >= size())
        {
            throw new RuntimeException("Looking for time "+(t*Simulator.dt)+" time range is ["+(
                    Simulator.time-(size()-1)*Simulator.dt)+","+(Simulator.time+Simulator.dt)
                    +"]");
        }

        
        return super.get(idx).getFinalC();
    }
    
    
    public void nextTimeStep()
    {
        removeFirst();
        
        BoundaryCondition r = getLast();
        BoundaryCondition next = new BoundaryCondition(Simulator.time, Simulator.time+Simulator.dt, r.getFinalC(), r.getFinalC());
        
        add(next);
    }
    
    
    public void addCC(int t, int value)
    {
        int difference = Simulator.indexTime(Simulator.time) - t;
        int idx = size() - difference-1;

        BoundaryCondition r = get(idx);
        r.setFinalC(r.getFinalC()+value);
        
    }

    
}
