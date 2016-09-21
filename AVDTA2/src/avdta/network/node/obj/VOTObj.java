/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;

/**
 * This objective function weights vehicles by their value-of-time.
 * @author Michael
 */
public class VOTObj implements ObjFunction
{
    
    /**
     * Initialization work before scanning the vehicle list. Nothing to be done.
     */
    public void initialize(Node n){}
    
    /**
     * Returns whether the IP is trying to minimize the objective function.
     * @return false
     */
    public boolean isMinimize()
    {
        return false;
    }
    
    /**
     * Returns the coefficient for moving vehicle across the intersection
     * 
     * @param v the {@link Vehicle} the weight applies to
     * @param n the intersection at which the weight applies to
     * @return the {@link Vehicle} value of time
     */
    public double value(Vehicle v, TBR n)
    {
        return v.getVOT();
    }
    
    /**
     * Returns the type code associated with the {@link VOTObj}
     * @return {@link ReadNetwork#VOT}
     */
    public int getType()
    {
        return ReadNetwork.VOT;
    }
}
