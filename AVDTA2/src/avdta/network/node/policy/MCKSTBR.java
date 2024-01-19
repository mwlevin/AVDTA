/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.node.obj.ObjFunction;
import avdta.network.node.Intersection;
import avdta.network.node.Intersection;
import avdta.network.node.PriorityTBR;
import avdta.network.node.PriorityTBR;
import avdta.network.node.Signalized;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import avdta.network.node.Node;
import java.util.TreeSet;

/**
 * A heuristic to solve the multiple constraint knapsack problem applied to the IP for conflict region model. 
 * This class constructs an efficiency priority based on the capacity consumed by vehicles and the given objective function.
 * @author Michael
 */

public class MCKSTBR extends PriorityTBR
{
    /**
     * Constructs the {@link MCKSTBR} with the specified {@link ObjFunction} and a null {@link Node}. A non-null {@link Node} is required for simulation.
     * @param func the objective function to be used
     */
    public MCKSTBR(ObjFunction func)
    {
        this(null, func);
    }
    
    /**
     * Constructs the {@link MCKSTBR} with the specified {@link ObjFunction} and the specified {@link Intersection}
     * @param n the {@link Intersection} controlled by this {@link MCKSTBR}
     * @param func the objective function to be used
     */
    public MCKSTBR(Intersection n, ObjFunction func)
    {
        super(n, new MCKSPriority(func));
    }  
    
    /**
     * Returns the objective function used
     * @return the objective function used
     */
    public ObjFunction getObj()
    {
        return ((MCKSPriority)getPolicy()).getObj();
    }
    
    /**
     * Returns the {@link Signalized} form of the {@link ObjFunction} for adding signal data, if it exists, or null otherwise
     * @return the {@link Signalized} form of the {@link ObjFunction} for adding signal data, if it exists, or null otherwise
     * @see Signalized
     */
    public Signalized getSignal()
    {
        ObjFunction obj = ((MCKSPriority)getPolicy()).getObj();
        return (obj instanceof Signalized)? (Signalized)obj : null;
    }
}
