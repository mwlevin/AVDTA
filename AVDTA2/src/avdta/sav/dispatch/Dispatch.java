/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav.dispatch;

import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.network.node.Node;
import avdta.project.SAVProject;
import avdta.sav.SAVDest;
import avdta.sav.SAVOrigin;
import avdta.sav.SAVSimulator;
import avdta.sav.Taxi;
import avdta.sav.Traveler;
import avdta.vehicle.DriverType;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * This class defines methods called by {@link SAVSimulator} and {@link SAVOrigin} to dispatch taxis.
 * Subclasses defining dispatch behavior should override these methods.
 * The following behaviors are undefined:
 * taxi route choice, matching travelers to taxis
 * However, travelers will exit a taxi that arrives at their destination.
 * This class does not have to include that.
 * After arriving at a centroid, a taxi's path will be null.
 * Assigning a valid path to a taxi at an origin will cause the taxi to try to depart.
 * (If travelers entered the taxi, the taxi must first wait for {@link Taxi#DELAY_ENTER}.)
 * 
 * {@link SAVOrigin} contains a set of unassigned taxis. 
 * See {@link SAVOrigin#addFreeTaxi(avdta.sav.Taxi)} and {@link SAVOrigin#removeFreeTaxi(avdta.sav.Taxi)} to make use of this feature.
 * The dispatch should update these sets, if they are to be used.
 * When a taxi arrives {@link Dispatch#taxiArrived(avdta.sav.Taxi, avdta.sav.SAVOrigin)} it should be added to the appropriate free list, if it is not immediately assigned to another trip.
 * When a taxi departs, it should be removed from the appropriate free list.
 * 
 * {@link SAVOrigin} also contains a list of enroute taxis.
 * Whenever a taxi changes paths, it will update the lists of enroute taxis via {@link SAVOrigin#addEnrouteTaxi(avdta.sav.Taxi)} and {@link SAVOrigin#removeEnrouteTaxi(avdta.sav.Taxi)}.
 * These methods also update traveler estimated departure times, based on a first-come-first-served assumption.
 * 
 * 
 * 
 * @author Michael
 */
public abstract class Dispatch 
{
    public static final String[] DISPATCHERS = new String[]{"default"};
    public static final int DEFAULT = 0;
    
    public static int findDispatch(String d)
    {
        for(int i = 0; i < DISPATCHERS.length; i++)
        {
            if(d.equalsIgnoreCase(DISPATCHERS[i]))
            {
                return i;
            }
        }
        return -1;
    }
    
    public static Dispatch createDispatch(String d)
    {
        int idx = findDispatch(d);
        
        switch(idx)
        {
            case DEFAULT:
                return new DefaultDispatch();
            default:
                throw new RuntimeException("Dispatcher \""+d+"\" not found.");
        }
    }
    
    private Map<SAVOrigin, Map<SAVDest, Path>> paths;
    
    private SAVSimulator simulator;
    
    /**
     * Constructs the dispatch.
     */
    public Dispatch()
    {
        
    }
    
    /**
     * Returns the simulator in use.
     * @return the simulator in use
     */
    public SAVSimulator getSimulator()
    {
        return simulator;
    }
    
    /**
     * This method is called by {@link SAVOrigin} when a taxi arrives at an {@link SAVOrigin}.
     * (Taxis arriving at {@link SAVDest}inations may dwell there for some time before moving to the {@link SAVOrigin}.
     * Note that the taxi may have passengers currently in it, but all passengers destined for the linked {@link SAVDest} have already exited.
     * @param taxi the taxi that arrived
     * @param node the {@link SAVOrigin} the taxi arrived at
     */
    public abstract void taxiArrived(Taxi taxi, SAVOrigin node);
    
    /**
     * This method is called by {@link SAVSimulator} every time step.
     */
    public abstract void newTimestep();
    
    /**
     * This method is called by {@link SAVOrigin} when a traveler departs and requests a taxi.
     * @param person the departing traveler
     */
    public abstract void newTraveler(Traveler person);
    
    /**
     * This method is called by {@link SAVSimulator} after data has been read.
     * It should be used to initialize any instance variables that are dependent on the project or network.
     * Subclasses should call {@link Dispatch#initialize(avdta.project.SAVProject, avdta.sav.SAVSimulator)}.
     * 
     * @param project the project
     * @param simulator the simulator
     */
    public void initialize(SAVProject project, SAVSimulator simulator)
    {
        this.simulator = simulator;
        paths = new HashMap<SAVOrigin, Map<SAVDest, Path>>();
        
        for(Node n : simulator.getNodes())
        {
            if(n instanceof SAVOrigin)
            {
                paths.put((SAVOrigin)n, new HashMap<SAVDest, Path>());
            }
        }
        
        for(Node n : simulator.getNodes())
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
    
    
    
    /**
     * This method is called by {@link SAVSimulator} to restart the simulation.
     * It should return the dispatch to the appropriate state for the start of simulation.
     * 
     * Subclasses should call {@link Dispatch#reset()}.
     */
    public void reset(){}
    
    /**
     * This is called when a traveler departs an origin.
     * @param taxi the taxi the traveler departed on
     * @param person the departing traveler
     */
    public abstract void travelerDeparted(Taxi taxi, Traveler person);
    
    /**
     * Adds a taxi to the list of free taxis.
     * @param t the list of free taxis
     */
    public void addFreeTaxi(Taxi t)
    {
        t.getLocation().addFreeTaxi(t);
    }
    
    /**
     * Removes a taxi from the list of free taxis.
     * @param t the taxi to be removed
     */
    public void removeFreeTaxi(Taxi t)
    {
        t.getLocation().removeFreeTaxi(t);
    }
    
    /**
     * Returns the stored path associated with the origin and destination.
     * Paths are updated every {@link Simulator#ast_duration}.
     * 
     * @param o the origin
     * @param d the destination
     * @return the stored path 
     */
    public Path getPath(SAVOrigin o, SAVDest d)
    {
        return paths.get(o).get(d);
    }
    
    /**
     * Updates the stored path associated with the origin and destination.
     * Note that paths are updated every {@link Simulator#ast_duration}.
     * 
     * @param o the origin
     * @param d the destination
     * @param p the new path
     */
    public void setPath(SAVOrigin o, SAVDest d, Path p)
    {
        paths.get(o).put(d, p);
    }
    
    public void updateShortestPaths()
    {
        
        TravelCost costFunc = simulator.getCostFunction();
        
        for(SAVOrigin o : paths.keySet())
        {
            //System.out.println("\t"+o);
            Map<SAVDest, Path> temp = paths.get(o);
            
            simulator.link_dijkstras(o, null, Simulator.time, 1.0, DriverType.AV, costFunc);
            
            for(SAVDest d : temp.keySet())
            {
                Path path = simulator.link_trace(o, d);
                
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
