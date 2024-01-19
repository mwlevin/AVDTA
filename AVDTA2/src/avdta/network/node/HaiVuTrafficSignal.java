/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.DriverType;
import avdta.network.link.Link;
import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.LTMLink;
import avdta.network.link.TransitLane;
import avdta.network.type.Type;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Math;
import java.util.HashSet;
import java.util.Set;

/**
 * This class implements traffic signals at intersections ({@link Node}) of a 
 * network. <br>
 * A signal has {@link Phase}s, each of which has a {@link PhaseMovement} for a 
 * specified period of time in a cycle. <br>
 * {@code List<Phase> phases}: is a list of all the phases in the signals, which 
 * defines the turning movements allowed in each phase. <br>
 * {@code curr_idx} tells the current phase of the signal. <br>
 * {@code curr_time} is the current time in the phase. <br>
 * {@code total_time} is the cycle time. <br>
 * {@code turns} maps an incoming link to a mapping which maps an outgoing link 
 * to a {@link PhaseMovement}.
 * @author Michael
 */
public class HaiVuTrafficSignal extends IntersectionControl implements Signalized
{
    private List<Phase> phases;
    private int curr_idx;
    private double curr_time;
    private double total_time;
    public static final double LOST_TIME = 2.0;
    public static final double CYCLE_LENGTH = 225.0;
    private static final boolean CREATE_OWN_PHASES = true;
    private static final boolean PHASES_GREATER_THAN_ONE_CONDITIONAL = true;
    
    private double offset;
    
    private Map<Link, Map<Link, PhaseMovement>> turns;
    private List<MPTurn> MPTurns = new ArrayList<>();
    /**
     * Instantiates a traffic signal with null.
     */
    public HaiVuTrafficSignal()
    {
        this(null);
    }
    /**
     * Instantiates a traffic signal at a given {@link Intersection}.
     * @param n An intersection at which traffic signal needs to be implemented.
     */
    public HaiVuTrafficSignal(Intersection n)
    {
        super(n);
        
        phases = new ArrayList<Phase>();
        turns = new HashMap<Link, Map<Link, PhaseMovement>>();
    }
    
    public MPTurn getMPTurnforTurn(Turn t) {
        for (MPTurn mp_turn : MPTurns) {
            if (mp_turn.i == t.i && mp_turn.j == t.j) {
                return mp_turn;
            }
        }
        
        return null;
    }
    
    public Map<Link, Map<Link, PhaseMovement>> getTurns() {
        return turns;
    }
    
    public Signalized getSignal()
    {
        return this;
    }
    
    public double getOffset()
    {
        return offset;
    }
    
    /**
     * Gives the number of phases in the given signal.
     * @return Returns the number of phases in the signal.
     */
    public int getNumPhases()
    {
        return phases.size();
    }
    
    public Type getType()
    {
        return ReadNetwork.SIGNAL;
    }
    
    /**
     * Returns if the signal has conflicting regions, which it has (always), and 
     * so it returns true.
     * @return Returns {@code true}.
     */
    public boolean hasConflictRegions()
    {
        return true;
    }
    /**
     * Adds a phase {@code p} and also updates the cycle time with addition of 
     * this phase.
     * Phases do not need to be added in sequence order.
     * @param p A phase which needs to be added to a signal.
     */
    public void addPhase(Phase p)
    {
        phases.add(p);
        
        total_time += p.getDuration();
    }
    
    /**
     * Updates the offset for the signal cycle
     * @param offset the new offset
     */
    public void setOffset(double offset)
    {
        this.offset = offset;
    }
    
    
    public List<Phase> getPhases()
    {
        return phases;
    }
    
    private void setPhases() {
        total_time = 225;
        for (Phase phase : phases) {
            phase.setGreenTime((CYCLE_LENGTH - phases.size() * LOST_TIME)/phases.size());
        }
    }
    
    /**
     * Resets the current phase and the current time to 0, inserts the mapping 
     * of the phase movements in turns, and sets the start time of each phase in 
     * the cycle.
     */
    public void initialize()
    {
        if (CREATE_OWN_PHASES) {
            //createPhases calls createMPTurns
            createPhases();
        } else {
            createMPTurns();
        }

        Collections.sort(phases);
        setPhases();

        Intersection node = getNode();
        
        if(phases.size() > (PHASES_GREATER_THAN_ONE_CONDITIONAL ? 1 : 0))
        {
            curr_time = offset  % getDuration();
            curr_idx = 0;

            double temp = curr_time;
            for(Phase p : phases)
            {
                if(temp >= p.getDuration())
                {
                    temp -= p.getDuration();
                    curr_idx++;
                }
                else
                {
                    break;
                }
            }


            for(Phase p : phases)
            {
                for(Turn t : p.getAllowed())
                {
                    if(!turns.containsKey(t.i))
                    {
                        turns.put(t.i, new HashMap<Link, PhaseMovement>());
                    }

                    if(!turns.get(t.i).containsKey(t.j))
                    {
                        turns.get(t.i).put(t.j, new PhaseMovement());
                    }
                }
            }

            double time = 0;

            for(Phase p : phases)
            {
                p.setStartTime(time);
                time += p.getDuration();
            }
        }
        else
        {
            for(Link i : node.getIncoming())
            {
                turns.put(i, new HashMap<Link, PhaseMovement>());
                for(Link j : node.getOutgoing())
                {
                    turns.get(i).put(j, new PhaseMovement());
                }
            }
        }
    }

    public void createMPTurns() {
        MPTurns = new ArrayList<>();
        Intersection node = getNode();
        for(Link i : node.getIncoming())
        {
            for(Link j : node.getOutgoing())
            {
                if(canMove(i, j, DriverType.HV))
                {
                    MPTurns.add(new MPTurn(i, j));
                }
            }
        }
    }
    
    public void createPhases() {
        // need to create phases here
        Intersection node = getNode();
        createMPTurns();
        
        Set<Turn> matched = new HashSet<Turn>();
        
        Map<Link, Map<Link, TurningMovement>> conflicts = ConflictFactory.generate(node);

        
        phases =  new ArrayList<>();
        
        // look for compatible combinations of 2 turns
        // then add MPTurns as feasible
        
        for(int i = 0; i < MPTurns.size()-1; i++)
        {
            for(int j = i+1; j < MPTurns.size(); j++)
            {
                if(!hasConflicts(MPTurns.get(i), MPTurns.get(j), conflicts))
                {
                    List<Turn> allowed = new ArrayList<>();
                    allowed.add(MPTurns.get(i));
                    allowed.add(MPTurns.get(j));
                    
                    outer:for(Turn t : MPTurns)
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
        
        
        for(Turn t : MPTurns)
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
    
    public double getDuration()
    {
        double output = 0;
        
        for(Phase p : phases)
        {
            output += p.getDuration();
        }
        
        return output;
    }

    /**
     * Resets the current phase and the current time to 0, and sets the flow 
     * {@code q} of the outgoing link of the turn movement to 0.
     */
    public void reset()
    {
        curr_time = offset;
        curr_idx = 0;
        
        double temp = curr_time % getDuration();
        for(Phase p : phases)
        {
            if(temp >= p.getDuration())
            {
                temp -= p.getDuration();
                curr_idx++;
            }
            else
            {
                break;
            }
        }
        
        if(phases.size() > 0)
        {
            for(Link i : turns.keySet())
            {
                for(Link j : turns.get(i).keySet())
                {
                    PhaseMovement mvt = turns.get(i).get(j);
                    mvt.setMaxFlow(0);
                    mvt.blocked = false;
                }
            }


        }
    }
    /**
     * Checks if the given pair of links in a turning movement is involved in 
     * the phases at the signal; if not, movement from {@code i} to {@code j} is 
     * not allowed. Movement is also allowed if one of the links is a centroid 
     * connector.
     * @param i Incoming link.
     * @param j Outgoing link,
     * @param driver AV or HV.
     * @return Returns true if the input links are involved in the phase or if 
     * at least one of the input links is a centroid connector.
     */
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        if(j.isCentroidConnector() || i.isCentroidConnector())
        {
            return true;
        }
        else if(phases.size() > 0)
        {
            return turns.containsKey(i) && turns.get(i).containsKey(j);
        }
        else
        {
            return true;
        }
    }   
    
    private void assignGreenTimes() {
        double denominator = 0;
        
        for (Phase phase : phases) {
            double pressure = 0;
            for (Turn turn : phase.getTurns()) {
                pressure += turn.getMaxPressure();
            }
            
            double nu = 2.5;
            double exp = Math.exp(nu * pressure);
            denominator += exp;
        }

        for (Phase phase : phases) {
            double pressure = 0;
            for (Turn turn : phase.getTurns()) {
                pressure += turn.getMaxPressure();
            }
            
            double nu = 2.5;
            double exp = Math.exp(nu * pressure);

            double green_time = (CYCLE_LENGTH - phases.size() * LOST_TIME)/phases.size();
            
            if (denominator > 0) {
                green_time = exp/denominator * (CYCLE_LENGTH - phases.size() * LOST_TIME);
            }

            phase.setGreenTime(green_time);
        }
    }
    /**
     * Maps the green time of the various phases to the discretized time setting.
     */
    public void calcGreenTime()
    {
        if(phases.size() > (PHASES_GREATER_THAN_ONE_CONDITIONAL ? 1 : 0))
        {
            // data collection
            Phase current_phase = phases.get(curr_idx);
            for (Turn turn : current_phase.getAllowed()) {
                turn.updateAvgRedLightTime(Simulator.time);
            }
            
            
            double time_rem = Network.dt;
        
            for(Link i : turns.keySet())
            {
                for(Link j : turns.get(i).keySet())
                {
                    PhaseMovement movement = turns.get(i).get(j);
                    movement.newTimestep();
                }
            }
            while(time_rem > 0)
            {

                Phase curr_phase = phases.get(curr_idx);

                double green_time = Math.min(time_rem, curr_phase.getGreenTime(curr_time));


                for(Turn turn : curr_phase.getAllowed())
                {     
                    try
                    {
                        turns.get(turn.i).get(turn.j).addMaxFlow((green_time / Network.dt) *
                                Math.min(turn.j.getCapacityPerTimestep() * turn.j.getNumLanes(),
                                turn.i.getCapacityPerTimestep() * turn.i.getNumLanes()));
                    }
                    catch(RuntimeException ex)
                    {
                        throw ex;
                    }

                }

                double rem_time = curr_phase.getRemainingTime(curr_time);
                double rem_time_dt = Math.min(time_rem, rem_time);

                if(rem_time <= time_rem)
                {
                    curr_idx++;
                }

                time_rem -= rem_time_dt;
                curr_time += rem_time_dt;
                
                if(curr_idx == phases.size())
                {
                    curr_idx = 0;
                    curr_time -= total_time;
                    
                    if (phases.size() > 0) {
                        assignGreenTimes();
                    }
                }
            }
        }
        else
        {
            for(Link i : turns.keySet())
            {
                for(Link j : turns.get(i).keySet())
                {
                    PhaseMovement movement = turns.get(i).get(j);

                    movement.newTimestep();
                    movement.addMaxFlow(1000);
                }
            }
        }
    }

    /**
     * Implements a time-step of movement of vehicles at the intersection with 
     * the signal.
     * {@link Vehicle}s that do not have a next {@link Link} (some buses) are removed from the network.
     * {@link Vehicle}s that move are removed from their previous {@link Link} ({@link Link#removeVehicle(avdta.vehicle.Vehicle)} and added to their next {@link Link} ({@link Link#addVehicle(avdta.vehicle.Vehicle)}).
     * This requires that {@link Vehicle#getPrevLink()} is the incoming {@link Link} and {@link Vehicle#getNextLink()} is the outgoing {@link Link}.
     * @return Returns the number of vehicle which has exited the network.
     */
    public int step()
    {
        Node node = getNode();
        
        int exited = 0;
        
        calcGreenTime();
        
        
        int moved = 0;
        for(Link i : turns.keySet())
        {
            Map<Link, PhaseMovement> temp = turns.get(i);
            
            for(Link j : temp.keySet())
            {
                PhaseMovement mvt = temp.get(j);
                mvt.q = mvt.Q;
                
                mvt.addLeftovers();
                mvt.q = Math.ceil(mvt.q);

            }
            
            i.S = i.getNumSendingFlow();
        }
        
        for(Link j : getNode().getOutgoing())
        {
            j.R = j.getReceivingFlow();
        }
        
        
        List<Link> inc = new ArrayList<Link>();
        
        for(Link i : node.getIncoming())
        {
            if(i instanceof TransitLane)
            {
                inc.add(i);
            }
        }
        
        for(Link i : node.getIncoming())
        {
            if(!(i instanceof TransitLane))
            {
                inc.add(i);
            }
        }
        
        for(Link i : inc)
        {
            // multiple queues
            List<Vehicle> sending = i.getVehiclesCanMove();

            for(Vehicle v : sending)
            {

                for (MPTurn mpTurn : MPTurns) {
                    if (mpTurn.i == i && mpTurn.j == v.getNextLink()) {
                        mpTurn.updateAvgWaitingTime(v.enter_time, Simulator.time);
                    }
                }

                Link j = v.getNextLink();

                if(j == null)
                {
                    i.removeVehicle(v);
                    v.exited();
                    exited++;

                    continue;
                }
                double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                

                PhaseMovement movement = (turns.get(i) != null? turns.get(i).get(j) : null);


                double receivingFlow = j.scaleReceivingFlow(v);
                
                // always move vehicles onto centroid connectors
                if(j.isCentroidConnector())
                {
                    i.S -= equiv_flow;
                    i.removeVehicle(v);
                    j.addVehicle(v);

                    
                }
                else if(i.isCentroidConnector() && j.R >= receivingFlow)
                {
                    j.R -= receivingFlow;

                    i.removeVehicle(v);
                    j.addVehicle(v);
                    moved++;

                }
                else if(j.R >= receivingFlow && movement != null && movement.hasAvailableCapacity(equiv_flow))
                {
                    i.S -= equiv_flow;
                    i.q += equiv_flow;

                    j.R -= receivingFlow;

                    movement.update(equiv_flow);

                    i.removeVehicle(v);
                    j.addVehicle(v);
                    moved++;     
                }
                else
                {

                }
               

            }
        }
        return exited;
    }
    
    public List<MPTurn> getMPTurns() {
        return MPTurns;
    }
}
