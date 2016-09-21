/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.node.obj.BackPressureObj;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;
import avdta.network.node.obj.P0Obj;
import avdta.network.node.policy.RandomPolicy;
import java.util.Comparator;
import java.util.Collection;
import java.util.List;

/**
 * An abstract prioritization of vehicles in reservation controls. 
 * This class also contains instantiations of commonly used policies and objectives. 
 * There is no need to create distinct policies or objectives for each intersection.
 * @author Michael
 */
public abstract class IntersectionPolicy implements Comparator<Vehicle>
{
    public static final FCFSPolicy FCFS = new FCFSPolicy();
    public static final FIFOPolicy FIFO = new FIFOPolicy();
    public static final AuctionPolicy auction = new AuctionPolicy();
    public static final RandomPolicy random = new RandomPolicy();
    public static final BackPressureObj backpressure = new BackPressureObj();
    public static final P0Obj P0 = new P0Obj();
    
    /**
     * Initializes {@link Vehicle} priority for the specified {@link Node} and {@link Vehicle}
     * @param node the intersection at which the {@link Vehicle} is waiting.
     * @param vehicle the {@link Vehicle} to be initialized
     */
    public abstract void initialize(TBR node, Vehicle vehicle);
    
    /**
     * Compares two {@link Vehicle}s according to their reservation priority. {@link Vehicle}s are listed in order of decreasing priority.
     * @param v1 the first vehicle being compared
     * @param v2 the second vehicle being compared
     * @return sorting index for vehicles
     */
    public abstract int compare(Vehicle v1, Vehicle v2);
    
    /**
     * Any actions taken when the {@link Vehicle}'s reservation is accepted.
     * @param v the {@link Vehicle} with an accepted reservation
     */
    public void onAccept(Vehicle v){}
    
    
    /**
     * To use the list of vehicles waiting behind, set this to return true, then use initialize() method
     * @return false
     */
    public boolean usesVehiclesBehind()
    {
        return false;
    }
    
    /**
     * Initializes vehicle priority using a list of vehicles behind. See {@link IntersectionPolicy#usesVehiclesBehind()} to activate this
     * @param node the {@link Node} this priority applies to
     * @param vehicle the {@link Vehicle} this priority applies to
     * @param queue the {@link List} of sending flow on the vehicles
     */
    public void initialize(TBR node, Vehicle vehicle, List<Vehicle> queue){}
            
    /**
     * Resets this policy to restart the simulation
     */
    public void reset(){}
    
    /**
    * Initialization work before scanning vehicle list
    * @param n the {@link Node} to be initialized at
    */
    public void initialize(Node n){}
    
    /**
     * Returns the type code associated with this policy
     * @return depends on subclass
     */
    public abstract int getType();
}
