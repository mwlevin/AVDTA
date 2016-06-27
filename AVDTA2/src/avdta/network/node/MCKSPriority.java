/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.Vehicle;
import avdta.network.node.ConflictRegion;
import avdta.network.node.IntersectionPolicy;
import avdta.network.node.TBR;

/**
 * Defines efficiency function to prioritize vehicles when using MCKS greedy heuristic.
 * @author Michael
 */
public class MCKSPriority extends IntersectionPolicy
{
    private ObjFunction func;
    
    public MCKSPriority(ObjFunction func)
    {
        this.func = func;
    }
    
    public ObjFunction getObj()
    {
        return func;
    }
    
    public void initialize(Node n)
    {
        func.initialize(n);
    }
    
    public int compare(Vehicle v1, Vehicle v2)
    {
        if(v1.efficiency != v2.efficiency)
        {
            if(func.isMinimize())
            {
                return (int)(10000*(v1.efficiency - v2.efficiency));
            }
            else
            {
                return (int)(10000*(v2.efficiency - v1.efficiency));
            }
        }
        else if(v1.getNetEnterTime() != v2.getNetEnterTime())
        {
            return v1.getNetEnterTime() - v2.getNetEnterTime();
        }
        else
        {
            return v1.getId() - v2.getId();
        }
    }
    
    public void initialize(TBR node, Vehicle v)
    {
        
        double weight = 0;
        
        for(ConflictRegion cr : node.getConflicts(v))
        {
            if(v.getNextLink() != null)
            {
                weight += cr.adjustFlow(v.getPrevLink(), v.getNextLink()) / cr.R;
            }
        }
        
        weight += 1 / v.getNextLink().R;
        
        v.efficiency = func.value(v, node) / weight;
        
        /*
        double weight = 0;
        
        for(ConflictRegion cr : node.getConflicts(v))
        {
            weight += cr.adjustFlow(v.getPrevLink());
        }
        
        weight += 1;
        
        v.efficiency = func.value(v, node) / weight;
        */
    }
}
