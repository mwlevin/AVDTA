/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.ReadNetwork;
import avdta.network.node.policy.WeightedFCFSPolicy;
import avdta.network.link.Link;
import avdta.network.node.Intersection;
import avdta.network.node.Phase;
import avdta.network.node.PriorityTBR;
import avdta.network.node.Signalized;
import avdta.network.node.Turn;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;

/**
 * This class implements a signal-weighted reservations. 
 * {@link Vehicle} waiting time is weighted by how much capacity the traffic signal allocates to their turning movement.
 * The {@link SignalWeightedTBR} is a {@link Signalized}; it does not implement a traffic signal, but uses the signal timing data.
 * @author Michael
 */
public class SignalWeightedTBR extends PriorityTBR implements Signalized, DelayWeights
{
    private Map<Link, Map<Link, Double>> weights;
    private double total_weight;
    private double offset;
    
    private List<Phase> phases;
    
    /**
     * Constructs this {@link SignalWeightedTBR} with a null {@link Node}. A non-null {@link Node} is required for simulation.
     */
    public SignalWeightedTBR()
    {
        weights = new HashMap<Link, Map<Link, Double>>();
        phases = new ArrayList<Phase>();
        total_weight = 0.0;
        
        setPolicy(new WeightedFCFSPolicy(this));
    }
    
    /**
     * Constructs this {@link SignalWeightedTBR} with the specified {@link Intersection}. 
     * @param n the {@link Intersection} controlled by this {@link SignalWeightedTBR}
     */
    public SignalWeightedTBR(Intersection n)
    {
        super(n);
        weights = new HashMap<Link, Map<Link, Double>>();
        phases = new ArrayList<Phase>();
        total_weight = 0.0;
        
        setPolicy(new WeightedFCFSPolicy(this));
    }
    
    /**
     * the {@link List} of signal {@link Phase}s
     * @return the {@link List} of signal {@link Phase}s
     */
    public List<Phase> getPhases()
    {
        return phases;
    }
    
    /**
     * Updates the signal cycle offset. The offset does not affect this policy.
     * @param o the new signal cycle offset
     */
    public void setOffset(double o)
    {
        this.offset = o;
    }
    
    /**
     * Returns the signal cycle offset. The offset does not affect this policy.
     * @return the signal cycle offset
     */
    public double getOffset()
    {
        return offset;
    }
    
    
    /**
     * Returns the weight associated with the specified turning movement.
     * @param i the incoming {@link Link}
     * @param j the outgoing {@link Link}
     * @return the weight associated with the specified turning movement
     */
    public double getWeight(Link i, Link j)
    {
        if(total_weight == 0)
        {
            return 1;
        }
        
        try
        {
            return weights.get(i).get(j) / total_weight;
        }
        catch(NullPointerException ex)
        {
            return 0.01;
        }
    }
    
    /**
     * Adds a signal {@link Phase} and updates the weights accordingly. {@link Phase} order does not matter.
     * @param p the {@link Phase} to be added
     */
    public void addPhase(Phase p)
    {
        phases.add(p);
        
        for(Turn t : p.getTurns())
        {
            Link i = t.i;
            Link j = t.j;
        
        
            Map<Link, Double> temp = new HashMap<Link, Double>();

            if(!weights.containsKey(i))
            {
                weights.put(i, temp = new HashMap<Link, Double>());
            }

            if(temp.containsKey(j))
            {
                temp.put(j, temp.get(j)+p.getGreenTime());
            }
            else
            {
                temp.put(j, p.getGreenTime());
            }
        }
        
        total_weight += p.getGreenTime();
    }
    
    /**
     * Returns the type code associated with this policy
     * @return {@link ReadNetwork#RESERVATION}+{@link ReadNetwork#WEIGHTED}
     */
    public int getType()
    {
        return ReadNetwork.RESERVATION + ReadNetwork.WEIGHTED;
    }
}
