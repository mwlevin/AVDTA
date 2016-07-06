/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.ReadNetwork;
import avdta.network.node.TBR;
import avdta.vehicle.Vehicle;

/**
 *
 * @author micha
 */
public class TransitFirst extends IntersectionPolicy
{
    private IntersectionPolicy defaultPolicy;
    
    public TransitFirst(IntersectionPolicy defaultPolicy)
    {
        this.defaultPolicy = defaultPolicy;
    }
    
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
    
    public void initialize(TBR node, Vehicle v)
    {
        defaultPolicy.initialize(node, v);
    }
    
    public int getType()
    {
        return defaultPolicy.getType();
    }
}
