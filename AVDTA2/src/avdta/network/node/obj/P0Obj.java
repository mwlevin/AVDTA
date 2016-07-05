/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.node.obj.ObjFunction;
import avdta.network.link.Link;
import avdta.network.Simulator;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;
import java.util.List;

/**
 *
 * @author Michael
 */
public class P0Obj implements ObjFunction
{
    public boolean isMinimize()
    {
        return false;
    }
    
    public void initialize(Node n){}
    
    public double value(Vehicle vehicle, TBR node)
    {
        Link i = vehicle.getPrevLink();

        double pressure_i = 0;
        
        double Q = i.getCapacity();
        double t_f = i.getFFTime();
        double t = i.getAvgTT(Simulator.time);
        
        pressure_i = Q*(t - t_f);
        
        return pressure_i;
    }
    
}
