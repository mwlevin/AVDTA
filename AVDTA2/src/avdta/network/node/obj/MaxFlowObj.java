/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.ReadNetwork;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;

@Deprecated
/**
 * @author Michael
 */
public class MaxFlowObj implements ObjFunction
{
    private double queue_weight;
    
    public MaxFlowObj(double queue_weight)
    {
        this.queue_weight = queue_weight;
    }
    
    public void initialize(Node n){}
    
    public boolean isMinimize()
    {
        return false;
    }
    
    public double value(Vehicle v, TBR n)
    {
        Link i = v.getPrevLink();
        Link j = v.getNextLink();

        //return 1+Math.pow(i.getQueueLength() / i.getNumLanes(), 2) * queue_weight;
        return Math.pow(i.getQueueLength() / i.getNumLanes(), 2) - Math.pow(j.getQueueLength() / j.getNumLanes(), 2) ;
        //return 1;
        
        /*
        
        double value = 0;
        
        for(ConflictRegion cr : n.getConflicts(v))
        {
            value += cr.adjustFlow(i);
        }
        
        return value;
        * */
    }
    
    public int getType()
    {
        return ReadNetwork.Q2;
    }
}
