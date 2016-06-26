/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.intersection.candidate;

import avdta.network.node.VehIndex;
import java.util.Iterator;

/**
 *
 * @author ut
 */
public class CandidateIterator implements Iterator<VehIndex>
{
    private VehIndex[] priority;
    private boolean[] served;
    private int index;
    private CandidateSolution sol;
    
    public CandidateIterator(VehIndex[] priority, boolean[] served, int startIndex, CandidateSolution sol)
    {
        this.priority = priority;
        this.served = served;
        this.index = startIndex;
        this.sol = sol;
    }
    
    public void remove()
    {
        sol.serve(index);
    }
    
    public int updateIndex()
    {
        int output = index;
        
        while(++output < priority.length && served[output]);
        
        return output;
    }

    public VehIndex next()
    {
        index = updateIndex();
        
        return priority[index];
    }
    
    public boolean hasNext()
    {
        int nextIndex = updateIndex();
        
        return nextIndex < priority.length;
    }
}
