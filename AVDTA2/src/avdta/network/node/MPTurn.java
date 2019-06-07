/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import avdta.network.link.CentroidConnector;
import avdta.network.link.MPLink;

/**
 * Designed to be used with MPLinks
 * @author mlevin
 */
public class MPTurn extends Turn
{
    private int queue;
    private double p_ij;
    
    
    public double num;
    public double denom;
    
    public MPTurn(Link i, Link j)
    {
        super(i, j);
    }
    
    public void setTurningProportion(double p)
    {
        p_ij = p;
    }
    
    public double getWeight(MPWeight func)
    {
        double weight = queue;
        
        Node n = j.getDest();
        
        if(n instanceof Intersection)
        {
            MaxPressure control = (MaxPressure)((Intersection)n).getControl();
            
            for(MPTurn t : control.getTurns())
            {
                if(t.i == j)
                {
                    weight -= t.getTurningProportion() * t.getQueue();
                }
            }
        }
        
        return weight;
    }
    

    
    public void calculateQueue()
    {
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
        
        this.queue = queue;
    }
    
    public int getQueue()
    {
        return queue;
    }
    
    public double getTurningProportion()
    {
        return p_ij;
    }
}
