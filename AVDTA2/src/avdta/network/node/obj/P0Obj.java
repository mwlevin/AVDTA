/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.ReadNetwork;
import avdta.network.node.obj.ObjFunction;
import avdta.network.link.Link;
import avdta.network.Simulator;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;
import avdta.network.type.Type;
import java.util.List;

/**
 * This is the objective function used for the P0 policy. 
 * @author Michael
 */
public class P0Obj implements ObjFunction
{
    /**
     * Returns whether the IP is trying to minimize the objective function.
     * @return false
     */
    public boolean isMinimize()
    {
        return false;
    }
    
    /**
     * Initialization work before scanning the vehicle list. Nothing to be done.
     */
    public void initialize(Node n){}
    
    /**
     * Returns the coefficient for moving vehicle across the intersection
     * The coefficient is based on the pressure from high link travel times.
     * 
     * @param vehicle the {@link Vehicle} the weight applies to
     * @param node the intersection at which the weight applies to
     * @return coefficient for moving vehicle across the intersection. 
     */
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
    
    /**
     * Returns the type code associated with the {@link P0Obj}
     * @return {@link ReadNetwork#P0}
     */
    public Type getType()
    {
        return ReadNetwork.P0;
    }
    
}
