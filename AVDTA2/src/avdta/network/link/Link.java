/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;
import avdta.util.RunningAvg;
import avdta.network.Network;
import avdta.vehicle.Vehicle;
import avdta.network.Network;
import avdta.network.Simulator;
import avdta.network.node.Intersection;
import avdta.network.node.Location;
import avdta.network.node.TBR;
import avdta.vehicle.DriverType;
import avdta.vehicle.fuel.ICV;
import java.awt.Color;
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
    public boolean added;

    
    // elevation
    private double grade;
    
    // upstream cumulative count
    public int[] flowin;
    
    private Location[] coords;
    
    private boolean selected;
    
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
        
        coords = new Location[2];
        coords[0] = source;
        coords[1] = dest;
        
        
        
        
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
     * Returns the angle this link makes with its downstream node 
     * @return the angle this link makes with its downstream node in radians
     */
    public double getIncomingAngle()
    {
        if(coords.length >= 2)
        {
            return coords[coords.length-2].angleTo(coords[coords.length-1]);
        }
        else
        {
            return getDirection();
        }
    }
    
    /**
     * Returns whether this link has a transit lane for transit
     * @return 
     * @see TransitLane
     */
    public boolean hasTransitLane()
    {
        return (this instanceof SplitCTMLink);
    }
    
    /**
     * Returns the angle this link makes with its upstream node
     * @return the angle this link makes with its upstream node in radians
     */
    public double getOutgoingAngle()
    {
        if(coords.length >= 2)
        {
            return coords[0].angleTo(coords[1]);
        }
        else
        {
            return getDirection();
        }
    }
    
    /**
     * Returns the distance to a separate {@link Location}. 
     * This is based on approximating the link as a straight line segment.
     * Used for visualization.
     * @param loc the specified {@link Location}
     * @return the distance to the specified {@link Location}
     */
    public double distanceTo(Location loc)
    {
        return pDistance(loc.getX(), loc.getY(), source.getX(), source.getY(), dest.getX(), dest.getY());
    }
    
    private double pDistance(double x, double y,  double x1, double y1, double x2, double y2) 
    {

        double A = x - x1;
        double B = y - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = -1;
        if (len_sq != 0) //in case of 0 length line
            param = dot / len_sq;

        double xx, yy;

        if (param < 0) {
          xx = x1;
          yy = y1;
        }
        else if (param > 1) {
          xx = x2;
          yy = y2;
        }
        else {
          xx = x1 + param * C;
          yy = y1 + param * D;
        }

        double dx = x - xx;
        double dy = y - yy;
        return Math.sqrt(dx * dx + dy * dy);
      }
    
    /**
     * Creates a {@link LinkRecord}, which is used for manipulating and writing the data associated with a {@link Link}
     * @return a {@link LinkRecord} of this {@link Link}
     */
    public LinkRecord createLinkRecord()
    {
        return new LinkRecord(getId(), getType(), getSource().getId(), getDest().getId(), getLength(), getFFSpeed(), getWaveSpeed(), 
                getCapacityPerLane(), getNumLanes());
    }
    
    /**
     * Returns whether this {@link Link} is selected for visualization
     * @return whether this {@link Link} is selected for visualization
     */
    public boolean isSelected()
    {
        return selected;
    }
    
    /**
     * Updates whether this {@link Link} is selected for visualization
     * @param s whether this {@link Link} is selected for visualization
     */
    public void setSelected(boolean s)
    {
        selected = s;
    }
    
    /**
     * Returns the {@color Color} used to draw this {@link Link} in visualization
     * @return the {@color Color} used to draw this {@link Link} in visualization
     */
    public Color getColor()
    {
        return selected? Color.red : Color.black;
    }
    
    /**
     * Returns the width used to draw this {@link Link} in visualization
     * @return the width used to draw this {@link Link} in visualization
     */
    public int getWidth()
    {
        return selected? 5 : 3;
    }
    
    /**
     * Updates the coordinates of this {@link Link}. 
     * For visualization purposes, links can have multiple coordinates besides the upstream and downstream nodes.
     * @param array the new coordinates
     */
    public void setCoordinates(Location[] array)
    {
        coords = array;
        
        coords[0] = source;
        coords[coords.length-1] = dest;
    }
    
    /**
     * Updates the coordinates of this {@link Link}. 
     * For visualization purposes, links can have multiple coordinates besides the upstream and downstream nodes.
     * @param list the new coordinates
     */
    public void setCoordinates(List<Location> list)
    {
        coords = new Location[list.size()];
        
        int idx = 0;
        
        for(Location l : list)
        {
            coords[idx++] = l;
        }
        
        coords[0] = source;
        coords[coords.length-1] = dest;
    }
    
    /**
     * 
     * @return an int corresponding to the type of the link
     */
    public abstract int getType();
   
    /**
     * Returns the effective queue length on this {@link Link}
     * @return the effective queue length on this {@link Link}
     */
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
     * Returns the number of lanes available at the downstream end of this {@link Link}, which may change due to dynamic lane reversal.
     * @return the number of lanes available at the downstream end
     */
    public int getDsLanes()
    {
        return numLanes;
    }
    
    /**
     * Returns the number of lanes available at the upstream end of this {@link Link}, which may change due to dynamic lane reversal.
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
    
    /**
     * Returns the average grade (change in elevation)
     * @return the average grade (change in elevation) in percent
     */
    public double getAvgGrade()
    {
        return (dest.getElevation() - source.getElevation()) / (getLength() * 5280);
    }
    
    /**
     * Returns the average grade (change in elevation)
     * @return the average grade of this link in radians
     */
    public double getGrade()
    {
        return grade;
    }
    
    /**
     * Returns the queue length on this {@link Link}
     * @return the queue length on this {@link Link}
     */
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
    

    /*
    * @return the receiving flow scale for v
    */
    public double scaleReceivingFlow(Vehicle v)
    {
        return 1.0;
    }
    
    /**
     * Returns the coordinates representing this {@link Link} for visualization
     * @return the coordinates representing this {@link Link}
     */
    public Location[] getCoordinates()
    {
        return coords;
    }
    
    /**
     * Updates the capacity per lane of this {@link Link} (veh/hr)
     * @param capacity the new capacity
     */
    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }
    
    /**
     * Returns the downstream cumulative count for this {@link Link}
     * @return the downstream cumulative count for this {@link Link}
     */
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
     * Returns the upstream cumulative count to his {@link Link} at the specified time
     * @param t the time in seconds
     * @return upstream cumulative count
     */
    public int getFlowin(int t)
    {
        return flowin[t / Simulator.ast_duration];
    }
    
    /**
     * Calculate the upstream cumulative counts based on input data
     */
    public void postProcessFlowin()
    {
        for(int i = 1; i < flowin.length; i++)
        {
            flowin[i] += flowin[i-1];
        }
    }
    
    /**
     * Returns average flow at specified time
     * @param t the time (seconds)
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
     * Returns average direction of the link based on upstream and downstream {@link Node} coordinates
     * @return heading (radians) of the link, based on node coordinates
     */
    public double getDirection()
    {
        return source.angleTo(dest);
    }
    
    /**
     * Returns the id
     * @return id
     */
    public String toString()
    {
        return ""+id;
    }
    
    /**
     * Returns the id
     * @return id
     */
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
     * Returns the number of {@link Vehicle}s that could exit the link this time step
     * @return the size of the sending flow set
     */
    public abstract int getNumSendingFlow();
    
    /**
     * Returns the list of {@link Vehicle}s that could exit the link this time step
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
     * Returns all stored average travel times
     * @return an array of {@link RunningAvg} containing stored travel times
     */
    public RunningAvg[] getAvgTTs()
    {
        return avgTT;
    }
    
    /**
     * Sets the average travel time at the specified time to the specified value
     * @param enter the time (seconds)
     * @param tt experienced travel time (seconds)
     */
    public void setAvgTT(int enter, double tt)
    {
        int idx = (int)Math.min(Simulator.num_asts-1, enter / Simulator.ast_duration);
        
        if(idx >= 0 && idx < avgTT.length && avgTT[idx].getCount() > 0)
        {
            avgTT[idx].setValue(Math.max(getFFTime(), tt));
        }
    }
    
    /**
     * Returns the average toll at the specified time
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
     * Returns the toll at the specified time
     * @param time Time at which the toll is to be found.
     * @return the actual toll experienced at the specified time
     */
    public double getToll(int time)
    {
        return toll;
    }
    
    /**
     * Sets the toll. Time-dependent tolls are not supported within the {@link Link} class
     * @param time the time (seconds)
     * @param t the new toll ($)
     */
    public void setToll(int time, double t)
    {
        this.toll = t;
    }

    /**
     * Returns the average speed at the specified time
     * @param enter An integer indicating the time entered.
     * @return average speed for vehicles entering at specified time, based on average travel time
     */
    public double getAvgSpeed(int enter)
    {
        return length / (getAvgTT(enter) / 3600.0);
    }
    
    /**
     * Returns the average energy consumption at the specified time
     * @param enter An integer indicating the time entered.
     * @return average energy consumption for vehicles entering at specified time
     */
    public double getAvgEnergy(int enter)
    {  
        return length * 14.58 * Math.pow(getAvgSpeed(enter), -0.6253);
    }
    
    /**
     * Returns the average fuel consumption at the specified time
     * @param enter An integer indicating the time entered.
     * @return average fuel consumption for vehicles entering at specified time
     */
    public double getAvgFuel(int enter)
    {
        return getAvgEnergy(enter) / ICV.ENERGY_PER_GAL * ICV.FUELCOST;
    }
    
    /**
     * Returns the number of vehicles that could enter the link this time step
     * @return receiving flow
     */
    public abstract double getReceivingFlow();
    
    
    /**
     * Returns the upstream node for this link
     * @return upstream node for this link
     */
    public Node getSource()
    {
        return source;
    }
    
    /**
     * Returns the downstream node for this link
     * @return downstream node for this link
     */
    public Node getDest()
    {
        return dest;
    }
    
    /**
     * Updates the downstream node for this link
     * @param d the new downstream node
     */
    public void setDest(Node d)
    {
        dest.getIncoming().remove(this);
        dest = d;
        dest.addLink(this);
    }
    
    /**
     * Updates the upstream node for this link
     * @param o the new upstream node
     */
    public void setSource(Node o)
    {
        source.getOutgoing().remove(this);
        source = o;
        source.addLink(this);
    }
    
    /**
     * Returns the total capacity 
     * @return total capacity (not per lane) (veh/hr)
     */
    public double getCapacity()
    {
        return getCapacityPerLane() * numLanes;
    }
    
    /**
     * Returns the capacity per lane 
     * @return capacity per lane (veh/hr)
     */
    public double getCapacityPerLane()
    {
        return capacity;
    }
    
    /**
     * Returns capacity per lane per time step
     * @return capacity per lane, scaled by timestep (veh/s)
     */
    public double getCapacityPerTimestep()
    {
        return getCapacity() * Network.dt/3600.0;
    }

    /**
     * Returns the free flow travel time
     * @return free flow travel time (seconds)
     */
    public double getFFTime()
    {
        return length / ffspd * 3600.0;
    }
    
    /**
     * Returns the free flow speed
     * @return free flow speed (mi/hr)
     */
    public double getFFSpeed()
    {
        return ffspd;
    }
    
    /**
     * Returns the congested wave speed
     * @return the congested wave speed (mi/hr)
     */
    public double getWaveSpeed()
    {
        return wavespd;
    }
    
    /**
     * Updates the congested wave speed
     * @param w the new congested wave speed (mi/hr)
     */
    public void setWaveSpeed(double w)
    {
        wavespd = w;
    }
    
    /**
     * Returns the number of lanes. 
     * Note that the number of lanes per cell may vary due to dynamic lane reversal.
     * @return the number of lanes
     */
    public int getNumLanes()
    {
        return numLanes;
    }
    
    /**
     * Returns the total jam density
     * @return total jam density (not per lane) (veh/mi)
     */
    public double getJamDensity()
    {
        return jamd * numLanes;
    }
    
    /**
     * Returns the jam density per lane 
     * @return jam density per lane (veh/mi)
     */
    public double getJamDensityPerLane()
    {
        return jamd;
    }
    
    /**
     * Returns the length of the link
     * @return length of the link (mi)
     */
    public double getLength()
    {
        return length;
    }
    
    /**
     * Returns the average density of the link
     * @return average density of this link (veh/mi)
     */
    public double getDensity()
    {
        return getOccupancy() / getLength();
    }
    
    /**
     * Returns the number of vehicles on this link
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
     * Returns the sum of sending flow from upstream node's incoming links. Used for dynamic lane reversal.
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
    
    /**
     * Orders links based on id
     * @param rhs the link being compared to
     * @return order based on link id
     */
    public int compareTo(Link rhs)
    {
        return id - rhs.id;
    }
    
    /**
     * Returns whether the given {@link DriverType} can use this {@link Link}. 
     * It depends on the downstream intersection control; human vehicles cannot use reservations unless the parameter is set.
     * @param driver the {@link DriverType} specifying human or autonomous
     * @return whether the driver can use this {@link Link}
     */
    public boolean canUseLink(DriverType driver)
    {
        if((dest instanceof Intersection) &&
            (((Intersection)dest).getControl() instanceof TBR))
        {
            return driver.isAV() || Simulator.getHVsUseReservations();
        }
        else
        {
            return true;
        }
    }
    
}
