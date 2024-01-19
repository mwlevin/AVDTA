/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.link.MPLink;
import avdta.util.RunningAvg;
import avdta.vehicle.Vehicle;
import java.util.Map;

/**
 * This class just defines a data type to store a turn i.e. a pair of {@link Link}
 * data-types ({@code i} and {@code j}).
 * @author Michael
 */
public class Turn implements java.io.Serializable, Comparable<Turn>
{
    public Link i, j;
    /**
     * Instantiates the turn with incoming link {@code i} and outgoing link 
     * {@code j}.
     * @param i Incoming link for that turn.
     * @param j Outgoing link for that turn.
     */
    private int lastActivatedTime = 0;
    public RunningAvg avgRedLightTime = new RunningAvg();
    public RunningAvg avgWaitingTime = new RunningAvg();
    
    public Turn(Link i, Link j)
    {
        this.i = i;
        this.j = j;
    }
    
    public double getCapacity()
    {
        return Math.min(i.getCapacity(), j.getCapacity());
    }
    
    public double getCapacityPerTimestep()
    {
        return Math.min(i.getCapacityPerTimestep(), j.getCapacityPerTimestep());
    }
    
    public int compareTo(Turn rhs)
    {
        if(rhs.i != i)
        {
            return i.getId() - rhs.i.getId();
        }
        else
        {
            return j.getId() - rhs.j.getId();
        }
    }
    
    /**
     * Returns a {@link String} representation of the turn
     * @return a {@link String} representation of the turn
     */
    public String toString()
    {
        return ""+i.getId()+"-"+j.getId();
    }
    
    public int hashCode()
    {
        return i.getId()+j.getId();
    }
    
    /**
     * Checks whether the incoming and outgoing {@link Link}s are the same as another {@link Turn}
     * @param rhs the {@link Turn} to compare to
     * @return whether the incoming and outgoing {@link Link}s match
     */
    public boolean equals(Turn rhs)
    {
        return i == rhs.i && j == rhs.j;
    }
    
    public void updateAvgRedLightTime(int currentTime) {
        avgRedLightTime.add(currentTime - lastActivatedTime);
        lastActivatedTime = currentTime;
    }
    
    public void updateAvgWaitingTime(int enter, int exit) {
        avgWaitingTime.add(exit - enter);
    }
    
    private int getCalculatedQueue() {
        int queue = 0;
        
        Iterable<Vehicle> sending;
        
        if(i instanceof CentroidConnector)
        {
            sending = i.getVehicles();
        }
        else
        {
            sending = ((MPLink)i).getLastCell().getOccupants();
        }
        
        for(Vehicle v : sending)
        {
            if(v.getNextLink() == j)
            {
                queue++;
            }
        }
        return queue;
    }
    
    private double getMaxPressureWeight() {
        double weight = getCalculatedQueue();
        Node n = j.getDest();
        
        if (n instanceof Intersection) {
            HaiVuTrafficSignal control = (HaiVuTrafficSignal)((Intersection) n).getControl();
            
            for (MPTurn mp_turn : control.getMPTurns()) {
                if (mp_turn.i == j) {
                    weight -= mp_turn.getTurningProportion() * mp_turn.getQueue();
                }
            }
        }
        
        return weight;
    }
    
    public double getMaxPressure() {
        // return getMaxPressureWeight() * getCapacityPerTimestep();
        return getMaxPressureWeight();
    }
}
