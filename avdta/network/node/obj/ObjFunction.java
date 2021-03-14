/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;


import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;
import avdta.network.type.Type;

/**
 * Objective function for IP for conflict region model
 * @author Michael
 */
public interface ObjFunction
{
    /**
     * Returns the coefficient for moving vehicle across the intersection
     * Note that for the true IP (but not for the MCKS heuristic), non-positive coefficients prevent vehicle movement. 
     * 
     * @param v the {@link Vehicle} the weight applies to
     * @param n the intersection at which the weight applies to
     * @return coefficient for moving vehicle across the intersection. 
     */
    public double value(Vehicle v, TBR n);
    
    /**
     * Returns whether the IP is trying to minimize the objective function.
     * @return whether the IP should minimize the objective function
     */
    public boolean isMinimize();
    
    /**
     * Initialization work before scanning the vehicle list
     * @param n the node to be initialized at
     */
    public void initialize(Node n);
    
    /**
     * Returns the type code associated with this objective.
     * @return depends on subclass
     */
    public Type getType();
}
