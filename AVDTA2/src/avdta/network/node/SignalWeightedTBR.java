/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author ml26893
 */
public class SignalWeightedTBR extends PriorityTBR implements Signalized, DelayWeights
{
    private Map<Link, Map<Link, Double>> weights;
    private double total_weight;
    
    public SignalWeightedTBR()
    {
        weights = new HashMap<Link, Map<Link, Double>>();
        total_weight = 0.0;
        
        setPolicy(new WeightedFCFSPolicy(this));
    }
    
    public SignalWeightedTBR(Intersection n)
    {
        super(n);
        weights = new HashMap<Link, Map<Link, Double>>();
        total_weight = 0.0;
        
        setPolicy(new WeightedFCFSPolicy(this));
    }
    
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
    
    public void addPhase(Phase p)
    {
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
}
