/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.node.Intersection;
import avdta.network.node.PriorityTBR;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A heuristic to solve the multiple constraint knapsack problem, applied to the IP for conflict region model.
 * @author Michael
 */

public class MCKSTBR extends PriorityTBR
{
    public MCKSTBR(ObjFunction func)
    {
        this(null, func);
    }
    public MCKSTBR(Intersection n, ObjFunction func)
    {
        super(n, new MCKSPriority(func));
    }  
    
    public ObjFunction getObj()
    {
        return ((MCKSPriority)getPolicy()).getObj();
    }
}
