/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.ReadNetwork;
import avdta.network.type.Type;
import java.util.List;
import java.util.ArrayList;
import avdta.network.Simulator;
import avdta.vehicle.Vehicle;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.link.MPLink;
import avdta.vehicle.DriverType;



/**
 * Intersection control for max-pressure.
 * This code ignores FIFO at links.
 * It is designed to work with MPLink.
 * @author mlevin
 */
public class MaxPressure extends IntersectionControl 
{
    private Node node;
    private List<Phase> phases;
    private List<MPTurn> turns;
    
    public MaxPressure(Node node) 
    {
        this.node = node;
    }
    
    public void initialize()
    {
        // need to create phases here
        phases =  new ArrayList<>();
        
        
        // need to set up turning proportions here too
    }
    
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return i.getSource() != j.getDest();
    }
    
    public void prepare()
    {
        for(MPTurn t : turns)
        {
            t.calculateQueue();
        }
    }
    
    public void reset()
    {
        
    }
    
    public Type getType()
    {
        return ReadNetwork.MAX_PRESSURE;
    }
    
    public Phase choosePhase()
    {
        Phase best = null;
        double max_pressure = Integer.MIN_VALUE;
        
        for(Phase p : phases)
        {
            double pressure = 0;
            
            for(Turn t : p.getTurns())
            {
                pressure += ((MPTurn)t).getWeight() * t.getCapacity();
            }
            
            if(pressure > max_pressure)
            {
                max_pressure = pressure;
                best = p;
            }
        }
        
        
        return best;
    }
    
    
    public List<MPTurn> getTurns()
    {
        return turns;
    }
    
    public boolean hasConflictRegions()
    {
        return false;
    }
    

    
    public int step()
    {
        Phase phase = choosePhase();
        
        
        

        int exiting = 0;

        
        // move vehicles according to selected phase
        for(Turn t : phase.getTurns())
        {
            double usable = (Simulator.dt - phase.getRedTime()) / Simulator.dt;
            
            int max_y = (int)Math.round(usable * Math.min(t.i.getCapacityPerTimestep(), t.j.getCapacityPerTimestep()));
            
            Iterable<Vehicle> sending;
            
            
            List<Vehicle> moved = new ArrayList<>();
            
            if(t.i instanceof CentroidConnector)
            {
                sending = t.i.getVehicles();
            }
            else
            {
                // t.i is MPLink
                sending = ((MPLink)t.i).getLastCell().getOccupants();
            }
            
            for(Vehicle v : sending)
            {
                if(v.getNextLink() == null)
                {
                    moved.add(v);
                }
                
                else if(max_y > 0 && v.getNextLink() == t.j)
                {
                    moved.add(v);
                    max_y--;
                }
                
                
            }
            
            for(Vehicle v : moved)
            {
                Link j = v.getNextLink();
                Link i = v.getCurrLink();
                
                i.removeVehicle(v);
                
                if(j == null)
                {
                    exiting++;
                    v.exited();
                }
                else
                {
                    j.addVehicle(v);
                }
            }
        }
        
        return exiting;
    }
}
