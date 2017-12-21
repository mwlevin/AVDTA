/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.ReadNetwork;
import avdta.network.node.obj.ObjFunction;
import avdta.vehicle.Vehicle;
import avdta.network.node.ConflictRegion;
import avdta.network.node.ConflictRegion;
import avdta.network.node.Node;
import avdta.network.node.TBR;
import avdta.network.node.TBR;
import avdta.network.type.Type;

/**
 * Defines efficiency function to prioritize vehicles when using MCKS greedy heuristic.
 * @author Michael
 */
public class MCKSPriority extends IntersectionPolicy implements ObjPolicy
{
    private ObjFunction func;
    
    /**
     * Constructs this {@link MCKSPriority} with the given objective
     * @param func the objective function 
     */
    public MCKSPriority(ObjFunction func)
    {
        this.func = func;
    }
    
    /**
     * Returns the objective function
     * @return the objective function
     */
    public ObjFunction getObj()
    {
        return func;
    }
    
    /**
     * Initializes the objective function for the specified {@link Node}
     * @param n the {@link Node} that this {@link MCKSPriority} is used on.
     */
    public void initialize(Node n)
    {
        func.initialize(n);
    }
    
    /**
     * Compares two {@link Vehicle}s according to their reservation priority. {@link Vehicle}s are listed in order of decreasing priority.
     * Vehicles are sorted according to their efficiency.
     * @param v1 the first vehicle being compared
     * @param v2 the second vehicle being compared
     * @return sorting index for vehicles
     */
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
    
    /**
     * Initializes vehicle efficiency based on the conflict region and downstream receiving flow used. 
     * @param node the {@link Node} this priority applies to
     * @param v the {@link Vehicle} this priority applies to
     */
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
    
    /**
     * Returns the type code associated with this policy
     * @return {@link ReadNetwork#MCKS} + the type of the {@link ObjFunction}
     */
    public Type getType()
    {
        return func.getType();
    }
}
