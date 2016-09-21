/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.DriverType;
import avdta.network.link.Link;

/**
 * An intersection in the traffic network. Each intersection has an {@link IntersectionControl} that specifies vehicle movement
 * @author Michael
 */
public class Intersection extends Node
{
    private IntersectionControl control;

    /**
     * Instantiates intersection with location (0, 0) and specified control. Null control is acceptable for instantiation but not for simulation
     * @param id
     * @param control 
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
     * Instantiates intersection with specified control. Null control is acceptable for instantiation but not for simulation
     * @param id
     * @param control 
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
    
    /**
     * 
     * @return the Signalized from the IntersectionControl
     */
    public Signalized getSignal()
    {
        return control.getSignal();
    }
    
    /**
     * 
     * @return the IntersectionControl type 
     */
    public int getType()
    {
        return control.getType();
    }
    
    /**
     * 
     * @param i the incoming link
     * @param j the outgoing link
     * @param driver the driver type (specifies conventional/autonomous vehicle and transit)
     * @return whether a vehicle with the given driver can turn from i to j
     */
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return control.canMove(i, j, driver);
    }
    
    /**
     * 
     * @return whether this intersection control uses conflict regions to determine turning movement constraints 
     */
    public boolean hasConflictRegions()
    {
        return control.hasConflictRegions();
    }
    
    /**
     * Resets the IntersectionControl to restart the simulation
     */
    public void reset()
    {
        control.reset();
    }
    
    /**
     * Initializes the IntersectionControl at the start of simulation
     */
    public void initialize()
    {
        control.initialize();
    }
    
    /**
     * Executes one time step of the IntersectionControl
     * @return the number of exiting vehicles
     */
    public int step()
    {
        return control.step();
    }
    
    /**
     * 
     * @return the IntersectionControl controlling this Intersection 
     */
    public IntersectionControl getControl()
    {
        return control;
    }
    
    /**
     * Updates the control on this Intersection
     * @param c 
     */
    public void setControl(IntersectionControl c)
    {
        control = c;
        
        control.setNode(this);
    }
}
