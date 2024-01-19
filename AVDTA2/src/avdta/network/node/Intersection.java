/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.Simulator;
import avdta.network.link.CTMLink;
import avdta.network.link.CentroidConnector;
import avdta.vehicle.DriverType;
import avdta.network.link.Link;
import avdta.network.type.Type;
import avdta.util.RunningAvg;

/**
 * An intersection in the traffic network. Each intersection has an {@link IntersectionControl} that specifies vehicle movement
 * @author Michael
 */
public class Intersection extends Node
{
    private IntersectionControl control;
    private RunningAvg avgQLength = new RunningAvg();
    private RunningAvg avgDelay = new RunningAvg();

    private int totalQueueLength = 0;

    /**
     * Instantiates {@link Intersection} with {@link Location} (0, 0) and specified control. Null control is acceptable for instantiation but not for simulation
     * @param id the id of the {@link Node}
     * @param control the intersection control
     */
    public Intersection(int id, IntersectionControl control)
    {
        super(id);
        if(control != null)
        {
            this.control = control;
            control.setNode(this);
        }
    }
    
    /**
     * Instantiates {@link Intersection} with specified control. Null control is acceptable for instantiation but not for simulation
     * @param id the id of the {@link Node}
     * @param loc the {@link Location} of this {@link Node}
     * @param control the intersection control
     */
    public Intersection(int id, Location loc, IntersectionControl control)
    {
        super(id, loc);
        if(control != null)
        {
            this.control = control;
            control.setNode(this);
        }
    }
    
    public void prepare()
    {
        control.prepare();
    }
    
    /**
     * Returns the {@link Signalized} form of the {@link IntersectionControl}, if it exists
     * 
     * @return the {@link Signalized} form of the {@link IntersectionControl}, if it exists
     */
    public Signalized getSignal()
    {
        if(control instanceof Signalized)
        {
            return ((Signalized)control);
        }
        else
        {
            return null;
        }
    }
    
    /**
     * Returns a type code specifying the type of {@link IntersectionControl}
     * 
     * @return the {@link IntersectionControl} type 
     */
    public Type getType()
    {
        return control.getType();
    }
    
    /**
     * Checks whether a vehicle with the given {@link DriverType} can make the specified turning movement
     * 
     * @param i the incoming {@link Link}
     * @param j the outgoing {@link Link}
     * @param driver the {@link DriverType} (specifies conventional/autonomous vehicle and transit)
     * @return whether a vehicle with the given {@link DriverType} can turn from {@link Link} i to {@link Link} j
     */
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return control.canMove(i, j, driver);
    }
    
    /**
     * Checks whether this intersection uses {@link ConflictRegion}s
     * 
     * @return whether this intersection control uses {@link ConflictRegion}s to determine turning movement constraints 
     */
    public boolean hasConflictRegions()
    {
        return control.hasConflictRegions();
    }
    
    /**
     * Resets the {@link IntersectionControl} to restart the simulation
     */
    public void reset()
    {
        avgQLength.reset();
        avgDelay.reset();
        control.reset();
    }
    
    /**
     * Initializes the {@link IntersectionControl} at the start of simulation
     */
    public void initialize()
    {
        control.initialize();
    }
    
    /**
     * Executes one time step of the {@link IntersectionControl}
     * @return the number of exiting vehicles
     */
    public int step()
    {
        updateTotalQueueLength();
        return control.step();
    }

    private void updateTotalQueueLength() {
        totalQueueLength = 0;
        for (Link link : getIncoming()) {
            totalQueueLength += link.getQueueLength();
        }
    }
    
    /**
     * Returns the {@link IntersectionControl} controlling this {@link Intersection}
     * @return the {@link IntersectionControl} controlling this {@link Intersection}
     */
    public IntersectionControl getControl()
    {
        return control;
    }
    
    /**
     * Updates the control on this {@link Intersection}
     * @param c the new {@link IntersectionControl}
     */
    public void setControl(IntersectionControl c)
    {
        control = c;
        
        control.setNode(this);
    }
    
    public double getAvgQLength() {
        return avgQLength.getAverage();
    }
    
    public void updateAvgQLength() {
        int count = 0;
        for (Link l : getIncoming()) {
            if (l instanceof CTMLink) {
                //count = count + l.getQueueLength();
                count = count + ((CTMLink) l).getLastCellOccupancy();
            }
            
            if (l instanceof CentroidConnector) {
                count = count + ((CentroidConnector) l).getOccupancy();
            }
        }
        avgQLength.add(count);
    }
    
    public double getAvgDelay() {
        return avgDelay.getAverage();
    }
    
    public void updateAvgDelay() {
        double value = 0;
        if (control instanceof MaxPressure) {
            for (MPTurn turn : ((MaxPressure) control).getTurns()) {
//                if (turn.i instanceof CentroidConnector) {
//                    continue;
//                }
                value = value + turn.getAverageWaitingTime();
            }
        }
        avgDelay.add(value);
    }

    public int getTotalQueueLength() {
        return totalQueueLength;
    }
}
