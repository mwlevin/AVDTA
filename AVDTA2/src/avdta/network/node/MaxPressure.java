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

    public static boolean cycleLengthPrinted = false;
    public static final double LOST_TIME = 2.0;
    
    //this is the cycle length - i.e. all phases have to be activated at least once within this cycleLength
    public static int cycleLength = 6;
    //flag for whether or not we are enforcing phases having to be activated at least once within this cycleLength
    public static boolean useCycleMP = false;
    
    //This is the current phase within a cycle.
    private int cycleCounter = 0;
    //current phase this intersection control is on
    private int currentPhase = 1;
    
    private List<Phase> phases;
    private List<MPTurn> turns;
    private Phase lastPhase = null;
    
    public static MPWeight weight_function;
    
    public MaxPressure(Intersection node) 
    {
        super(node);
        useCycleMP = false;
    }
    
    public MaxPressure(Intersection node, int cycleLength)
    {
        super(node);
        useCycleMP = true;
        cycleLength = cycleLength;
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
        
        Set<Turn> matched = new HashSet<Turn>();
        
        Map<Link, Map<Link, TurningMovement>> conflicts = ConflictFactory.generate(node);

        
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
                    
                    for(Turn t : allowed)
                    {
                        matched.add(t);
                    }
                    Phase p = new Phase(0, allowed, Simulator.dt-LOST_TIME, 0, LOST_TIME);
                    phases.add(p);
                }
            }
        }
        
        
        for(Turn t : turns)
        {
            if(!matched.contains(t))
            {
                Phase p = new Phase(0, new Turn[]{t}, Simulator.dt-LOST_TIME, 0, LOST_TIME);
                phases.add(p);
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
        
        if(t1.j == t2.j && getNode().getOutgoing().size() == 2)
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
    
    double max_pressure;
    
    public Phase choosePhase()
    {

        Phase best = null;
        max_pressure = Integer.MIN_VALUE;
        

        
        for(Phase p : phases)
        {
            double pressure = 0;

            
            for(Turn t : p.getTurns())
            {
                pressure += ((MPTurn)t).getWeight(weight_function) * t.getCapacityPerTimestep();
            }

            
            if(pressure > max_pressure)
            {
                max_pressure = pressure;
                best = p;
            }
        }
        
        
        
        return best;
    }
    
    //if we have the same number of phases left in the cycle as timesteps left in the cycle, move on
    //else if we have a phase that is not our current phase with the highest weight, move on
    //else stay
    //
    // set current phase equal to the index + 1 of the best phase in the phase list
    // if we have gone back to phase 1 from the last phase, set cycleCounter = 0, else increment cycleCounter
    public Phase choosePhaseCycleMP()
    {
//        if (getNode().getId() == 13053) {
//            System.out.println(phases.size());
//            System.out.println(getNode().getIncoming().size());
//            System.out.println(getNode().getOutgoing().size());
//        }
        Phase best = null;
        max_pressure = Integer.MIN_VALUE;
        
        for(Phase p : phases)
        {
            double pressure = 0;

            
            for(Turn t : p.getTurns())
            {
                pressure += ((MPTurn)t).getWeight(weight_function) * t.getCapacityPerTimestep();
            }

            
            if(pressure > max_pressure)
            {
                max_pressure = pressure;
                best = p;
            }
        }
        
        //if the number of cycle timesteps remaining is less than or equal to the number of phases remaining in the cycle, move on
        if (cycleLength - cycleCounter <= phases.size() - currentPhase) {
            best = getNextPhase();
        } else if (phases.indexOf(best) + 1 != currentPhase){
            //else if the phase with the highest weight is not the current phase, move on
            best = getNextPhase();
        } else {
            //else (i.e. if the phase with the highest weight is the current phase and we still have timesteps remaining to spare), stay on current phase
            best = phases.get(currentPhase - 1);
        }
        
        //if we were currently not on the first phase and our next best phase is the first phase, reset cycleCounter to 0
        //else increment cycleCounter
        if (currentPhase != 1 && phases.indexOf(best) == 0) {
            cycleCounter = 0;
        } else {
            cycleCounter ++;
        }
        //set the currentphase equal to the index of our next best phase + 1.
        currentPhase = phases.indexOf(best) + 1;
        return best;
    }
    
    //currentPhase is the number of the phase starting from 1
    private boolean isLastPhase() {
        if (currentPhase == phases.size()) {
            return true;
        } else {
            return false;
        }
    }
    
    private Phase getFirstPhase() {
        return phases.get(0);
    }
    
    private Phase getNextPhase () {
        Phase nextPhase = null;
        if (isLastPhase()) {
            nextPhase = getFirstPhase();
        } else {
            //System.out.println("currentphase: " + currentPhase);
            //System.out.println("# of phases: " + phases.size());
            //System.out.println("node id: " + getNode().getId());
            nextPhase = phases.get((currentPhase - 1) + 1);
        }
        return nextPhase;
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
        debugging();

        Phase phase = null;
        if (phases.size() == 0) {
            if (getNode().getId() != 5655 && getNode().getId() != 13089) {
                System.out.println("Node without phases: " + getNode().getId());
            }
        }
        if (useCycleMP && phases.size() > 0) {
            phase = choosePhaseCycleMP();
        } else {
            phase = choosePhase();
        }
        
        if (phase != null) {
            for (Turn t : phase.getAllowed()) {
                t.updateAvgRedLightTime(Simulator.time);
            }
        }
        
        //System.out.println("Node: "+ getNode().getId()+ "move movements: "+ phase);
        
        List<Vehicle> moved = new ArrayList<>();

        Node node = getNode();
        
        int waiting = 0;
        
        // move vehicles to centroid connectors
        for(Link i : node.getIncoming())
        {
            Iterable<Vehicle> sending;
            
            if(i instanceof CentroidConnector)
            {
                sending = i.getVehicles();
            }
            else
            {
                // t.i is MPLink
                sending = ((MPLink)i).getLastCell().getOccupants();
            }
            
            for(Vehicle v : sending)
            {
                if(v.getNextLink() == null || v.getNextLink().isCentroidConnector())
                {
                    moved.add(v);
                }
                waiting++;
            }
        }

        int exiting = 0;

        

        if(phase != null)
        {
        
            // move vehicles according to selected phase
            for(Turn t : phase.getTurns())
            {
                double usable = (Simulator.dt - phase.getRedTime()) / Simulator.dt;
                if (lastPhase != null && lastPhase == phase) {
                    usable = 1;
                }

                int max_y = (int)Math.round(usable * Math.min(t.i.getCapacityPerTimestep(), t.j.getCapacityPerTimestep()));

                Iterable<Vehicle> sending;
                
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


                    if(max_y > 0 && v.getNextLink() == t.j)
                    {
                        moved.add(v);
                        max_y--;
                        
                        t.updateAvgWaitingTime(v.enter_time, Simulator.time);
                        for (Integer i : Simulator.delayTable.keySet()) {
                            if (Simulator.time - v.enter_time >= i) {
                                Simulator.delayTable.put(i, Simulator.delayTable.get(i) + 1);
                            }
                        }
                    }


                }


            }
        }
        
            
        for(Vehicle v : moved)
        {
            Link j = v.getNextLink();
            Link i = v.getCurrLink();

            //if (i != null) {
                i.removeVehicle(v);
            //}

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
        
        lastPhase = phase;
        
        return exiting;
    }

    private void debugging() {
        if (Simulator.time >= 10785 && getNode().getId() == 5577) {
            System.out.println("time: " + Simulator.time);
            Intersection intersection = ((Intersection) getNode());
            for (Link incomingLink : intersection.getIncoming()) {
                System.out.println("id: " + incomingLink.getId() + " queue: " + incomingLink.getQueueLength());
            }
        }

        if (getNode().getId() == 5577) {
            Intersection intersection = ((Intersection) getNode());
            for (Link incomingLink : intersection.getIncoming()) {
                if (incomingLink.getId() == 18602) {
                    System.out.println(incomingLink.getQueueLength());
                }
            }
        }
    }
}
