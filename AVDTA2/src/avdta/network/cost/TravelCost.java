/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.cost;

import avdta.network.link.Link;
import avdta.network.Simulator;
import avdta.network.Network;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;

/**
 * A {@link TravelCost} is an abstract way to calculate per link travel cost, used for finding shortest paths in {@link Network}.
 * It defines the method {@link TravelCost#cost(avdta.network.link.Link, double, int)}, which returns the cost for traveling a link at the specified time with the specified value of time.
 * There is no need to create new instances for each {@link Network}. 
 * Some commonly used {@link TravelCost} subclasses are defined within the {@link TravelCost} class.
 * 
 * @author Michael
 */
public abstract class TravelCost 
{
    public static final TTCost ttCost = new TTCost();
    
    public static final TravelCost generalizedCost = new GenCost();
    
    public static final TravelCost ffTime = new FFTime();
    
    public static final TravelCost dnlTime = new DNLTime();
    public static final TravelCost dnlGenCost = new DNLGenCost();
    public static final TravelCost SPaT_Cost = new SPaT_Cost();
    
    /**
     * Calculates the cost of traveling on {@link Link} l starting at the specified time, with the specified value of time.
     * @param l the {@link Link}
     * @param vot the value of time ($/hr)
     * @param time the time of arrival (s)
     * @return depends on subclass 
     */
    public abstract double cost(Link l, double vot, int time, DriverType driver);
    public abstract double cost(Link l, double vot, int time);

    public double ffCost(Link aThis) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
