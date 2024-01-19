/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.network.node.Intersection;
import avdta.network.type.Type;

/**
 * This represents a centroid connector, which connects a {@link Intersection} to a {@link Zone} or vice versa.
 * Centroid connectors do not have capacity or jam density constraints.
 * Vehicles always travel at free flow on a centroid connector, but may not be able to exit the connector onto the network immediately.
 * This is implemented as a point queue.
 * @author Michael
 */
public class CentroidConnector extends Link
{
    private LinkedList<Vehicle> queue;
    
    /**
     * Constructs the centroid connector with the given parameters. Other link parameters are not used.
     * @param id the id
     * @param source the upstream {@link Node}
     * @param dest the downstream {@link Node}
     */
    public CentroidConnector(int id, Node source, Node dest)
    {
        super(id, source, dest, 0, 0, 0, 0, 0, 1);
        
        queue = new LinkedList<Vehicle>();
    }

    public Iterable<Vehicle> getVehicles()
    {
        // these vehicles will exit automatically, so don't include them in pressure-based calculations
        if(getDest().isZone())
        {
            return new ArrayList<Vehicle>();
        }
        else
        {
            return queue;
        }
    }

    
    /**
     * Returns the average energy consumption --- 0.
     * @param enter the arrival time
     * @return 0
     */
    public double getAvgEnergy(int enter)
    {  
        return 0;
    }
    
    /**
     * Returns the centroid associated with this centroid connector. 
     * This may be the source or destination node, depending on which is a centroid.
     * @return the centroid
     */
    public Zone getZone()
    {
        if(getSource() instanceof Zone)
        {
            return (Zone)getSource();
        }
        else
        {
            return (Zone)getDest();
        }
    }
    
    /**
     * Returns the intersection associated with this centroid connector. 
     * This may be the source or destination node, depending on which is an intersection.
     * @return the centroid
     */
    public Intersection getIntersection()
    {
        if(getSource() instanceof Zone)
        {
            return (Intersection)getDest();
        }
        else
        {
            return (Intersection)getSource();
        }
    }
    
    /**
     * Returns the capacity per lane
     * @return 100000
     */
    public double getCapacityPerLane()
    {
        return 100000;
    }
    
    /**
     * Returns the free flow travel time
     * @return 0
     */
    public double getFFTime()
    {
        return Simulator.dt;
    }
    
    /**
     * Resets this link for a new simulation.
     */
    public void reset()
    {
        queue.clear();
        super.reset();
    }
    
    /**
     * Returns the receiving flow
     * @return {@link Integer#MAX_VALUE}
     */
    public double getReceivingFlow()
    {
        return Integer.MAX_VALUE;
    }
    
    /**
     * Returns the sending flow - which is everything on the link.
     * @return the queue size
     */
    public int getNumSendingFlow()
    {
        return queue.size();
    }
    
    /**
     * Returns the list of sending flow, and updates the arrival time of {@link Vehicle}s in the sending flow.
     * @return everything in the queue
     */
    public List<Vehicle> getSendingFlow()
    {
        List<Vehicle> output = new ArrayList<Vehicle>();
        
        for(Vehicle v : queue)
        {
            output.add(v);
            
            if(v.arr_time < 0)
            {
                v.arr_time = Simulator.time;
            }
        }
        
        
        return output;
    }
    
    /**
     * Returns the sending flow
     * @return {@link CentroidConnector#getSendingFlow()}
     */
    public List<Vehicle> getVehiclesCanMove()
    {
        return getSendingFlow();
    }
    
    /**
     * Returns the number of {@link Vehicle}s on this link
     * @return the size of the queue
     */
    public int getOccupancy()
    {
        return queue.size();
    }
    
    public void step() {
//        if (getId() == 425014) {
//            System.out.println("CC 425014. Occupancy: " + getOccupancy());
//        }
//        
//        if (getId() == 325014) {
//            System.out.println("CC 325014. Occupancy: " + getOccupancy());
//        }
    }
    
    /**
     * Removes a {@link Vehicle} from this link.
     * @param v the {@link Vehicle} to be removed
     * @return if the {@link Vehicle} was on this link
     */
    public boolean removeVehicle(Vehicle v)
    {
        if(queue.remove(v))
        {
            updateTT(v);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Adds a {@link Vehicle} to this link
     * @param v the {@link Vehicle} to be added
     */
    public void addVehicle(Vehicle v)
    {
        queue.add(v);
        
        v.enteredLink(this);
        
        Link j = v.getNextLink();
        
        if(j != null && j instanceof DLRCTMLink)
        {
            ((DLRCTMLink)j).addToUsSendingFlow();
        }
    }
    
    /**
     * Returns the type code of this link
     * @return {@link ReadNetwork#CENTROID}
     */
    public Type getType()
    {
        return ReadNetwork.CENTROID;
    }
    
    /**
     * Returns whether this is a centroid connector
     * @return true
     */
    public boolean isCentroidConnector()
    {
        return true;
    }
}
