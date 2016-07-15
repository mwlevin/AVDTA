/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package avdta.network.link;

import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import avdta.vehicle.VehTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Michael
 */
public class LTMLink extends Link
{
    private LinkedList<VehTime> queue;
    
    private ChainedArray N_up, N_down;
    
    private boolean init;
    
    private double capacityUp, capacityDown;

    public LTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd = capacity / (jamd - capacity / ffspd), jamd, length, numLanes);
        
        queue = new LinkedList<VehTime>();
        init = false;
    }
    
    public void initialize()
    {
        N_up = new ChainedArray(getUSLookbehind()+2);
        N_down = new ChainedArray(getDSLookBehind()+2);

        this.capacityUp = getCapacity() * Network.dt / 3600.0;
        this.capacityDown = getCapacity() * Network.dt / 3600.0;
        
        init = true;
    }
    
    public int getUSLookbehind()
    {
        return (int)Math.ceil(getLength()/getFFSpeed()*3600 / Network.dt);
    }
    public int getDSLookBehind()
    {
        return (int)Math.ceil(getLength()/getWaveSpeed()*3600 / Network.dt);
    }
    // includes fractions lost to discretization
    public double getCurrentUpstreamCapacity()
    {
        return capacityUp;
    }
    
    public double getCurrentDownstreamCapacity()
    {
        return capacityDown;
    }    
    
    public int getType()
    {
        return ReadNetwork.LTM;
    }
    
    public void reset()
    {
        queue.clear();
        N_up = new ChainedArray(getUSLookbehind()+2);
        N_down = new ChainedArray(getDSLookBehind()+2);

        
        super.reset();
    }
    
    public void step()
    {
        capacityUp -= (int)capacityUp;
        
        capacityUp += getCapacity() * Network.dt / 3600.0;

        capacityDown -= (int)capacityDown;
        
        capacityDown += getCapacity() * Network.dt / 3600.0;
    }
    
    public void addVehicle(Vehicle veh)
    {
        veh.enteredLink(this);
        
        queue.add(new VehTime(veh, Simulator.time));     
        
        addN_up(Simulator.time, 1);

    }
    
    public int getOccupancy()
    {
        return queue.size();
    }
    
    
    public LinkedList<VehTime> getQueue()
    {
        return queue;
    }
    
    public int getNumSendingFlow()
    {
        return (int)Math.min(getN_up(Simulator.time - getLength()/getFFSpeed()*3600 + Network.dt) - 
                getN_down(Simulator.time), getCurrentDownstreamCapacity());
    }
    
    public List<Vehicle> getSendingFlow()
    {
        List<Vehicle> output = new ArrayList<Vehicle>();
        
        int max = getNumSendingFlow();
        
        for(VehTime vt : queue)
        {
            if(max > 0)
            {
                output.add(vt.vehicle);
                
                if(vt.vehicle.arr_time < 0)
                {
                    vt.vehicle.arr_time = Simulator.time;
                    
                    
                }
                
                
                max--;
            }
            else
            {
                break;
            }
        }
        
        S = output.size();
        
        
        return output;
    }
    
    public List<Vehicle> getVehiclesCanMove()
    {
        return getSendingFlow();
    }
    
    public boolean removeVehicle(Vehicle veh)
    {
        Iterator<VehTime> iter = queue.iterator();
        
        while(iter.hasNext())
        {
            VehTime vt = iter.next();
            
            if(vt.vehicle == veh)
            {
                iter.remove();
                addN_down(Simulator.time, 1);
                updateTT(vt.vehicle);
                
                return true;
            }
        }
 
        return false;
    }
    
    public double getReceivingFlow()
    {
        return Math.min(getN_down(Simulator.time - getLength()/getWaveSpeed()*3600 + Network.dt) 
                + getJamDensity() * getLength() - getN_up(Simulator.time),
                getCurrentUpstreamCapacity());
    }
    
    public int getNumWaiting()
    {
        return getN_up(Simulator.time - getLength()/getFFSpeed()*3600 + Network.dt) - getN_down(Simulator.time);
    }
    
    public void addN_up(double t, int val)
    {
        N_up.add(Simulator.indexTime(t), val);
    }
    
    public void addN_down(double t, int val)
    {
        N_down.add(Simulator.indexTime(t), val);
    }
    

    public int getN_up(double t)
    {
        if(t < 0)
        {
            return 0;
        }
        else
        {
            return N_up.get(Simulator.indexTime(t));
        }
    }
    
    public int getN_down(double t)
    {
        if(t < 0)
        {
            return 0;
        }
        else
        {
            return N_down.get(Simulator.indexTime(t));
        }
    }
}
