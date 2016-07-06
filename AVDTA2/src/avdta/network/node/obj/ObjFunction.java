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
 * Objective function for IP for conflict region model
 * @author Michael
 */
public interface ObjFunction
{
    /**
     * 
     * @param v
     * @param n
     * @return coefficient for moving vehicle across the intersection. Note that for the true IP (but not for MCKS heuristic), non-positive coefficients prevent vehicle movement. 
     */
    public double value(Vehicle v, TBR n);
    
    /**
     * 
     * @return whether the IP should minimize the objective function
     */
    public boolean isMinimize();
    
    /**
     * Initialization work before scanning the vehicle list
     */
    public void initialize(Node n);
    
    public int getType();
}
