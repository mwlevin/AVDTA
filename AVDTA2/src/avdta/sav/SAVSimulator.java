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

    public SAVSimulator(SAVProject project)
    {
        super(project);
        
        taxis = new ArrayList<Taxi>();
        setCostFunction(TravelCost.dnlTime);
        
        setDispatch(new DefaultDispatch());
    }
    
    
    public SAVSimulator(SAVProject project, Set<Node> nodes, Set<Link> links)
    {
        super(project, nodes, links);
    }
    
    public void setDispatch(Dispatch dispatch)
    {
        this.dispatch = dispatch;
    }
    
    public SAVProject getProject()
    {
        return (SAVProject)super.getProject();
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
    
    
    
    public void initialize()
    {
        super.initialize();
        
        Set<Node> nodes = getNodes();
        
        dispatch.initialize(getProject(), this);
        
        
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
