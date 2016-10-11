/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
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
import avdta.sav.dispatch.Dispatch;
import avdta.gui.util.StatusUpdate;
import avdta.sav.dispatch.DefaultDispatch;

/**
 * This class extends {@link Simulator} to handle the simulation of travelers and taxis.
 * Along with {@link SAVOrigin}, {@link SAVDest}, and {@link Traveler}, the movement of travelers out of taxis, and the movement of taxis through the network, is fully defined.
 * The matching of SAVs to travelers is decided by the {@link Dispatch}. 
 * This class facilitates the dispatch by implementing traveler and vehicle behavior once their matchings and paths have been assigned.
 * 
 * {@link SAVSimulator} relies on a {@link Dispatch}
 * @author Michael
 */
public class SAVSimulator extends Simulator
{ 
    
    private List<Taxi> taxis;
    private List<Traveler> travelers;
    
    public static Dispatch dispatch;
    
    
    
    private static int traveling;

    /**
     * Constructs the simulator for the specified project.
     * @param project the project
     */
    public SAVSimulator(SAVProject project)
    {
        super(project);
        
        taxis = new ArrayList<Taxi>();
        setCostFunction(TravelCost.dnlTime);
        
        setDispatch(new DefaultDispatch());
    }
    
    /**
     * Constructs the simulator for the specified project, with the given sets of {@link Node}s and {@link Link}s.
     * @param project the project
     * @param nodes the set of nodes
     * @param links the set of links
     */
    public SAVSimulator(SAVProject project, Set<Node> nodes, Set<Link> links)
    {
        super(project, nodes, links);
        
        taxis = new ArrayList<Taxi>();
        setCostFunction(TravelCost.dnlTime);
        
        setDispatch(new DefaultDispatch());
    }
    
    /**
     * Updates the dispatch. 
     * This should not be called during simulation.
     * @param dispatch the new dispatch
     */
    public void setDispatch(Dispatch dispatch)
    {
        this.dispatch = dispatch;
    }
    
    /**
     * Returns the associated project.
     * @return the associated project
     */
    public SAVProject getProject()
    {
        return (SAVProject)super.getProject();
    }
    
    /**
     * Returns the list of all travelers.
     * @return the list of all travelers
     */
    public List<Traveler> getTravelers()
    {
        return travelers;
    }
    
    /**
     * Returns the total energy consumption by summing over all taxis.
     * This includes empty travel.
     * @return the total energy consumption
     */
    public double getTotalEnergy()
    {
        double output = 0.0;
        
        for(Taxi v : taxis)
        {
            output += v.getTotalEnergy();
        }
        
        return output;
    }
    
    /**
     * Returns the total vehicle miles traveled by summing over all taxis.
     * This includes empty travel.
     * @return the total vehicle miles traveled
     */
    public double getTotalVMT()
    {
         double output = 0.0;
        
        for(Taxi v : taxis)
        {
            output += v.total_distance;
        }
        
        return output;       
    }
    
    /**
     * Returns the empty vehicle miles traveled by summing over all taxis.
     * @return the empty vehicle miles traveled
     */
    public double getEmptyVMT()
    {
         double output = 0.0;
        
        for(Taxi v : taxis)
        {
            output += v.empty_distance;
        }
        
        return output;       
    }
    
    /**
     * Returns the average mpg over all taxis.
     * @return the average mpg
     */
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
    
    
    /**
     * Initializes the simulator.
     * This is called after reading in all data.
     * This also calls {@link Dispatch#initialize(avdta.project.SAVProject, avdta.sav.SAVSimulator)}.
     */
    public void initialize()
    {
        super.initialize();
        
        Set<Node> nodes = getNodes();
        
        dispatch.initialize(getProject(), this);
        
        
    }
    
    /**
     * Adds the taxi to the simulation, and parks it at its starting location (see {@link Taxi#getStartLocation()}.
     * @param taxi the taxi to be added
     */
    public void addTaxi(Taxi taxi)
    {
        taxis.add(taxi);
                
        ((SAVOrigin)taxi.getStartLocation()).addParkedTaxi(taxi);
    }
        
    
    /**
     * Updates the list of taxis.
     * @param taxis the new list of taxis
     */
    public void setTaxis(List<Taxi> taxis)
    {
        this.taxis = taxis;
    }
    
    /**
     * Updates the list of travelers, and sorts them by departure time.
     * @param travelers the new list of travelers
     */
    public void setTravelers(List<Traveler> travelers)
    {
        this.travelers = travelers;
        
        Collections.sort(travelers);
    }
    
    /**
     * Resets the simulator to restart the simulation.
     * This moves taxis to their start location (see {@link Taxi#getStartLocation()}) and registers them as a free taxi in the {@link Dispatch}.
     * This also calls {@link Traveler#reset()}.
     */
    public void resetSim()
    {
        super.resetSim();
        
        for(Taxi t : taxis)
        {
            t.getStartLocation().addParkedTaxi(t);
            t.setLocation(t.getStartLocation());
            dispatch.addFreeTaxi(t);
        }

        
        for(Traveler t: travelers)
        {
            t.reset();
        }
        
        
        
        traveler_idx = 0;
        
        traveling = 0;

    }
    
    
    
    int traveler_idx = 0;
    
    /**
     * Adds travelers and creates events for the {@link Dispatch}.
     * This method is called every time step as part of {@link SAVSimulator#simulate()}.
     * Every {@link Simulator#ast_duration}, this calls {@link Dispatch#updateShortestPaths()}.
     * Travelers departing at the specified time will be added to the appropriate origin.
     * Finally, this calls {@link Dispatch#newTimestep()}.
     */
    public void addVehicles()
    {
        super.addVehicles();
        
        // update shortest paths if needed
        if(time % ast_duration == 0)
        {
            
            
            if(statusUpdate != null)
            {
                statusUpdate.update((double) time / Simulator.duration, ((double)ast_duration) / Simulator.duration);
            }
            
            dispatch.updateShortestPaths();
        }
        
        
        
        // add travelers
        while(traveler_idx < travelers.size())
        {
            Traveler t = travelers.get(traveler_idx);

            if(t.getDepTime() <= Simulator.time)
            {
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
        dispatch.newTimestep();
    }
    
    
    

    /**
     * Returns the average waiting time over all travelers.
     * @return the average waiting time (s)
     */
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
            }
        }
        
        
        return output / count;
    }
    
    
    /**
     * Returns the average in-vehicle travel time over all travelers.
     * @return the average in-vehicle travel time (s)
     */
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
    
    /**
     * Returns the average travel time over all travelers.
     * This includes in-vehicle travel time and waiting time.
     * @return the average travel time (s)
     */
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
    
    /**
     * This adds the traveler to the taxi, and should be called by the {@link Dispatch} to handle all simulator updates associated with a traveler entering a taxi.
     * This should only be called when the taxi and the traveler are at the same location (the same {@link SAVOrigin}).
     * This calls {@link Dispatch#travelerDeparted(avdta.sav.Taxi, avdta.sav.Traveler)} to notify the {@link Dispatch} and {@link Traveler#enteredTaxi()}.
     * This also causes the taxi to dwell at its location for {@link Taxi#DELAY_ENTER} seconds.
     * @param taxi the taxi
     * @param person the traveler
     */
    public void addTravelerToTaxi(Taxi taxi, Traveler person)
    {
        dispatch.travelerDeparted(taxi, person);
        
        person.enteredTaxi();
        taxi.addPassenger(person);
        taxi.delay = Taxi.DELAY_ENTER;
        traveling++;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    /**
     * Returns a list of all taxis.
     * @return a list of all taxis
     */
    public List<Taxi> getTaxis()
    {
        return taxis;
    }
    
    /**
     * Returns the number of traveler trips.
     * @return the number of traveler trips
     */
    public int getNumTrips()
    {
        return travelers.size();
    }
    
    
    
    /**
     * Checks whether the simulation has finished. 
     * This requires that all travelers have exited.
     * @return if all travelers have exited
     */
    public boolean isSimulationFinished()
    {
        return exit_count == travelers.size();
    }

    /**
     * Called when the simulation has finished.
     * This updates the {@link StatusUpdate} to update the GUI,
     */
    public void simulationFinished()
    {
        if(statusUpdate != null)
        {
            statusUpdate.update(1, 0);
        }
    }
    
    
}
