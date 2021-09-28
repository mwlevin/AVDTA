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
public class TrafficSignal extends IntersectionControl implements Signalized
{
    private List<Phase> phases;
    private int curr_idx;
    private double curr_time;
    private double total_time;
    
    private double offset;
    
    private Map<Link, Map<Link, PhaseMovement>> turns;
    /**
     * Instantiates a traffic signal with null.
     */
    public TrafficSignal()
    {
        this(null);
    }
    /**
     * Instantiates a traffic signal at a given {@link Intersection}.
     * @param n An intersection at which traffic signal needs to be implemented.
     */
    public TrafficSignal(Intersection n)
    {
        super(n);
        
        phases = new ArrayList<Phase>();
        turns = new HashMap<Link, Map<Link, PhaseMovement>>();
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
    
    /**
     * Resets the current phase and the current time to 0, inserts the mapping 
     * of the phase movements in turns, and sets the start time of each phase in 
     * the cycle.
     */
    public void initialize()
    {
        Collections.sort(phases);
        
        
        /*
        if(phases.size() == 0)
        {
            // no phases:
            // create a phase for each incoming link
            int seq = 1;
            Node node = getNode();
            for(Link i : node.getIncoming())
            {
                if(!i.isCentroidConnector())
                {
                    List<Turn> turns = new ArrayList<Turn>();
                    for(Link j : node.getOutgoing())
                    {
                        if(!j.isCentroidConnector())
                        {
                            turns.add(new Turn(i, j));
                        }
                    }
                    addPhase(new Phase(seq, turns, 6, 0, 0));
                }
            }
        }
        */
        
        if(phases.size() > 0)
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
            Node node = getNode();
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

    
    /**
     * Ignore this function/method.
     */
    /*
    public void calcAvgGreenTime()
    {
        for(Link i : turns.keySet())
        {
            for(Link j : turns.get(i).keySet())
            {
                PhaseMovement movement = turns.get(i).get(j);
                
                movement.newTimestep();
                
            }
        }
        
        for(Link j : getNode().getOutgoing())
        {
            j.R = j.getReceivingFlow();
        }
        
        for(Phase p : phases)
        {
            double green_time = p.getGreenTime();
            
            for(Turn turn : p.getAllowed())
            {
                
                try
                {
                    turns.get(turn.i).get(turn.j).addMaxFlow( (green_time / total_time) * turn.j.R);
                }
                catch(RuntimeException ex)
                {
                    System.out.println(turn.i+" "+turn.j);
                    throw ex;
                }
            }
        }
    }
    */
    
    /**
     * Maps the green time of the various phases to the discretized time setting.
     */
    public void calcGreenTime()
    {
        if(phases.size() > 0)
        {
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
        
        for(Link i : inc) //iterate through all the incoming links
        {
            // multiple queues
            List<Vehicle> sending = i.getVehiclesCanMove();

            for(Vehicle v : sending)
            {
                Link j = v.getNextLink(); //the next link for vehicle v
                //System.out.println("Veh " + v.toString() + " at node " + node.toString() + " moving to " + j.toString());

                if(j == null)
                {
                    //System.out.println(v.toString() + " has exited the network");
                    i.removeVehicle(v);
                    v.exited();
                    exited++;

                    continue;
                }
                double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                

                PhaseMovement movement = (turns.get(i) != null? turns.get(i).get(j) : null);


                double receivingFlow = j.scaleReceivingFlow(v); // this is set to 1
                
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
                    //System.out.println(v.toString() + " moving from " + i.toString() + " to " + j.toString());
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
                    //System.out.println("No turning movement occured for Veh " + v.toString() + " at node " + node.toString() + " moving to " + j.toString());
                    //System.out.println(j.toString() + "R = " + j.R + "      recievingFlow: " + receivingFlow);
                    /*System.out.println(j.toString()+ " Reviving flow constraint: " + (j.R >= receivingFlow) +
                            "\nmovment != null constraint:" + (movement != null) + "\nAvail capacity constraint: " + (movement.hasAvailableCapacity(equiv_flow)));*/
                }
               

            }
        }
        return exited;
    }
}
