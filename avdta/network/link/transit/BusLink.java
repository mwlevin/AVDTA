/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.transit;

import avdta.network.link.transit.TransitLink;
import avdta.network.node.Node;
import java.util.Set;
import java.util.TreeSet;

/**
 * This represents the virtual connection between two bus stops. Travel times are based on experienced travel times via buses traveling between the two stops.
 * @author Michael
 */
public class BusLink extends TransitLink
{
    private Set<TTRecord> tts;
    
    /**
     * Constructs the {@link BusLink} with the given upstream and downstream nodes
     * @param source the upstream node
     * @param dest the downstream node
     */
    public BusLink(Node source, Node dest)
    {
        super(source, dest);
        
        tts = new TreeSet<TTRecord>();
    }
    
    /**
     * Resets this {@link BusLink} to restart the simulation
     */
    public void reset()
    {
        tts.clear();
    }
    
    /**
     * Records the travel time between the bus stops by a bus
     * @param enter the departure time
     * @param exit the arrival time
     */
    public void setTT(int enter, int exit)
    {

        tts.add(new TTRecord(enter, exit-enter));
    }
    
    /**
     * Returns the travel time for a given departure time. The travel time is based on the experienced travel time for the first bus departing after the departure time.
     * @param dtime the departure time
     * @return the travel time (s)
     */
    public double getTT(int dtime)
    {
        for(TTRecord t : tts)
        {
            if(t.getDepTime() >= dtime)
            {
                double output = t.getTT() + t.getDepTime() - dtime;
                
 
                return output;
            }
        }
        
        return Integer.MAX_VALUE;
    }
    
    
}
