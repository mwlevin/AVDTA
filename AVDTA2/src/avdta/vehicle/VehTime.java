/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import java.io.Serializable;

/**
 *
 * @author ut
 */
public class VehTime implements Serializable
{
    public Vehicle vehicle;
    public int time;
    
    public VehTime(Vehicle v, int t)
    {
        this.vehicle = v;
        this.time = t;
    }
}
