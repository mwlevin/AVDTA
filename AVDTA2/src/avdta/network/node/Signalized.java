/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import java.util.List;

/**
 * This interface specifies the methods and data needed for any intersection control that uses or mimics a traffic signal.
 * 
 * @author Michael
 */
public interface Signalized 
{
    /**
     * Adds a {@link Phase} to this signal. {@link Phase}s do not need to be added in sequence order.
     * @param p 
     */
    public void addPhase(Phase p);
    
    /**
     * Updates the offset for this signal cycle
     * @param o the new offset
     */
    public void setOffset(double o);
    
    /**
     * Returns the offset for this signal cycle
     * @return the offset for this signal cycle
     */
    public double getOffset();
    
    /**
     * Returns the {@link List} of {@link Phase}s used in this signal
     * @return the {@link List} of {@link Phase}s used in this signal
     */
    public List<Phase> getPhases();
}
