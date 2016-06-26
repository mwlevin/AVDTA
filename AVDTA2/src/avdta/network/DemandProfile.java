/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import java.util.TreeMap;

/**
 *
 * @author Michael
 */
public class DemandProfile extends TreeMap<Integer, AST>
{
    private int lowest_id;
    
    public DemandProfile()
    {
        lowest_id = Integer.MAX_VALUE;
    }
    

    
    public AST put(int v, AST a)
    {
        if(a.getId() < lowest_id)
        {
            lowest_id = a.getId();
        }
        
        return super.put(v, a);
    }
    
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
