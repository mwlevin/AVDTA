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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



/**
 * Intersection control for max-pressure.
 * This code ignores FIFO at links.
 * It is designed to work with MPLink.
 * @author mlevin
 */
public class MaxPressure extends IntersectionControl 
{
    private List<Phase> phases;
    private List<MPTurn> turns;
    
    public MaxPressure(Intersection node) 
    {
        super(node);
    }
    
    public void initialize()
    {
        // need to create phases here
        
        turns = new ArrayList<>();
        Intersection node = getNode();
        
        for(Link i : node.getIncoming())
        {
            for(Link j : node.getOutgoing())
            {
                if(canMove(i, j, DriverType.HV))
                {
                    turns.add(new MPTurn(i, j));
                }
            }
        }
        
        Map<Link, Map<Link, TurningMovement>> conflicts = ConflictFactory.generate(node);
        
        for(Link i : conflicts.keySet())
        {
            for(Link j : conflicts.get(i).keySet())
            {
                if(conflicts.get(i).get(j).size() > 0)
                System.out.println(i.getSource()+"\t"+j.getDest()+" - "+conflicts.get(i).get(j).size());
            }
            System.out.println();
        }
        
        phases =  new ArrayList<>();
        
        // look for compatible combinations of 2 turns
        // then add turns as feasible
        
        for(int i = 0; i < turns.size()-1; i++)
        {
            for(int j = i+1; j < turns.size(); j++)
            {
                if(!hasConflicts(turns.get(i), turns.get(j), conflicts))
                {
                    List<Turn> allowed = new ArrayList<>();
                    allowed.add(turns.get(i));
                    allowed.add(turns.get(j));
                    
                    outer:for(Turn t : turns)
                    {
                        if(allowed.contains(t))
                        {
                            continue;
                        }
                        
                        for(Turn t2 : allowed)
                        {
                            if(hasConflicts(t, t2, conflicts))
                            {
                                continue outer;
                            }
                        }
                        
                        allowed.add(t);
                    }
                    
                    Phase p = new Phase(0, allowed, Simulator.dt-2, 0, 2);
                    phases.add(p);
                }
            }
        }
        
        // remove duplicate phases
        List<Phase> duplicates = new ArrayList<>();
        for(int i = 0; i < phases.size()-1; i++)
        {
            for(int j = i+1; j < phases.size(); j++)
            {
                if(phases.get(i).equals(phases.get(j)))
                {
                    duplicates.add(phases.get(j));
                }
            }
        }
        
        for(Phase p : duplicates)
        {
            phases.remove(p);
        }

    }
    
    public List<Phase> getPhases()
    {
        return phases;
    }
    
    
    public boolean hasConflicts (Turn t1, Turn t2, Map<Link, Map<Link, TurningMovement>> conflicts)
    {
        if(t1.i == t2.i)
        {
            return false;
        }
        
        
        
        Set<ConflictRegion> c1 = conflicts.get(t1.i).get(t1.j);
        Set<ConflictRegion> c2 = conflicts.get(t2.i).get(t2.j);
        
        
        Set<ConflictRegion> intersection = new HashSet<>();
        
        for(ConflictRegion c : c1)
        {
            if(c2.contains(c))
            {
                intersection.add(c);
            }
        }
        
        if(intersection.size() == 0)
        {
            return false;
        }
        
        // 2 left turns that don't share links
        if(c1.size() == 3 && c2.size() == 3 &&
                t1.i != t2.i && t1.j != t2.j &&
                t1.i.getSource() != t2.j.getDest() &&
                t1.j.getDest() != t1.i.getSource()
                )
        {
            return false;
        }
        

        return true;
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
