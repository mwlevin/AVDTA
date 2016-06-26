/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.intersection.candidate;

import avdta.network.node.VehIndex;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author ut
 */
public class CandidateSolution implements Iterable<VehIndex>
{
    private VehIndex[] priority;
    private boolean[] served;
    private int index;
   
    private int numServed;
    
    public CandidateSolution(List<VehIndex> list)
    {
        this.priority = new VehIndex[list.size()];
        
        for(int i = 0; i < priority.length; i++)
        {
            priority[i] = list.get(i);
        }
        
        served = new boolean[priority.length];
        index = 0;
    }
    
    public VehIndex[] getPriorityList()
    {
        return priority;
    }
    
    public void clear()
    {
        for(int i = 0; i < served.length; i++)
        {
            served[i] = false;
        }
        
        numServed = 0;
    }
    
    public int getNumServed()
    {
        return numServed;
    }
    
    public void serve(int index)
    {
        numServed++;
        served[index] = true;
    }
    
    public Iterator<VehIndex> iterator()
    {
        return new CandidateIterator(priority, served, index, this);
    }

    public int size()
    {
        return priority.length;
    }
}
