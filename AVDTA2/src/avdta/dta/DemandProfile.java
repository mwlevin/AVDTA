/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.dta.AST;
import java.util.TreeMap;

/**
 * A demand profile is a set of assignment intervals.
 * These assignment intervals should not overlap in time.
 * @author Michael
 */
public class DemandProfile extends TreeMap<Integer, AST>
{
    
    
    /**
     * Adds the assignment interval to this {@link DemandProfile}.
     * @param a the assignment interval to be added
     */
    public void add(AST a)
    {
        put(a.getId(), a);
    }
    
    /**
     * This method normalizes the weights so that they sum to 1.
     * This should only be called after all assignment intervals have been added.
     */
    public void normalizeWeights()
    {
        double total = 0.0;
        
        for(int id : keySet())
        {
            total += get(id).getWeight();
        }
        
        for(int id : keySet())
        {
            AST a = get(id);
            a.setWeight(a.getWeight() / total);
        }
    }
    
    /**
     * Returns the assignment interval corresponding to the given time.
     * This method will only return one {@link AST} if multiple spans the specified time. 
     * If so, the {@link AST} returned will be the one that has the lower id.
     * @param time the time (s)
     * @return the assignment interval corresponding to the given time
     */
    public int getAST(int time)
    {
        for(int id : keySet())
        {
            AST a = get(id);
            if(a.contains(time))
            {
                return a.getId();
            }
        }
        
        return -1;
    }
}
