/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.cost.TravelCost;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import avdta.project.SAVProject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Michael
 */
public class SAVSimulator extends Simulator
{ 
    
    private List<Taxi> taxis;
    private List<Traveler> travelers;
    
    private static TreeSet<Traveler> waiting;
    private static Map<SAVOrigin, TreeSet<Taxi>> freeTaxis;

    private static Map<SAVOrigin, Map<SAVDest, Path>> paths;
    
    public static boolean relocate;
    public static boolean ride_share;
    public static double cost_factor;
    
    private static int traveling;

    public SAVSimulator(SAVProject project)
    {
        super(project);
        
        taxis = new ArrayList<Taxi>();
        paths = new HashMap<SAVOrigin, Map<SAVDest, Path>>();
        waiting = new TreeSet<Traveler>();
        freeTaxis = new HashMap<SAVOrigin, TreeSet<Taxi>>();

        setCostFunction(TravelCost.dnlTime);
        relocate = project.getOption("relocate").equalsIgnoreCase("true");
        ride_share = project.getOption("ride-sharing").equalsIgnoreCase("true");
        cost_factor = Double.parseDouble(project.getOption("cost-factor"));

    }
    
    
    public SAVSimulator(SAVProject project, Set<Node> nodes, Set<Link> links)
    {
        super(project, nodes, links);
        
        taxis = new ArrayList<Taxi>();
        paths = new HashMap<SAVOrigin, Map<SAVDest, Path>>();
        waiting = new TreeSet<Traveler>();
        freeTaxis = new HashMap<SAVOrigin, TreeSet<Taxi>>();

        setCostFunction(TravelCost.dnlTime);
        relocate = project.getOption("relocate").equalsIgnoreCase("true");
        ride_share = project.getOption("ride-sharing").equalsIgnoreCase("true");
        cost_factor = Double.parseDouble(project.getOption("cost-factor"));

    }
    
    public List<Traveler> getTravelers()
    {
        return travelers;
    }
    
    public double getTotalEnergy()
    {
        double output = 0.0;
        
        for(Taxi v : taxis)
        {
            output += v.getTotalEnergy();
        }
        
        return output;
    }
    
    public double getTotalVMT()
    {
         double output = 0.0;
        
        for(Taxi v : taxis)
        {
            output += v.total_distance;
        }
        
        return output;       
    }
    
    public double getEmptyVMT()
    {
         double output = 0.0;
        
        for(Taxi v : taxis)
        {
            output += v.empty_distance;
        }
        
        return output;       
    }
    
    public double getAvgMPG()
    {
        double total = 0;
        double count = 0;
        
        for(Vehicle v : taxis)
        {
            if(v.getExitTime() < Simulator.duration && v.getTotalEnergy() > 0)
            {
                total += v.getMPG();
                count++;
            }
        }
        
        return total / count;
    }
    
    public static Path getPath(SAVOrigin o, SAVDest d)
    {
        return paths.get(o).get(d);
    }
    
    public void initialize()
    {
        super.initialize();
        
        Set<Node> nodes = getNodes();
        
        for(Node n : nodes)
        {
            if(n instanceof SAVOrigin)
            {
                paths.put((SAVOrigin)n, new HashMap<SAVDest, Path>());
                
                freeTaxis.put((SAVOrigin)n, new TreeSet<Taxi>());
            }
        }
        
        for(Node n : nodes)
        {
            if(n instanceof SAVDest)
            {
                for(SAVOrigin o : paths.keySet())
                {
                    paths.get(o).put((SAVDest)n, null);
                }
            }
        }
    }
    
    public void addTaxi(Taxi taxi)
    {
        taxis.add(taxi);
                
        ((SAVOrigin)taxi.getOrigin()).addParkedTaxi(taxi);
    }
        
    
    public void setTaxis(List<Taxi> taxis)
    {
        this.taxis = taxis;
    }
    
    public void setTravelers(List<Traveler> travelers)
    {
        this.travelers = travelers;
        
        Collections.sort(travelers);
    }
    
    public void resetSim()
    {
        super.resetSim();
        
        for(Taxi t : taxis)
        {
            t.getStartLocation().addParkedTaxi(t);
            t.setLocation(t.getStartLocation());
            addFreeTaxi(t);
        }

        
        for(Traveler t: travelers)
        {
            t.reset();
        }
        
        waiting.clear();
        
        traveler_idx = 0;
        
        traveling = 0;

    }
    
    
    
    int traveler_idx = 0;
    
    public void addVehicles()
    {
        // update shortest paths if needed
        if(time % ast_duration == 0)
        {
            updateShortestPaths();
            
            if(statusUpdate != null)
            {
                statusUpdate.update((double) time / Simulator.duration);
            }
            
            for(Traveler t : waiting)
            {
                t.unable = false;
            }
            
            if(print_status)
            {
                int numFreeTaxis = 0;

                for(SAVOrigin o : freeTaxis.keySet())
                {
                    numFreeTaxis += freeTaxis.get(o).size();
                }
                
                System.out.println(Simulator.time+" "+numFreeTaxis+" "+exit_count+" "+waiting.size());
            }
        }
        
        // add travelers
        while(traveler_idx < travelers.size())
        {
            Traveler t = travelers.get(traveler_idx);

            if(t.getDepTime() <= Simulator.time)
            {
                waiting.add(t);
                ((SAVOrigin)t.getOrigin()).addTraveler(t);
                traveler_idx++;
            }
            else
            {
                break;
            }
        }

        
        
        // assign taxis
        // initial rule: assign nearest unused taxi

        Set<Traveler> handled = new HashSet<Traveler>();
        
        for(Traveler person : waiting)
        {
            if(person.unable)
            {
                continue;
            }
            
            
            // if there are enough enroute taxis, stop
            
            if(person.getOrigin().getEnrouteTaxis() >= person.getOrigin().getNumWaiting() && person.etd - Simulator.time >= -10*60 && person.etd - Simulator.time <= 10*60)
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

                Path temp = paths.get(origin).get(person.getOrigin().getLinkedZone());
                double cost = temp.getCost();

                if(cost < min)
                {
                    min = cost;
                    best = freeTaxis.get(origin).first();
                    path = temp;
                }

            }
            
            // assign taxi to travel to person
            if(best != null && (person.etd == Integer.MAX_VALUE || person.etd < Simulator.time || (person.etd > Simulator.time + min && person.etd > Simulator.time + Traveler.ALLOWED_DELAY)))
            {
                if(path.size() > 0)
                {
                    best.setPath(path);
                    person.etd = (int)min;
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
                    double temp = paths.get(r).get(s.getLinkedZone()).getCost();
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
                    freeTaxis.get(r).pollFirst().setPath(paths.get(r).get(dest));
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
    

    
    public double getAvgWait()
    {
        double output = 0;
        
        int count = 0;
        
        for(Traveler t : travelers)
        {
            if(t.isExited())
            {
                output += t.getDelay();
                count++;
                
                //System.out.println(t.enter_time+" "+t.getDepTime()+" "+t.exit_time);
            }
        }
        
        System.out.println(count);
        
        return output / count;
    }
    
    public void testRelocate()
    {
        for(SAVOrigin o : paths.keySet())
        {
            System.out.println(o+" "+(o.getParkedTaxis().size() - o.getStoredGoal()));
        }
    }
    
    public double getAvgIVTT()
    {
        double output = 0;
        
        int count = 0;
        
        for(Traveler t : travelers)
        {
            if(t.isExited())
            {
                output += t.getIVTT();
                count++;
            }          
        }
        
        return output / count;
    }
    
    public double getAvgTT()
    {
        double output = 0;
        
        int count = 0;
        
        for(Traveler t : travelers)
        {
            if(t.isExited())
            {
                output += t.getTT();
                count++;
            }                  
        }
        
        return output / count;
    }
    
    
    public static void addTraveler(Taxi taxi, Traveler person)
    {
        waiting.remove(person);
        person.enteredTaxi();
        taxi.addPassenger(person);
        traveling++;
    }
    
    
    public static void assignTaxi(Taxi taxi, Traveler person)
    {
        removeFreeTaxi(taxi);
        /*
        waiting.remove(person);
        person.enteredTaxi();
        traveling++;
        taxi.addPassenger(person);
        */
        addTraveler(taxi, person);
        
        Path firstPath = paths.get(person.getOrigin()).get(person.getDest());
        
        
        if(ride_share)
        {
            double arr_time = Simulator.time + (int)firstPath.getCost();
            
            Set<Traveler> left = new HashSet<Traveler>();

            for(Traveler p : person.getOrigin().getWaitingTravelers())
            {
                if(p != person && p.getDest() == person.getDest() && taxi.getNumPassengers() < taxi.getCapacity())
                {
                    addTraveler(taxi, p);
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
    
    
    public static void assignTaxi(Taxi taxi)
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
            addTraveler(taxi, person);
            
            Path firstPath = paths.get(person.getOrigin()).get(person.getDest());
            taxi.setPath(firstPath);
            return;
        }
        
        // 1st person on waiting list always goes first.
        // other people may add to the end in order
        // Clarke-Wright merge algoritm: merge routes into 1st person's route
        
        
        
        Traveler first = waitingList.pollFirst();
        addTraveler(taxi, first);
        int tt = 0;
        
        
        SAVDest dest = first.getDest();
        
        Path firstPath = paths.get(origin).get(dest);
        taxi.setPath(firstPath);
        
        // match travelers to first
        Set<Traveler> removed = new HashSet<Traveler>();
        
        for(Traveler t : waitingList)
        {
            if(dest == t.getDest())
            {
                addTraveler(taxi, t);
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
    
    // initial condition: waitingList contains no travelers going to destinations already visited in the order list
    public static void assignTaxi_ride_share(Taxi taxi)
    {
        SAVOrigin origin = taxi.getLocation();
        
        TreeSet<Traveler> waitingList = origin.getWaitingTravelers();
        
        List<Traveler> passengers = taxi.getPassengers();
        
        SAVDest lastDest = passengers.get(passengers.size()-1).getDest();
        SAVOrigin curr = origin;
        
        double tt = 0;
        
        for(Traveler t : passengers)
        {
            tt += (int)paths.get(curr).get(t.getDest()).getCost();
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
                double newTT = paths.get(curr).get(t.getDest()).getCost()+ tt;
                if(newTT < 60*60*5 && newTT < paths.get(origin).get(t.getDest()).getCost() * cost_factor)
                {
                    
                    lastDest = t.getDest();
                    addTraveler(taxi, t);
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
                        addTraveler(taxi, t);
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
    
    static class Savings implements Comparable<Savings>
    {
        public int tt;
        public SAVDest i, j;
        
        public Savings(SAVDest i, SAVDest j, int tt)
        {
            this.i = i;
            this.j = j;
            this.tt = tt;
        }
        
        public int compareTo(Savings rhs)
        {
            return rhs.tt - tt;
        }
    }
    
    public TreeSet<Traveler> getWaiting()
    {
        return waiting;
    }
    
    public List<Taxi> getTaxis()
    {
        return taxis;
    }
    
    public int getNumTrips()
    {
        return travelers.size();
    }
    
    public static void addFreeTaxi(Taxi t)
    {
        freeTaxis.get(t.getLocation()).add(t);
    }
    
    public static void removeFreeTaxi(Taxi t)
    {
        freeTaxis.get(t.getLocation()).remove(t);
    }
    
    public boolean isSimulationFinished()
    {
        return exit_count == travelers.size() && waiting.size() == 0 && time > 3600;
    }

    public void simulationFinished()
    {
        if(statusUpdate != null)
        {
            statusUpdate.update(1);
        }
    }
    
    private void updateShortestPaths()
    {
        
        TravelCost costFunc = getCostFunction();
        
        for(SAVOrigin o : paths.keySet())
        {
            //System.out.println("\t"+o);
            Map<SAVDest, Path> temp = paths.get(o);
            
            node_dijkstras(o, Simulator.time, 1.0, DriverType.AV, costFunc);
            
            for(SAVDest d : temp.keySet())
            {
                Path path = node_trace(o, d);
                
                if(path.size() == 0)
                {
                    path.setCost(Integer.MAX_VALUE);
                }
                temp.put(d, path);
            }
        }
        
        // not sure if necessary
        for(SAVOrigin o : paths.keySet())
        {
            paths.get(o).put((SAVDest)o.getLinkedZone(), new Path());
        }
    }
}
