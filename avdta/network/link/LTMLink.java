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
import avdta.network.node.Highway;
import avdta.network.node.Intersection;
import avdta.network.node.IntersectionControl;
import avdta.network.type.Type;
import avdta.vehicle.Vehicle;
import avdta.vehicle.VehTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the link transmission model. 
 * It uses {@link ChainedArray}s to store cumulative counts at the upstream and downstream ends, and stores vehicles in a {@link LinkedList}.
 * @author Michael
 */
public class LTMLink extends Link
{
    private LinkedList<VehTime> queue;
    
    private CumulativeCountStorage N_up, N_down;
    
    private boolean init;
    
    private double capacityUp, capacityDown;

    /**
     * Constructs the link with the given parameters 
     * @param id the link id
     * @param source the source node
     * @param dest the destination node
     * @param capacity the capacity per lane (veh/hr)
     * @param ffspd the free flow speed (mi/hr)
     * @param wavespd the congested wave speed (mi/hr)
     * @param jamd the jam density (veh/mi)
     * @param length the length (mi)
     * @param numLanes the number of lanes
     */
    public LTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd = capacity / (jamd - capacity / ffspd), jamd, length, numLanes);
        //super(id, source, dest, capacity = ffspd * (wavespd/2 * jamd) / (ffspd + wavespd/2), ffspd, wavespd/2, jamd, length, numLanes);
        //super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);


        queue = new LinkedList<VehTime>();
        init = false;
    }
    
    public Iterable<Vehicle> getVehicles()
    {
        return new LTMIterable();
    }
    
    /**
     * Initializes this {@link LTMLink} after all data is read.
     * This creates the {@link ChainedArray}s used to store cumulative counts.
     */
    public void initialize()
    {
        N_up = new ChainedArray(getUSLookBehind()+2);
        N_down = new ChainedArray(getDSLookBehind()+2);

        this.capacityUp = getCapacity() * Network.dt / 3600.0;
        this.capacityDown = getCapacity() * Network.dt / 3600.0;
        
        init = true;

    }
    
    /**
     * Returns how far to look backwards in time for the upstream end
     * @return {@link Link#getLength()}/{@link Link#getFFSpeed()} (s)
     */
    public int getUSLookBehind()
    {
        return (int)Math.ceil(getLength()/getFFSpeed()*3600 / Network.dt);
    }
    
    /**
     * Returns how far to look backwards in time for the downstream end
     * @return {@link Link#getLength()}/{@link Link#getWaveSpeed()} (s)
     */
    public int getDSLookBehind()
    {
        return (int)Math.ceil(getLength()/getWaveSpeed()*3600 / Network.dt);
    }
    // includes fractions lost to discretization
    
    /**
     * Returns the current upstream capacity. This includes fractions lost to discretization.
     * @return the current upstream capacity
     */
    public double getCurrentUpstreamCapacity()
    {
        return capacityUp;
    }
    
    /**
     * Returns the current downstream capacity. This includes fractions lost to discretization.
     * @return the current downstream capacity
     */
    public double getCurrentDownstreamCapacity()
    {
        return capacityDown;
    }    
    
    /**
     * Returns the type code of this link
     * @return {@link ReadNetwork#LTM}
     */
    public Type getType()
    {
        return ReadNetwork.LTM;
    }
    
    /**
     * Resets this {@link LTMLink} to restart the simulation. This clears the {@link ChainedArray}s
     */
    public void reset()
    {
        queue.clear();
        N_up.clear();
        N_down.clear();

        
        super.reset();
    }
    
    /**
     * Executes one time step of simulation. 
     * This updates the current upstream and downstream capacities. 
     * Adding and removing vehicles occurs through {@link Node}s.
     */
    public void step()
    {
        capacityUp -= (int)capacityUp;
        
        capacityUp += getCapacity() * Network.dt / 3600.0;

        capacityDown -= (int)capacityDown;
        
        capacityDown += getCapacity() * Network.dt / 3600.0;
        

    }
    
    public void update()
    {
        N_up.nextTimeStep();
        N_down.nextTimeStep();
    }
    
    /**
     * Adds the {@link Vehicle} to this link
     * @param veh the {@link Vehicle} to be added
     */
    public void addVehicle(Vehicle veh)
    {
        veh.enteredLink(this);
        
        queue.add(new VehTime(veh, Simulator.time));     
        
        addN_up(Simulator.time, 1);
        
        

    }
    
    /**
     * Returns the number of {@link Vehicle}s on this link
     * @return the number of {@link Vehicle}s on this link
     */
    public int getOccupancy()
    {
        return queue.size();
    }
    
    /**
     * Returns the queue of {@link Vehicle}s. 
     * The queue is stored as a {@link LinkedList} of {@link VehTime}s, which contain the {@link Vehicle} arrival times used to determine sending flows.
     * @return the queue of {@link Vehicle}s
     */
    public LinkedList<VehTime> getQueue()
    {
        return queue;
    }
    
    /**
     * Returns the number of {@link Vehicle}s that could exit this link.
     * @return the size of the sending flow
     */
    public int getNumSendingFlow()
    {
        
        return (int)Math.min(getN_up(Simulator.time - getLength()/getFFSpeed()*3600 + Network.dt) - 
                getN_down(Simulator.time), getCurrentDownstreamCapacity());
    }
    
    
    /**
     * Returns the set of {@link Vehicle}s that could exit this link
     * @return the sending flow
     */
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
    
    /**
     * Returns the sending flow
     * @return {@link LTMLink#getVehiclesCanMove()}
     */
    public List<Vehicle> getVehiclesCanMove()
    {
        return getSendingFlow();
    }
    
    /**
     * Removes the {@link Vehicle} from this link
     * @param veh the {@link Vehicle} to be removed
     * @return if the {@link Vehicle} was removed
     */
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
    
    /**
     * Returns the receiving flow for this time step
     * @return the receiving flow for this time step
     */
    public double getReceivingFlow()
    {
        
        return Math.min(
                Math.min(Math.round(getN_down(Simulator.time - getLength()/getWaveSpeed()*3600 + Network.dt) 
                + getJamDensity() * getLength() - getN_up(Simulator.time)), getJamDensity()*getLength() - queue.size()),
                getCurrentUpstreamCapacity());
        

    }
    
    
    /**
     * Returns the number of vehicles waiting to exit, i.e. the component of sending flow unbounded by capacity.
     * @return the number of vehicles waiting to exit
     */
    public int getNumWaiting()
    {
        return getN_up(Simulator.time - getLength()/getFFSpeed()*3600 + Network.dt) - getN_down(Simulator.time);
    }
    
    /**
     * Adds to the upstream cumulative count.
     * @param t the time (s)
     * @param val the number of vehicles to add
     */
    public void addN_up(double t, int val)
    {
        N_up.addCC(Simulator.indexTime(t), val);
    }
    
    /**
     * Adds to the downstream cumulative count.
     * @param t the time (s)
     * @param val the number of vehicles to add
     */
    public void addN_down(double t, int val)
    {
        N_down.addCC(Simulator.indexTime(t), val);
    }
    

    /**
     * Returns the upstream cumulative count at the specified time
     * @param t the time (s)
     * @return the upstream cumulative count
     */
    public int getN_up(double t)
    {
        if(t < 0)
        {
            return 0;
        }
        else
        {
            return N_up.getCC(Simulator.indexTime(t));
        }
    }
    
    /**
     * Returns the downstream cumulative count at the specified time
     * @param t the time (s)
     * @return the downstream cumulative count
     */
    public int getN_down(double t)
    {
        if(t < 0)
        {
            return 0;
        }
        else
        {
            return N_down.getCC(Simulator.indexTime(t));
        }
    }
    
    class LTMIterable implements Iterable<Vehicle>
    {
        public Iterator<Vehicle> iterator()
        {
            return new LTMIterator(queue);
        }
    }
}



class LTMIterator implements Iterator<Vehicle>
{
    private Iterator<VehTime> iter;
    public LTMIterator(LinkedList<VehTime> queue)
    {
        this.iter = queue.iterator();
    }
    
    public boolean hasNext()
    {
        return iter.hasNext();
    }
    
    public Vehicle next()
    {
        VehTime next = iter.next();
        return next.vehicle;
    }
    
    public void remove()
    {
        iter.remove();
    }
}



