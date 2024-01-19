/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

/**
 * This interface should be implemented by any {@link Link} subclass with a {@link TransitLane}
 * @author Michael
 */
public interface AbstractSplitLink 
{
    /**
     * Returns the transit lane
     * @return the transit lane
     */
    public TransitLane getTransitLane();
}
