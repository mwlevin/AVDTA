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
    
    public Signalized getSignal()
    {
        return control.getSignal();
    }
    
    
    public int getType()
    {
        return control.getType();
    }
    
    
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return control.canMove(i, j, driver);
    }
    
    public boolean hasConflictRegions()
    {
        return control.hasConflictRegions();
    }
    
    public void reset()
    {
        control.reset();
    }
    
    public void initialize()
    {
        control.initialize();
    }
    
    public int step()
    {
        return control.step();
    }
    
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
