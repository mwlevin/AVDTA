/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;

/**
 *
 * @author ut
 */
public class VOTObj implements ObjFunction
{
    
    public VOTObj()
    {
    }
    
    public void initialize(Node n){}
    
    public boolean isMinimize()
    {
        return false;
    }
    
    public double value(Vehicle v, TBR n)
    {
        return v.getVOT();
    }
}
