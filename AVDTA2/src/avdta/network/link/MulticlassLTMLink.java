/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package avdta.network.link;

import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.link.multiclassnewell.LaxHopf;
import avdta.network.link.multiclassnewell.Region;
import avdta.network.node.Highway;
import avdta.network.node.Intersection;
import avdta.network.node.IntersectionControl;
import avdta.network.type.Type;
import avdta.vehicle.Vehicle;
import avdta.vehicle.VehTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class implements the link transmission model. 
 * It uses {@link ChainedArray}s to store cumulative counts at the upstream and downstream ends, and stores vehicles in a {@link LinkedList}.
 * @author Michael
 */
public class MulticlassLTMLink extends Link
{
    private static final int max_region_duration = 0;
    
    private LinkedList<VehTime> queue;
    
    private LinkedList<Region> regions1;
    private LinkedList<Region> regions2; // subset of regions used for multiclass newell
    
    private FixedSizeAVRegionLL N_down;
    private FixedSizeAVRegionLL N_up;
    
    private boolean init;
    
    private double capacityUp, capacityDown;
    
    private double Q_factor;

    /**
     * Constructs the link with the given parameters 
     * @param id the link id
     * @param source the source node
     * @param dest the destination node
     * @param capacity the capacity per lane (veh/hr)
     * @param ffspd the free flow speed (mi/hr)
     * @param wavespd the congested wave speed (mi/hr)
     * @param jamd the jam density (veh/mi)
     * @param length the length (mi)
     * @param numLanes the number of lanes
     */
    public MulticlassLTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd = capacity / (jamd - capacity / ffspd), jamd, length, numLanes);
        //super(id, source, dest, capacity = ffspd * (wavespd/2 * jamd) / (ffspd + wavespd/2), ffspd, wavespd/2, jamd, length, numLanes);
        //super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);

        queue = new LinkedList<VehTime>();
        init = false;
        
        Q_factor = 1;
        
        double Q = Region.getCapacity(this, 0);
        Q_factor = capacity / Q;
        
        regions1 = new LinkedList<>();
        regions2 = new LinkedList<>();
        Region start = new Region(0, Simulator.dt, 0, Integer.MAX_VALUE, 0);
        regions1.add(start);
        regions2.add(start);
    }
    
    public double getQFactor()
    {
        return Q_factor;
    }
    
    public Iterable<Vehicle> getVehicles()
    {
        return new LTMIterable();
    }
    
    /**
     * Initializes this {@link LTMLink} after all data is read.
     * This creates the {@link ChainedArray}s used to store cumulative counts.
     */
    public void initialize()
    {
        N_up = new FixedSizeAVRegionLL(getUSLookBehind()+2);
        N_down = new FixedSizeAVRegionLL(getDSLookBehind()+2);
        
        regions1 = new LinkedList<>();
        regions2 = new LinkedList<>();
        Region start = new Region(0, Simulator.dt, 0, Integer.MAX_VALUE, 0);
        regions1.add(start);
        regions2.add(start);
        

        this.capacityUp = getCapacity() * Network.dt / 3600.0;
        this.capacityDown = getCapacity() * Network.dt / 3600.0;
        
        init = true;
        
        added_av_count = 0;

    }
    
    /**
     * Returns how far to look backwards in time for the upstream end
     * @return {@link Link#getLength()}/{@link Link#getFFSpeed()} (s)
     */
    public int getUSLookBehind()
    {
        return (int)Math.ceil(getLength()/getFFSpeed()*3600 / Network.dt);
    }
    
    /**
     * Returns how far to look backwards in time for the downstream end
     * @return {@link Link#getLength()}/{@link Link#getWaveSpeed()} (s)
     */
    public int getDSLookBehind()
    {
        return (int)Math.ceil(getLength()/getWaveSpeed()*3600 / Network.dt);
    }
    // includes fractions lost to discretization
    
    /**
     * Returns the current upstream capacity. This includes fractions lost to discretization.
     * @return the current upstream capacity
     */
    public double getCurrentUpstreamCapacity()
    {
        return capacityUp;
    }
    

    
    /**
     * Returns the current downstream capacity. This includes fractions lost to discretization.
     * @return the current downstream capacity
     */
    public double getCurrentDownstreamCapacity()
    {
        return capacityDown;
    }    
    
    /**
     * Returns the type code of this link
     * @return {@link ReadNetwork#LTM}
     */
    public Type getType()
    {
        return ReadNetwork.LTM;
    }
    
    /**
     * Resets this {@link LTMLink} to restart the simulation. This clears the {@link ChainedArray}s
     */
    public void reset()
    {
        queue.clear();
        N_up.clear();
        N_down.clear();
        
        regions1 = new LinkedList<>();
        regions2 = new LinkedList<>();
        Region start = new Region(0, Simulator.dt, 0, Integer.MAX_VALUE, 0);
        regions1.add(start);
        regions2.add(start);

        added_av_count = 0;
        super.reset();
    }
    
    /**
     * Executes one time step of simulation. 
     * This updates the current upstream and downstream capacities. 
     * Adding and removing vehicles occurs through {@link Node}s.
     */
    public void step()
    {
        
        
        
        capacityUp -= (int)capacityUp;
        
        //capacityUp += getCapacity() * Network.dt / 3600.0;
        
        int count = 0;
        int av = 0;
        
        for(Link us : getSource().getIncoming())
        {
            List<Vehicle> S = us.getSendingFlow();
            for(Vehicle v : S)
            {
                if(v.getNextLink() == this)
                {
                    count++;
                    
                    if(v.getDriver().isAV())
                    {
                        av++;
                    }
                }
            }
        }

        if(count > 0)
        {
            updateIncomingVehicles((double)av/count);
        }
        else
        {
            updateIncomingVehicles(0);
        }


        capacityDown -= (int)capacityDown;
        
        double newCapacity = 0;
        double tau_tilde = 0;
        
        int start_cc = getN_down(Simulator.time);
        
        for(Region r : regions1)
        {
            if(r.getUpperB() <= start_cc)
            {
                continue;
            }
            
            double tau = (r.getUpperB() - r.getLowerB()) / r.getCapacity(this) *3600;
            
            /*
            if(Simulator.time == 12)
            {
                System.out.println("tau="+tau+" b: "+r.getLowerB()+" to "+r.getUpperB()+" "+newCapacity);
                System.out.println(r.getCapacity(this));
                System.out.println(r.getAVProp());
            }
            */
            
            if(tau_tilde + tau >= Simulator.dt)
            {
                newCapacity += r.getCapacity(this) * (Simulator.dt - tau_tilde) / 3600.0;
                break;
            }
            else
            {
                newCapacity += r.getCapacity(this) / 3600.0 *tau;
                tau_tilde += tau;
            }
        }
        
        /*
        if(Simulator.time == 12)
        {
            System.out.println("Qnew="+newCapacity);
            System.out.println("Startcc="+start_cc);
            //System.exit(1);
        }
        */
 
        capacityDown += newCapacity;
        

    }
    
    // need to call this in upstream node class based on incoming vehicles
    private double next_AV_prop = 0;
    public void updateIncomingVehicles(double AV_prop)
    {
        next_AV_prop = AV_prop;
        
        capacityUp += Region.getCapacity(this, next_AV_prop) * Network.dt / 3600.0;
        regions1.getLast().setAVProp(next_AV_prop);
        
    }
    
    public void update()
    {
        
        
        Region last = regions2.getLast();
        int newN_up = N_up.getCC(Simulator.indexTime(Simulator.time)); 
        int lastN_up = N_up.getCC(Simulator.indexTime(Simulator.time)-1); 
        
        int change = newN_up - lastN_up;
        
        int denom = last.getUpperB() - last.getLowerB();

        double new_AV_prop = 0;
        if(denom > 0)
        {
            new_AV_prop = (double)added_av_count / denom;
        }
        
        if(change > 0)
        {
            last.setUpperB(newN_up);

            last.setAVProp(new_AV_prop);
            
        }
        
        // check to combine last 2 regions
        if(regions2.size() > 1)
        {
            Region nextLast = regions2.get(regions2.size()-2);
            
            if(nextLast.getCCDiff() == 0 || Math.abs(nextLast.getAVProp() - last.getAVProp()) < 0.05)
            {
                nextLast.setUpperB(last.getUpperB());
                double avgAVprop = (nextLast.getAVProp() * nextLast.getCCDiff() + last.getAVProp() * last.getCCDiff()) /
                        (nextLast.getCCDiff() + last.getCCDiff()); 
                        
                nextLast.setAVProp(avgAVprop);
                
                regions2.remove();
                regions1.remove();
            }
        }
        
        N_up.nextTimeStep();
        N_down.nextTimeStep();
        
        
        if(change > 0)
        {
            added_av_count = 0;
            Region newR = new Region(Simulator.time, Simulator.time+Simulator.dt, last.getUpperB(), Integer.MAX_VALUE, 0);
            regions1.add(newR);
            regions2.add(newR);
        }
        
        int start1 = N_up.getFirst().getInitialC();
        
        while(regions1.getFirst().getUpperB() < start1)
        {
            regions1.removeFirst();
        }
        
        int start2 = N_down.getFirst().getInitialC();
        
        while(regions2.getFirst().getUpperB() < start2)
        {
            regions2.removeFirst();
        }
        
        /*
        if(getId() == 23)
        {
            System.out.println(Simulator.time+" - "+getId()+" - "+regions2.size()+" "+regions1.size());
            System.out.println("\t"+regions2.getFirst().getUpperB()+" / "+start2+" "+
                    N_down.getFirst().getInitialC()+" "+N_up.getFirst().getInitialC());
        }
        */
        
    }
    
    /**
     * Adds the {@link Vehicle} to this link
     * @param veh the {@link Vehicle} to be added
     */
    public void addVehicle(Vehicle veh)
    {
        veh.enteredLink(this);
        
        queue.add(new VehTime(veh, Simulator.time));     
        
        addN_up(Simulator.time, 1);
        
        if(veh.getDriver().isAV())
        {
            added_av_count++;
        }

    }
    
    /**
     * Returns the number of {@link Vehicle}s on this link
     * @return the number of {@link Vehicle}s on this link
     */
    public int getOccupancy()
    {
        return queue.size();
    }
    
    /**
     * Returns the queue of {@link Vehicle}s. 
     * The queue is stored as a {@link LinkedList} of {@link VehTime}s, which contain the {@link Vehicle} arrival times used to determine sending flows.
     * @return the queue of {@link Vehicle}s
     */
    public LinkedList<VehTime> getQueue()
    {
        return queue;
    }
    
    /**
     * Returns the number of {@link Vehicle}s that could exit this link.
     * @return the size of the sending flow
     */
    public int getNumSendingFlow()
    {
        int S = (int)Math.min(getN_up(Simulator.time - getLength()/getFFSpeed()*3600 + Network.dt) - 
                getN_down(Simulator.time), getCurrentDownstreamCapacity());
        
        //if(getId() == 12)
        //System.out.println(Simulator.time+" - "+getOccupancy()+"\t"+getCurrentDownstreamCapacity()+"\tS="+S);
        return S;
    }
    
    
    /**
     * Returns the set of {@link Vehicle}s that could exit this link
     * @return the sending flow
     */
    public List<Vehicle> getSendingFlow()
    {
        List<Vehicle> output = new ArrayList<Vehicle>();
        
        int max = getNumSendingFlow();
        
        for(VehTime vt : queue)
        {
            if(max > 0)
            {
                output.add(vt.vehicle);
                
                if(vt.vehicle.arr_time < 0)
                {
                    vt.vehicle.arr_time = Simulator.time;
                    
                    
                }
                
                
                max--;
            }
            else
            {
                break;
            }
        }
        
        S = output.size();
        
        
        return output;
    }
    
    /**
     * Returns the sending flow
     * @return {@link LTMLink#getVehiclesCanMove()}
     */
    public List<Vehicle> getVehiclesCanMove()
    {
        return getSendingFlow();
    }
    
    /**
     * Removes the {@link Vehicle} from this link
     * @param veh the {@link Vehicle} to be removed
     * @return if the {@link Vehicle} was removed
     */
    public boolean removeVehicle(Vehicle veh)
    {
        Iterator<VehTime> iter = queue.iterator();
        
        while(iter.hasNext())
        {
            VehTime vt = iter.next();
            
            if(vt.vehicle == veh)
            {
                iter.remove();
                addN_down(Simulator.time, 1);
                updateTT(vt.vehicle);
                
                return true;
            }
        }
 
        return false;
    }
    
    /**
     * Returns the receiving flow for this time step
     * @return the receiving flow for this time step
     */
    public double getReceivingFlow()
    {

        LaxHopf calc = new LaxHopf(this, N_up, N_down, regions2);
        
        double output = calc.calculateN_up((Simulator.time + Simulator.dt)/3600.0, regions2.size()-1);
        
       
        double capacity = getCurrentUpstreamCapacity();
        
        //System.out.println(Simulator.time+" "+capacity+" "+output);
       
        double ret = Math.min(capacity, output);
        
        /*
        if(getId() == 23)
        {
            System.out.println("Q="+Region.getCapacity(this, next_AV_prop)+" w="+Region.getW(this, next_AV_prop));
            System.out.println("t="+Simulator.time+" R="+ret+" Q="+getCurrentUpstreamCapacity());
        }
        */
        
        
        return ret;
    }
    
    
    /**
     * Returns the number of vehicles waiting to exit, i.e. the component of sending flow unbounded by capacity.
     * @return the number of vehicles waiting to exit
     */
    public int getNumWaiting()
    {
        return getN_up(Simulator.time - getLength()/getFFSpeed()*3600 + Network.dt) - getN_down(Simulator.time);
    }
    
    
    private int added_av_count;
    /**
     * Adds to the upstream cumulative count.
     * @param t the time (s)
     * @param val the number of vehicles to add
     */
    public void addN_up(double t, int val)
    {
        N_up.addCC(Simulator.indexTime(t), val);
        
       
        
        
    }
    
    /**
     * Adds to the downstream cumulative count.
     * @param t the time (s)
     * @param val the number of vehicles to add
     */
    public void addN_down(double t, int val)
    {
        N_down.addCC(Simulator.indexTime(t), val);
    }
    

    /**
     * Returns the upstream cumulative count at the specified time
     * @param t the time (s)
     * @return the upstream cumulative count
     */
    public int getN_up(double t)
    {
        if(t < 0)
        {
            return 0;
        }
        else
        {
            int t_1 = (int)Math.floor(t / Network.dt);
            int t_2 = (int)Math.ceil(t / Network.dt);
            
            if(t_1 != t_2)
            {
                return (N_up.getCC(t_1) + N_up.getCC(t_2))/2;
            }
            else
            {
                return N_up.getCC(t_1);
            }
        }
    }
    
    /**
     * Returns the downstream cumulative count at the specified time
     * @param t the time (s)
     * @return the downstream cumulative count
     */
    public int getN_down(double t)
    {
        if(t < 0)
        {
            return 0;
        }
        else
        {
            int t_1 = (int)Math.floor(t / Network.dt);
            int t_2 = (int)Math.ceil(t / Network.dt);
            
            if(t_1 != t_2)
            {
                return (N_down.getCC(t_1) + N_down.getCC(t_2))/2;
            }
            else
            {
                return N_down.getCC(t_1);
            }
        }
    }
    
    class LTMIterable implements Iterable<Vehicle>
    {
        public Iterator<Vehicle> iterator()
        {
            return new LTMIterator(queue);
        }
    }
}



class MulticlassLTMIterator implements Iterator<Vehicle>
{
    private Iterator<VehTime> iter;
    public MulticlassLTMIterator(LinkedList<VehTime> queue)
    {
        this.iter = queue.iterator();
    }
    
    public boolean hasNext()
    {
        return iter.hasNext();
    }
    
    public Vehicle next()
    {
        VehTime next = iter.next();
        return next.vehicle;
    }
    
    public void remove()
    {
        iter.remove();
    }
}



