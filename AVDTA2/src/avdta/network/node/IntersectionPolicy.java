/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.RandomPolicy;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;

/**
 * An abstract prioritization of vehicles in reservation controls
 * @author Michael
 */
public abstract class IntersectionPolicy implements Comparator<Vehicle>
{
    public static final FCFSPolicy FCFS = new FCFSPolicy();
    public static final FIFOPolicy FIFO = new FIFOPolicy();
    public static final AuctionPolicy auction = new AuctionPolicy();
    public static final RandomPolicy random = new RandomPolicy();
    public static final BackPressureObj backpressure = new BackPressureObj();
    
    
    /**
     * Initializes vehicle priority
     * @param node
     * @param vehicle 
     */
    public abstract void initialize(TBR node, Vehicle vehicle);
    
    /**
     * Vehicles sorted first have reservation priority
     * @param v1
     * @param v2
     * @return sorting index for vehicles
     */
    public abstract int compare(Vehicle v1, Vehicle v2);
    
    // action taken if accepted
    public void onAccept(Vehicle v){}
    
    
    /**
     * to use vehicles behind, set this to return true, then use initialize() method below
     */
    public boolean usesVehiclesBehind()
    {
        return false;
    }
    
    /**
     * Initializes vehicle priority using a list of vehicles behind. See usesVehiclesBehind() to activate this
     * @param node
     * @param vehicle
     * @param queue 
     */
    public void initialize(TBR node, Vehicle vehicle, List<Vehicle> queue){}
            
    /**
     * Resets this policy for the next simulation
     */
    public void reset(){}
    
    /**
    * initialization work before scanning vehicle list
    */
    public void initialize(Node n){}
}
