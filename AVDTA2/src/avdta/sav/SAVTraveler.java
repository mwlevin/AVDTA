/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.Simulator;
import avdta.sav.Taxi;
import avdta.sav.SAVDest;
import avdta.sav.SAVOrigin;
import avdta.traveler.Traveler;


/**
 *
 * @author Michael
 */
public class SAVTraveler extends Traveler
{
    private int etd;
    
    
    private Taxi assigned;
    
    public static final int ALLOWED_DELAY = 300;
    
    public boolean unable;
   
    /**
     * Constructs the traveler with the specified parameters.
     * @param id the id
     * @param origin the origin
     * @param dest the destination
     * @param dtime the departure time
     */
    public SAVTraveler(int id, SAVOrigin origin, SAVDest dest, int dtime, double vot)
    {
        super(id, origin, dest, dtime, vot);
        
        etd = Integer.MAX_VALUE;
    }
    
    /**
     * Inform the traveler that a taxi has been assigned to him.
     * @param t the taxi
     */
    public void setAssignedTaxi(Taxi t)
    {
        assigned = t;
    }
    
    /**
     * Returns the taxi assigned to this traveler.
     * @return the assigned taxi, or null if none exists
     */
    public Taxi getAssignedTaxi()
    {
        return assigned;
    }
    
    public int getEtd()
    {
        return etd;
    }
    
    public void setEtd(int e)
    {
        etd = e;
    }
    
    
    
    /**
     * Resets the traveler to restart the simulation.
     */
    public void reset()
    {
        super.reset();
        etd = Integer.MAX_VALUE;
        unable = false;
    }
    

    
    /**
     * Returns the origin.
     * @return the origin
     */
    public SAVOrigin getOrigin()
    {
        return (SAVOrigin)super.getOrigin();
    }
    
    /**
     * Returns the destination.
     * @return the destination
     */
    public SAVDest getDest()
    {
        return (SAVDest)super.getDest();
    }
    
    
    
    
    
    /**
     * This method is called when the traveler enters a taxi.
     * The current time is saved as the taxi enter time.
     */
    public void enteredTaxi()
    {
        setEnterTime(Simulator.time);
    }
    
    
    
    
}
