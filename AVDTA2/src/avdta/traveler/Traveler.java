/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.traveler;

import avdta.network.Simulator;
import avdta.network.node.Zone;
import avdta.sav.SAVDest;
import avdta.sav.SAVOrigin;
import avdta.sav.SAVTraveler;
import java.io.Serializable;

/**
 *
 * @author micha
 */
public class Traveler implements Comparable<Traveler>, Serializable
{
    private int id;
    private Zone origin;
    private Zone dest;
    private int dtime;
    
    private int enter_time, exit_time;
    private double vot;
    
    /**
     * Constructs the traveler with the specified parameters.
     * @param id the id
     * @param origin the origin
     * @param dest the destination
     * @param dtime the departure time
     */
    public Traveler(int id, Zone origin, Zone dest, int dtime, double vot)
    {
        this.id = id;
        this.origin = origin;
        this.dest = dest;
        this.dtime = dtime;
        this.vot = vot;
    }
    
    public String toString()
    {
        return ""+id;
    }
    
    public double getVOT()
    {
        return vot;
    }
    
    public void setVOT(double vot)
    {
        this.vot = vot;
    }
    
    public void setEnterTime(int enter)
    {
        enter_time = enter;
    }
    
    public int getEnterTime()
    {
        return enter_time;
    }
    
    /**
     * Returns the traveler's departure time.
     * @return the traveler's departure time
     */
    public int getDepTime()
    {
        return dtime;
    }
    
    /**
     * Orders travelers according to departure time, then id.
     * @param rhs the traveler to be compared against
     * @return traveler order
     */
    public int compareTo(Traveler rhs)
    {
        if(rhs.dtime != dtime)
        {
            return dtime - rhs.dtime;
        }
        else
        {
            return id - rhs.id;
        }
    }
    
    public boolean equals(Object o)
    {
        Traveler rhs = (Traveler)o;
        
        return rhs.id == id;
    }
    
    /**
     * Resets the traveler to restart the simulation.
     */
    public void reset()
    {
        enter_time = -1;
        exit_time = Simulator.duration;
    }
    
    /**
     * Returns the id.
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public Zone getOrigin()
    {
        return origin;
    }
    
    public Zone getDest()
    {
        return dest;
    }
    
    /**
     * Checks whether this traveler has exited.
     * @return if this traveler has exited
     */
    public boolean isExited()
    {
        return exit_time > 0;
    }
    
    /**
     * This method is called when the traveler exits a taxi.
     * The exit time is saved as the traveler exit time.
     */
    public void exited()
    {
        exit_time = Simulator.time;
    }
    
    /**
     * This returns the traveler waiting time.
     * The waiting time is the difference between the departure time and taxi enter time.
     * @return traveler waiting time
     */
    public int getDelay()
    {
        return enter_time - dtime;
    }
    
    /**
     * This returns the total travel time.
     * The total travel time is the difference between the exit time and departure time.
     * @return the total travel time
     */
    public int getTT()
    {
        return exit_time - dtime;
    }
    
    /**
     * This returns the in-vehicle travel time.
     * The in-vehicle travel time is the difference between the exit time and taxi enter time.
     * @return the in-vehicle travel time.
     */
    public int getIVTT()
    {
        return exit_time - enter_time;
    }
}
