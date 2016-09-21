/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import java.io.Serializable;
import avdta.network.link.LTMLink;

/**
 * This is a tuple of a {@link Vehicle} and an arrival time. 
 * This is used by the {@link LTMLink} class to check when {@link Vehicle}s have spent at least the free flow travel time on the link.
 * @author Michael
 */
public class VehTime implements Serializable
{
    public Vehicle vehicle;
    public int time;
    
    /**
     * Constructs the {@link VehTime} with the given parameters
     * @param v the {@link Vehicle}
     * @param t the arrival time
     */
    public VehTime(Vehicle v, int t)
    {
        this.vehicle = v;
        this.time = t;
    }
}
