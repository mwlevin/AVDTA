/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav.dispatch;

import avdta.network.Path;
import avdta.network.Simulator;
import static avdta.network.Simulator.ast_duration;
import avdta.network.cost.TravelCost;
import avdta.network.node.Node;
import avdta.project.SAVProject;
import avdta.sav.SAVDest;
import avdta.sav.SAVOrigin;
import avdta.sav.SAVSimulator;
import avdta.sav.Taxi;
import avdta.sav.Traveler;
import avdta.vehicle.DriverType;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Michael
 */
public class DefaultDispatch extends Dispatch
{
    private TreeSet<Traveler> waiting;
    private Map<SAVOrigin, TreeSet<Taxi>> freeTaxis;

    
    
    private boolean relocate;
    private boolean ride_share;
    private double cost_factor;
    
    
    
    public DefaultDispatch()
    {
    }
    
    public void initialize(SAVProject project, SAVSimulator simulator)
    {
        super.initialize(project, simulator);
        
        relocate = project.getOption("relocate").equalsIgnoreCase("true");
        ride_share = project.getOption("ride-sharing").equalsIgnoreCase("true");
        cost_factor = Double.parseDouble(project.getOption("cost-factor"));
        
        waiting = new TreeSet<Traveler>();
        freeTaxis = new HashMap<SAVOrigin, TreeSet<Taxi>>();
        
        for(Node n : simulator.getNodes())
        {
            if(n instanceof SAVOrigin)
            {
                freeTaxis.put((SAVOrigin)n, new TreeSet<Taxi>());
            }
        }
        
        
    }
    
    
    
    public void reset()
    {
        super.reset();
        waiting.clear();
    }
    
    public void newTraveler(Traveler person)
    {
        waiting.add(person);
        
        for(Taxi t : person.getOrigin().getParkedTaxis())
        {
            if(t.getPath() == null)
            {
                assignTaxi(t);
            }
        }
    }
    
    
    
    public void relocateTaxis()
    {

        
        Set<SAVOrigin> over = new TreeSet<SAVOrigin>(new Comparator<SAVOrigin>()
        {
            public int compare(SAVOrigin lhs, SAVOrigin rhs)
            {
                if(lhs.storedDiff != rhs.storedDiff)
                {
                    return rhs.storedDiff - lhs.storedDiff;
                }
                else
                {
                    return lhs.getId() - rhs.getId();
                }
            }
        });
        
        Set<SAVOrigin> under = new TreeSet<SAVOrigin>(new Comparator<SAVOrigin>()
        {
            public int compare(SAVOrigin lhs, SAVOrigin rhs)
            {
                if(lhs.storedDiff != rhs.storedDiff)
                {
                    return lhs.storedDiff - rhs.storedDiff;
                }
                else
                {
                    return lhs.getId() - rhs.getId();
                }
            }
        });
        
        for(SAVOrigin o : freeTaxis.keySet())
        {
            int freecount = 0;
            
            for(Taxi t : freeTaxis.get(o))
            {
                if(Simulator.time - t.park_time > 0*60)
                {
                    freecount++;
                }
            }
            
            o.storedDiff = freecount - o.getStoredGoal();
            
            
            if(o.storedDiff < 0)
            {
                under.add(o);
            }
            else if(o.storedDiff > 0)
            {
                over.add(o);
            }
        }
        
        int count = 0;
        
        double epsilon = 0;
        
        
        // # over = # under
        // each taxi over, send to nearest location that is under
        
        outer: for(SAVOrigin r : over)
        {
            while(r.storedDiff > Math.round(epsilon*r.getStoredGoal()))
            {
                // find closest origin 
                SAVOrigin closest = null;
                double dist = Integer.MAX_VALUE;
                
                for(SAVOrigin s : under)
                {
                    double temp = getPath(r, (SAVDest)s.getLinkedZone()).getCost();
                    if(s.storedDiff < Math.round(-epsilon*s.getStoredGoal()) && temp < dist)
                    {
                        closest = s;
                        dist = temp;
                    }
                }
                
                if(closest == null)
                {
                    continue outer;
                }
                
                int numToSend = (int)Math.round(Math.min(r.storedDiff - epsilon*r.getStoredGoal(), -closest.storedDiff + epsilon*closest.getStoredGoal()));
                r.storedDiff -= numToSend;
                closest.storedDiff += numToSend;
                
                SAVDest dest = (SAVDest)closest.getLinkedZone();
                
                for(int i = 0; i < numToSend; i++)
                {
                    freeTaxis.get(r).pollFirst().setPath(getPath(r, dest));
                    count++;
                }
            }
        }
        /*
        for(SAVOrigin o : freeTaxis.keySet())
        {
            if(o.storedDiff != 0)
            {
                System.out.println(o.getId()+" "+o.storedDiff);
            }
        }
        */
        /*
        for(SAVOrigin r : over)
        {
            for(SAVOrigin s : under)
            {
                
                if(paths.get(r).get(s.getLinkedZone()).getCost() < 60*60)
                {
                    while(r.storedDiff > epsilon*r.getStoredGoal() && s.storedDiff < -epsilon*s.getStoredGoal())
                    {
                        r.storedDiff--;
                        s.storedDiff++;
                        freeTaxis.get(r).pollFirst().setPath(paths.get(r).get(s.getLinkedZone()));
                        count++;
                    }
                }
            }
        }
        */

        //System.out.println("Relocating "+count);
    }
    
    public void travelerDeparted(Taxi taxi, Traveler person)
    {
        waiting.remove(person);
    }
    
    /*
    public void assignTaxiToTraveler(Taxi taxi, Traveler person)
    {
        removeFreeTaxi(taxi);
        
        simulator.addTravelerToTaxi(taxi, person);
        
        Path firstPath = paths.get(person.getOrigin()).get(person.getDest());
        
        
        if(ride_share)
        {
            double arr_time = Simulator.time + (int)firstPath.getCost();
            
            Set<Traveler> left = new HashSet<Traveler>();

            for(Traveler p : person.getOrigin().getWaitingTravelers())
            {
                if(p != person && p.getDest() == person.getDest() && taxi.getNumPassengers() < taxi.getCapacity())
                {
                    simulator.addTravelerToTaxi(taxi, p);
                    left.add(p);
                }
            }
            
            
            
            

            for(Traveler t : left)
            {
                person.getOrigin().removeTraveler(t);
            }
        }
        
        taxi.setPath(firstPath);
    }
    */
    
    public void assignTaxi(Taxi taxi)
    {
        SAVOrigin origin = taxi.getLocation();
        
        TreeSet<Traveler> waitingList = origin.getWaitingTravelers();
        
        if(waitingList.size() == 0)
        {
            return;
        }
        
        removeFreeTaxi(taxi);
        
        if(!ride_share)
        {
            Traveler person = waitingList.pollFirst();
            getSimulator().addTravelerToTaxi(taxi, person);
            
            Path firstPath = getPath(person.getOrigin(), person.getDest());
            taxi.setPath(firstPath);
            
        }
        else
        {
            // 1st person on waiting list always goes first.
            // other people may add to the end in order
            // Clarke-Wright merge algoritm: merge routes into 1st person's route



            Traveler first = waitingList.pollFirst();
            getSimulator().addTravelerToTaxi(taxi, first);
            int tt = 0;


            SAVDest dest = first.getDest();

            Path firstPath = getPath(origin, dest);
            taxi.setPath(firstPath);

            // match travelers to first
            Set<Traveler> removed = new HashSet<Traveler>();

            for(Traveler t : waitingList)
            {
                if(dest == t.getDest())
                {
                    getSimulator().addTravelerToTaxi(taxi, t);
                    removed.add(t);

                    if(taxi.getNumPassengers() >= taxi.getCapacity())
                    {
                        break;
                    }
                }
            }

            for(Traveler t : removed)
            {
                waitingList.remove(t);
            }

            assignTaxi_ride_share(taxi);
        }
    }
    
    // initial condition: waitingList contains no travelers going to destinations already visited in the order list
    public void assignTaxi_ride_share(Taxi taxi)
    {
        SAVOrigin origin = taxi.getLocation();
        
        TreeSet<Traveler> waitingList = origin.getWaitingTravelers();
        
        List<Traveler> passengers = taxi.getPassengers();
        
        SAVDest lastDest = passengers.get(passengers.size()-1).getDest();
        SAVOrigin curr = origin;
        
        double tt = 0;
        
        for(Traveler t : passengers)
        {
            tt += getPath(curr, t.getDest()).getCost();
            curr = (SAVOrigin)t.getDest().getLinkedZone();
        }
        
        boolean found = true;
        
        Set<Traveler> removed = new HashSet<Traveler>();
        
        outer: while(found && passengers.size() < taxi.getCapacity())
        {
            // look for another traveler who does not exceed cost by joining at the end
            // prioritize by longest waiting
            found = false;
            
            for(Traveler t : waitingList)
            {
                double newTT = getPath(curr, t.getDest()).getCost() + tt;
                
                if(newTT < 60*60*5 && newTT < getPath(origin, t.getDest()).getCost() * cost_factor)
                {
                    
                    lastDest = t.getDest();
                    getSimulator().addTravelerToTaxi(taxi, t);
                    waitingList.remove(t);
                    
                    tt = newTT;
                    curr = (SAVOrigin)lastDest.getLinkedZone();
                    
                    found = true;
                    
                    break;
                }
            }
            
            for(Traveler t : removed)
            {
                waitingList.remove(t);
            }

            removed.clear();
            
            if(found)
            {
                for(Traveler t : waitingList)
                {
                    if(lastDest == t.getDest())
                    {
                        getSimulator().addTravelerToTaxi(taxi, t);
                        removed.add(t);

                        if(passengers.size() >= taxi.getCapacity())
                        {
                            break outer;
                        }
                    }
                }
                
                for(Traveler t : removed)
                {
                    waitingList.remove(t);
                }
                
                removed.clear();
            }
        }
        
        for(Traveler t : removed)
        {
            waitingList.remove(t);
        }

        removed.clear();
        
    }
    
    /**
     * Returns a list of waiting travelers.
     * @return a list of waiting travelers
     */
    public TreeSet<Traveler> getWaiting()
    {
        return waiting;
    }
    
    
    
    public void newTimestep()
    {
        if(Simulator.time % Simulator.ast_duration == 0)
        {
            for(Traveler t : waiting)
            {
                t.unable = false;
            }
        }
        
        Set<Traveler> handled = new HashSet<Traveler>();
        
        for(Traveler person : waiting)
        {
            if(person.unable)
            {
                continue;
            }
            
            int etd = person.getEtd();
            // if there are enough enroute taxis, stop
            
            if(person.getOrigin().getEnrouteTaxis() >= person.getOrigin().getNumWaiting() && 
                    etd - Simulator.time >= -10*60 && etd - Simulator.time <= 10*60)
            {
                continue;
            }
            
            
            
            // find closest free taxi
            double min = Integer.MAX_VALUE;
            Taxi best = null;
            Path path = null;
            
            for(SAVOrigin origin : freeTaxis.keySet())
            {
                if(freeTaxis.get(origin).size() == 0)
                {
                    continue;
                }


                Path temp = getPath(origin, (SAVDest)(person.getOrigin().getLinkedZone()));
                double cost = temp.getCost();

                if(cost < min)
                {
                    min = cost;
                    best = freeTaxis.get(origin).first();
                    path = temp;
                }

            }
            
            // assign taxi to travel to person
       
            if(best != null && 
                    (etd == Integer.MAX_VALUE || etd < Simulator.time || 
                    (etd > Simulator.time + min && etd > Simulator.time + Traveler.ALLOWED_DELAY)))
            {
                if(path.size() > 0)
                {
                    best.setPath(path);
                    person.setEtd((int)min);
                }
                handled.add(person);
                removeFreeTaxi(best);
                
                // can add enroute taxi twice because using set
                person.getOrigin().addEnrouteTaxi(best);
            }
            else if(freeTaxis.size() > 0)
            {/*
                Taxi tempTaxi = new Taxi(-1, person.getOrigin());
                tempTaxi.tempTaxi = true;
                taxis.add(tempTaxi);
                person.getOrigin().addParkedTaxi(tempTaxi);
                freeTaxis.get(person.getOrigin()).add(tempTaxi);
                handled.add(person);
                */
                /*person.enteredTaxi();
                tempTaxi.addPassenger(person);
                tempTaxi.setPath(paths.get(person.getOrigin()).get(person.getDest()));
                traveling++;*/
                
                
                person.unable = true;
            }
        }
        
        for(Traveler t : handled)
        {
            waiting.remove(t);
        }
        
        if(relocate /*&& time % ast_duration == 0*/)
        {
            relocateTaxis();
        }
    }
    
    
    
    public void taxiArrived(Taxi taxi, SAVOrigin node)
    {
        if(ride_share && 
                taxi.getNumPassengers() > 0 && taxi.getNumPassengers() < taxi.getCapacity() && 
                node.getNumWaiting() > 0)
        {
            Set<SAVDest> dests = new HashSet<SAVDest>();
            
            for(Traveler p : taxi.getPassengers())
            {
                dests.add(p.getDest());
            }
            
            taxi.setPath(SAVSimulator.dispatch.getPath(node, taxi.getPassengers().get(0).getDest()));
            
            Set<Traveler> removed = new HashSet<Traveler>();
            for(Traveler p : node.getWaitingTravelers())
            {
                if(dests.contains(p.getDest()))
                {
                    getSimulator().addTravelerToTaxi(taxi, p);
                    removed.add(p);

                    if(taxi.getNumPassengers() >= taxi.getCapacity())
                    {
                        break;
                    }
                }
            }

            for(Traveler p : removed)
            {
                node.removeTraveler(p);
            }
            
            if(taxi.getNumPassengers() < taxi.getCapacity())
            {
                assignTaxi_ride_share(taxi);
            }
            
        }
        
        
        if(taxi.getNumPassengers() > 0)
        {
            taxi.setPath(getPath(node, taxi.getPassengers().get(0).getDest()));
        }
        // if no travelers waiting, hold taxi
        else if(node.getNumWaiting() == 0)
        {
            addFreeTaxi(taxi);
        }
        // otherwise, give taxi to longest waiting traveler
        else
        {
            assignTaxi(taxi);
            taxi.delay = Taxi.DELAY_ENTER;
        }
    }
}
