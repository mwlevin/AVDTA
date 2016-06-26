/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Simulator;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import avdta.network.node.Node;

/**
 *
 * @author Michael
 */
public class CentroidConnector extends Link
{
    private LinkedList<Vehicle> queue;
    
    public CentroidConnector(int id, Node source, Node dest)
    {
        super(id, source, dest, 0, 0, 0, 0, 0, 1);
        
        queue = new LinkedList<Vehicle>();
    }
    
    public double getAvgEnergy(int enter)
    {  
        return 0;
    }
    
    public double getCapacityPerLane()
    {
        return 9000;
    }
    
    public double getFFTime()
    {
        return 0;
    }
    
    public void reset()
    {
        queue.clear();
        super.reset();
    }
    
    public double getReceivingFlow()
    {
        return Integer.MAX_VALUE;
    }
    
    public int getNumSendingFlow()
    {
        return queue.size();
    }
    
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
    
    public List<Vehicle> getVehiclesCanMove()
    {
        return getSendingFlow();
    }
    
    public int getOccupancy()
    {
        return queue.size();
    }
    
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
    
    public void addVehicle(Vehicle v)
    {
        queue.add(v);
        
        v.enteredLink(this);
        
        Link j = v.getNextLink();
        
        if(j != null && j instanceof CTMLink)
        {
            ((CTMLink)j).addToUsSendingFlow();
        }
    }
    
    public int getType()
    {
        return Link.CENTROID;
    }
    
    public boolean isCentroidConnector()
    {
        return true;
    }
}
