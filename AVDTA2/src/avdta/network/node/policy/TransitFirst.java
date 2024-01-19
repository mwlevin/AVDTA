/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.network.node.TBR;
import avdta.network.type.Type;
import avdta.vehicle.Vehicle;

/**
 * This policy extends another {@link IntersectionPolicy} and operates as follows:
 * If the {@link Vehicle} is transit, it has priority; otherwise, the default {@link IntersectionPolicy} is used.
 * @author Michael
 */
public class TransitFirst extends IntersectionPolicy
{
    private IntersectionPolicy defaultPolicy;
    
    /**
     * Constructs this {@link TransitFirst} with the {@link FCFSPolicy}
     */
    public TransitFirst()
    {
        this(IntersectionPolicy.FCFS);
    }
    
    /**
     * Constructs this {@link TransitFirst} with the specified policy
     * @param defaultPolicy the default {@link IntersectionPolicy} to be used for non-transit {@link Vehicle}s
     */
    public TransitFirst(IntersectionPolicy defaultPolicy)
    {
        this.defaultPolicy = defaultPolicy;
    }
    
    /**
     * Compares two {@link Vehicle}s according to their reservation priority. {@link Vehicle}s are listed in order of decreasing priority.
     * Transit vehicles are always sorted to the top.
     * @param v1 the first vehicle being compared
     * @param v2 the second vehicle being compared
     * @return sorting index for vehicles
     */
    public int compare(Vehicle v1, Vehicle v2)
    {
        if(v1.isTransit() && !v2.isTransit())
        {
            return -1;
        }
        else if(v2.isTransit() && !v1.isTransit())
        {
            return 1;
        }
        else
        {
            return defaultPolicy.compare(v1, v2);
        }
    }
    
    
    /**
     * Initializes {@link Vehicle} priority for the specified {@link Node} and {@link Vehicle}
     * This calls {@link IntersectionPolicy#initialize(avdta.network.node.TBR, avdta.vehicle.Vehicle)} of the default policy
     * @param node the intersection at which the {@link Vehicle} is waiting.
     * @param v the {@link Vehicle} to be initialized
     */
    public void initialize(TBR node, Vehicle v)
    {
        defaultPolicy.initialize(node, v);
    }
    
    /**
    * Initialization work before scanning vehicle list.
    * This calls {@link IntersectionPolicy#initialize(avdta.network.node.Node)} of the default policy
    */
    public void initialize(Node n)
    {
        super.initialize(n);
    }
    
    /**
     * Returns the type code associated with this policy
     * @return {@link ReadNetwork#TRANSIT_FIRST}+ the type of the default policy
     */
    public Type getType()
    {
        return ReadNetwork.TRANSIT_FIRST;
    }
}
