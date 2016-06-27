/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;
import avdta.network.RunningAvg;
import avdta.network.Network;
import avdta.vehicle.Vehicle;
import avdta.network.Network;
import avdta.network.Simulator;
import avdta.vehicle.fuel.ICV;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * An abstract DTA link. The flow model is not specified, but defines universal 
 * properties. <br>
 * {@code LTM} stands for Link Transmission Model. <br>
 * {@code CTM} for Cell Transmission Model. <br>
 * {@code PQ} for Point-Queue Model. <br>
 * {@code CENTROID} is for links which originate from centroid connectors. <br>
 * {@code numLanes} is for the number of lanes on the link. <br>
 * {@code ffspd} is for the free flow speed. <br>
 * {@code wavespd} is for the wave speed. <br>
 * {@code jamd} is the jam density. <br>
 * {@code avgTT[]} is the average running time of all vehicles over an 
 * assignment interval. <br>
 * {@code avgToll[]} is the average toll. <br>
 * {@code R} and {@code S} are the receiving flow and the sending flow 
 * respectively. <br>
 * {@code q} and {@code Q} are the <b>flow</b> and the <b>capacity</b> 
 * respectively. <br>
 * {@code lanes_blocked} lanes blocked for intersection queuing in Tile-Based
 * Reservation. <br>
 * {@code arr_time} is the arrival time to the end of the link while trying to 
 * find time dependent shortest path. <br>
 * {@code prev} previous {@link Link} for shortest path related things. <br>
 * {@code grade} denotes the elevation of the link.
 * @author Michael
 */
public abstract class Link implements Serializable, Comparable<Link>
{
    public static final int LTM = 1;    //Link Transmission Model
    public static final int CTM = 2;
    public static final int PQ = 3;
    public static final int CENTROID = 100;
    
    
    
    public double dem;
    
    private int id;
    private Node source, dest;
    
    private int numLanes;
    private double capacity, ffspd, wavespd, jamd, length;
    
    private double toll;
    
    private RunningAvg[] avgTT, avgToll;
    
    public double R, S, q, Q;
    public int lanes_blocked;
    
    public double tempR;
    public int tempLanesBlocked;
    
    public double pressure;
    public int lastLinkCheck; 
    
    
    // arr_time, label refer to end of link
    public double label;
    public int arr_time;
    public Link prev;

    
    // elevation
    private double grade;
    
    // upstream cumulative count
    public int[] flowin;
    
    public Link(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        this.id = id;
        this.source = source;
        this.dest = dest;
        
        this.capacity = capacity;
        this.numLanes = numLanes;
        this.ffspd = ffspd;
        this.wavespd = wavespd;
        this.jamd = jamd;
        this.length = length;
        this.toll = 0;
        
        source.addLink(this);
        dest.addLink(this);
        
        
        
        avgTT = new RunningAvg[(int)Math.ceil(Simulator.duration / Simulator.ast_duration + 1)];
        
        for(int i = 0; i < avgTT.length; i++)
        {
            avgTT[i] = new RunningAvg();
        }

        avgToll = new RunningAvg[avgTT.length];
        
        for(int i = 0; i < avgToll.length; i++)
        {
            avgToll[i] = new RunningAvg();
        }
        
        flowin = new int[(int)Math.ceil((double)Simulator.duration / Simulator.ast_duration)+1];
        
    }
    
    /**
     * 
     * @return an int corresponding to the type of the link
     */
    public abstract int getType();
    
    /**
     * @return A description of the type of the link
     */
    public String strType()
    {
        switch(getType())
        {
            case CTM: return "CTM";
            case LTM: return "LTM";
            case PQ: return "PQ";
            case CENTROID: return "centroid";
            default: return "null";
        }
    }
            
    public int getEffQueueLength()
    {
        return 0;
    }
    /**
     * Returns flow which can be still sent (some vehicles have already used 
     * some part of the sending flow/turning movement flow).
     * @return A double indicating the remaining flow in that time step.
     */
    public double getRemainingTimestepFlow()
    {
        return R;
    }
    
    /**
     * 
     * @return the number of lanes available at the downstream end
     */
    public int getDsLanes()
    {
        return numLanes;
    }
    
    /**
     * 
     * @return the number of lanes available at the upstream end 
     */
    public int getUsLanes()
    {
        return numLanes;
    }
    
    /**
     * Initialize grade based on node elevations
     */
    public void setGrade()
    {
        if(!(this instanceof CentroidConnector))
        {
            grade = Math.atan((dest.getElevation() - source.getElevation()) / getLength());
        }
        else
        {
            grade = 0;
        }
    }
    
    public double getAvgGrade()
    {
        return (dest.getElevation() - source.getElevation()) / (getLength() * 5280);
    }
    
    /**
     * 
     * @return the average grade of this link 
     */
    public double getGrade()
    {
        return grade;
    }
    
    public int getQueueLength()
    {
        return 0;
    }
    
    /**
     * 
     * @return if this link is a centroid connector
     */
    public boolean isCentroidConnector()
    {
        return false;
    }
    
    /**
     * Used to update the average travel time of this link based on vehicle 
     * observations.
     * @param v A {@link Vehicle} for which travel times are to be updated.
     */
    public void updateTT(Vehicle v)
    {
    	updateTT(v.enter_time, Simulator.time + Network.dt);
        flowin[v.enter_time / Simulator.ast_duration +1]++;
    }
    

    
    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }
    
    public int getN()
    {
        int count = 0;
        for(RunningAvg r : avgTT)
        {
            count += r.getCount();
        }
        return count;
    }
    
    /**
     * Reset this link to restart the simulation
     */
    public void reset()
    {        
        for(RunningAvg r : avgTT)
        {
            r.reset();
        }
        
        for(int i = 0; i < flowin.length; i++)
        {
            flowin[i] = 0;
        }
        
    }
    
    /**
     * Initialize this link at the start of simulation
     */
    public void initialize()
    {
        for(int i = 0; i < avgTT.length; i++)
        {
            avgTT[i].reset();
        }
    }
    
    /**
     * @param t 
     * @return upstream cumulative count
     */
    public int getFlowin(int t)
    {
        return flowin[t / Simulator.ast_duration];
    }
    
    public void postProcessFlowin()
    {
        for(int i = 1; i < flowin.length; i++)
        {
            flowin[i] += flowin[i-1];
        }
    }
    
    /**
     * 
     * @param t
     * @return average flow in at t, in vph, based on upstream cumulative counts
     */
    public double getAvgFlow(int t)
    {
        int idx_bot = t / Simulator.ast_duration;
        int idx_top = t / Simulator.ast_duration+1;
        
        if(idx_top < flowin.length)
        {
            return (double)(flowin[idx_top] - flowin[idx_bot]) / (Simulator.ast_duration / 3600.0);
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * 
     * @return heading (radians) of the link, based on node coordinates
     */
    public double getDirection()
    {
        double output = Math.atan2(dest.getY() - source.getY(), dest.getX() - source.getX());
        
        if(output < 0)
        {
            output += 2*Math.PI;
        }
        
        return output;
    }
    
    /**
     * 
     * @return id
     */
    public String toString()
    {
        return ""+id;
    }
    
    public int getId()
    {
        return id;
    }
    
    
    public int hashCode()
    {
        return id;
    }
    
    /**
     * Adds a vehicle to the upstream end of this link
     * @param veh The {@link Vehicle} which is to be added.
     */
    public abstract void addVehicle(Vehicle veh);
    
    
    
    
    /**
     * 
     * @return the size of the sending flow set
     */
    public abstract int getNumSendingFlow();
    
    /**
     * 
     * @return sending flow, sorted by arrival time for FIFO
     */
    public abstract List<Vehicle> getSendingFlow();
    
    public abstract List<Vehicle> getVehiclesCanMove();
    
    /**
     * Removes a vehicle from the downstream end of the link.
     * @param veh The {@link Vehicle} which is to be removed.
     * @return whether the vehicle was removed
     */
    public abstract boolean removeVehicle(Vehicle veh);
    
    /**
     * Adds an observation to update average travel time.
     * @param enter An integer indicating the entry time.
     * @param exit An interger indicating the exit time.
     */
    public void updateTT(int enter, int exit)
    {
        avgTT[enter / Simulator.ast_duration].add(exit - enter);
    }
    
    /**
     * <b>Function overloading</b> in cases where the entry time is double instead 
     * of being an integer.
     * @param enter A double indicating the time entered.
     * @return average travel time for a vehicle entering at the specified time
     */
    public double getAvgTT(double enter)
    {
        return getAvgTT((int)enter);
    }
    
    /**
     * 
     * @param enter An integer indicating the time entered.
     * @return average travel time for a vehicle entering at the specified time
     */
    public double getAvgTT(int enter)
    {
        int idx = (int)Math.min(Simulator.num_asts-1, enter / Simulator.ast_duration);
        
        if(idx >= 0 && idx < avgTT.length && avgTT[idx].getCount() > 0)
        {
            return Math.max(getFFTime(),avgTT[idx].getAverage());
        }
        else
        {
            return getFFTime();
        }
    }
    
    /**
     * 
     * @param enter An integer indicating the time entered.
     * @return average toll for a vehicle entering at the specified time
     */
    public double getAvgToll(int enter)
    {
        int idx = (int)Math.min(Simulator.num_asts-1, enter / Simulator.ast_duration);
        
        if(avgTT[idx].getCount() > 0)
        {
            return avgToll[idx].getAverage();
        }
        else
        {
            return 0;
        }
    }
    
    /**
     * 
     * @param time Time at which the toll is to be found.
     * @return the actual toll experienced at the specified time
     */
    public double getToll(int time)
    {
        return toll;
    }
    
    
    public void setToll(int time, double t)
    {
        this.toll = t;
    }

    /**
     * 
     * @param enter An integer indicating the time entered.
     * @return average speed for vehicles entering at specified time, based on average travel time
     */
    public double getAvgSpeed(int enter)
    {
        return length / (getAvgTT(enter) / 3600.0);
    }
    
    /**
     * 
     * @param enter An integer indicating the time entered.
     * @return average energy consumption for vehicles entering at specified time
     */
    public double getAvgEnergy(int enter)
    {  
        return length * 14.58 * Math.pow(getAvgSpeed(enter), -0.6253);
    }
    
    /**
     * 
     * @param enter An integer indicating the time entered.
     * @return average fuel consumption for vehicles entering at specified time
     */
    public double getAvgFuel(int enter)
    {
        return getAvgEnergy(enter) / ICV.ENERGY_PER_GAL * ICV.FUELCOST;
    }
    
    /**
     * 
     * @return receiving flow
     */
    public abstract double getReceivingFlow();
    
    
    /**
     * 
     * @return upstream node for this link
     */
    public Node getSource()
    {
        return source;
    }
    
    /**
     * 
     * @return downstream node for this link
     */
    public Node getDest()
    {
        return dest;
    }
    
    public void setDest(Node d)
    {
        dest = d;
    }
    
    public void setSource(Node o)
    {
        source = o;
    }
    
    /**
     * 
     * @return total capacity (not per lane)
     */
    public double getCapacity()
    {
        return getCapacityPerLane() * numLanes;
    }
    
    /**
     * 
     * @return capacity per lane
     */
    public double getCapacityPerLane()
    {
        return capacity;
    }
    
    /**
     * 
     * @return capacity per lane, scaled by timestep
     */
    public double getCapacityPerTimestep()
    {
        return getCapacity() * Network.dt/3600.0;
    }

    /**
     * 
     * @return free flow travel time in seconds
     */
    public double getFFTime()
    {
        return length / ffspd * 3600.0;
    }
    
    /**
     * 
     * @return free flow speed, mph
     */
    public double getFFSpeed()
    {
        return ffspd;
    }
    
    /**
     * 
     * @return wave speed, mph
     */
    public double getWaveSpeed()
    {
        return wavespd;
    }
    
    public void setWaveSpeed(double w)
    {
        wavespd = w;
    }
    
    public int getNumLanes()
    {
        return numLanes;
    }
    
    /**
     * 
     * @return total jam density (not per lane)
     */
    public double getJamDensity()
    {
        return jamd * numLanes;
    }
    
    /**
     * 
     * @return jam density per lane
     */
    public double getJamDensityPerLane()
    {
        return jamd;
    }
    
    /**
     * 
     * @return length of the link, in miles
     */
    public double getLength()
    {
        return length;
    }
    
    /**
     * 
     * @return average density of this link
     */
    public double getDensity()
    {
        return getOccupancy() / getLength();
    }
    
    /**
     * 
     * @return number of vehicles on this link
     */
    public abstract int getOccupancy();
    
    /**
     * Prepare to change the state of this link in simulation
     */
    public void prepare(){}
    
    /**
     * Calculates state of this link in the next timestep
     */
    public void step(){}
    
    /**
     * Updates the state of this link based on calculations in step()
     */
    public void update(){}
    
    /**
     * 
     * @return sum of sending flow from upstream node's incoming links
     */
    public int getUsSendingFlow()
    {
        // count upstream sending flow
        int incoming = 0;
        for(Link i : getSource().getIncoming())
        {
            for(Vehicle v : i.getSendingFlow())
            {
                if(v.getNextLink() == this)
                {
                    incoming++;
                }
            }
        }
        
        return incoming;
    }
    
    public int compareTo(Link rhs)
    {
        return id - rhs.id;
    }
    
}
