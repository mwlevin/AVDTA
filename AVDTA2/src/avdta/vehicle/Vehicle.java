/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.dta.DTASimulator;
import avdta.duer.DUERSimulator;
import avdta.duer.Incident;
import avdta.duer.VMS;
import avdta.vehicle.wallet.Wallet;
import ilog.concert.IloIntVar;
import avdta.network.Network;
import avdta.vehicle.fuel.VehicleClass;
import avdta.network.link.CTMLink;
import avdta.network.link.cell.Cell;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.vehicle.route.FixedPath;
import avdta.vehicle.route.RouteChoice;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * Represents a (single) vehicle in the network. <br>
 * By default the vehicle has a length of 19 ft. Each vehicle has an {@code id} 
 * to uniquely identify it in the network. {@code route} is a {@link Path} which 
 * tells us the links that the vehicle will be using. {@code net_enter_time} is 
 * time the vehicle enters the network.
 * @author ut
 */
public abstract class Vehicle implements Serializable, Comparable<Vehicle>
{   
    public static double vehicle_length = 19;
    
    
    private Wallet wallet;
    private int id;

    private RouteChoice routeChoice;
    
    private double total_toll;
    
    public int arr_time, enter_time, reservation_time; // arrival time at intersection
    public double pressure;
    
    public double bid;
    
    private int time_waiting;
    
    private Link curr;
    
    private int exit_time, net_enter_time;
    
    private Path path;
    
    private Incident incident;
    
    // energy
    private int prev_cell_time;
    private Cell prev_cell;
    public int cell_enter;
    private Cell curr_cell;
    private double total_energy;
    private double total_distance;
    private VehicleClass vehClass;
    private DriverType driver;
    private double effFactor;
    
    // IP
    public IloIntVar x;
    
    // MCKS
    public double efficiency;
    /**
     * Instantiates a vehicle assigning an {@code id} to it.
     * @param id Uniquely identifies the vehicle in the network.
     */
    public Vehicle(int id)
    {
        this(id, Wallet.EMPTY, VehicleClass.icv, DriverType.AV);
    }
    /**
     * Instantiates a vehicle assigning it an {@code id}, a {@link VehicleClass}, 
     * and a {@link DriverType}.
     * @param id Uniquely identifies the vehicle in the network.
     * @param vehClass Class of the vehicle {ICV, BEV, etc.).
     * @param driver AV or HV.
     */
    public Vehicle(int id, VehicleClass vehClass, DriverType driver)
    {
        this(id, Wallet.EMPTY, vehClass, driver);
    }
    
    /**
     * Instantiates a vehicle with the given parameters
     * @param id Uniquely identifies the vehicle in the network.
     * @param wallet the {@link Wallet} used for bidding
     * @param vehClass Class of the vehicle {ICV, BEV, etc.).
     * @param driver AV or HV.
     */
    public Vehicle(int id, Wallet wallet, VehicleClass vehClass, DriverType driver)
    {
        this.vehClass = vehClass;
        this.id = id;
        this.driver = driver;
        this.wallet = wallet;

        exit_time = -1;
        
        arr_time = 0;
        
        //effFactor = .5 + Math.round(Simulator.rand.nextDouble());
        effFactor = 1.0;
        
        curr = null;
        path = new Path();
        incident = Incident.UNKNOWN;
    }
    
    /**
     * Converts the departure time to an assignment interval
     * @return the assignment interval of the vehicle
     */
    public int getAST()
    {
        return getDepTime() / DTASimulator.ast_duration;
    }
    
    public void setIncident(Incident i)
    {
        incident = i;
    }
    
    public Incident getIncident()
    {
        return incident;
    }
    
    /**
     * Returns whether this vehicle is transit
     * @return false
     */
    public boolean isTransit()
    {
        return false;
    }
    
    
    /**
     * Returns the origin {@link Node}
     * @return the origin {@link Node}
     */
    public abstract Node getOrigin();
    
    public abstract Node getDest();
    
    public abstract int getDepTime();
    
    public int hashCode()
    {
        return id;
    }
    
    
    /**
     * Returns the {@link VehicleClass} used for calculating energy consumption.
     * @return the {@link VehicleClass} 
     */
    public VehicleClass getVehClass()
    {
        return vehClass;
    }

    
    /**
     * Updates the exit time of this {@link Vehicle}.
     * @param etime the new exit time (s)
     */
    public void setExitTime(int etime)
    {
        this.exit_time = etime;
    }
    
    /**
     * Returns the value of time.
     * @return the value of time ($/hr)
     */
    public double getVOT()
    {
        return 1;
    }
    
    /**
     * Returns a type code for this vehicle
     * @return depends on subclass
     */
    public abstract int getType();
    
    /**
     * Sets the energy efficiency. This parameter scales energy consumption.
     * @param eff the new efficiency
     */
    public void setEfficiency(double eff)
    {
        effFactor = eff;
    }
    /**
     * Gets time at which the vehicle enters the network.
     * @return Returns the int time at which the vehicle enters the network.
     */
    public int getNetEnterTime()
    {
        return net_enter_time;
    }
    
    /**
     * Returns the energy efficiency parameter
     * @return the energy efficiency parameter
     */
    public double getEfficiency()
    {
        return effFactor;
    }
    
    /**
     * Returns the total toll paid
     * @return the total toll paid
     */
    public double getTotalToll()
    {
        return total_toll;
    }
    
    /**
     * Adds to the total toll paid
     * @param t the additional toll
     */
    public void addToll(double t)
    {
        total_toll += t;
    }
    /**
     * Sets the {@link DriverType} of the vehicle.
     * @param driver AV or HV.
     */
    public void setDriver(DriverType driver)
    {
        this.driver = driver;
    }
    /**
     * Gets the {@link DriverType} of the vehicle.
     * @return Returns {@link DriverType} of the vehicle.
     */
    public DriverType getDriver()
    {
        return driver;
    }
    
    public double getVMT()
    {
        return total_distance;
    }
    
    /**
     * Updates the position of the vehicle by setting the current cell as the 
     * previous cell.
     * @param curr Is the current {@link Cell} in which the vehicle is present.
     */
    public void updatePosition(Cell curr)
    {
        if(!curr.getLink().isCentroidConnector())
        {
            total_energy += calcEnergy();
            total_distance += curr.getLength();
        }
        
        if(cell_enter >= 0 && Simulator.fileout != null)
        {
            printCellRecord(curr_cell, cell_enter, Simulator.time);
        }
        
        prev_cell = curr_cell;
        prev_cell_time = Simulator.time - cell_enter;
        cell_enter = Simulator.time;
        curr_cell = curr;   
        
        
        
    }
    
    public int getDelayInCell()
    {
        return Simulator.time - cell_enter;
    }
    
    /**
     * Prints a cell record for the current {@link Cell} - the enter and exit times
     * @param curr the current {@link Cell}
     * @param enter the enter time to the {@link Cell} (s)
     * @param exit the exit time to the {@link Cell} (s)
     */
    public void printCellRecord(Cell curr, int enter, int exit)
    {
        Simulator.fileout.println(getId()+"\t"+curr.getLink().getId()+"\t"+enter+"\t"+exit);
    }
    
    
    
    /**
     * Calculates energy consumption
     * @return energy consumed during the last {@link Cell}
     */
    public double calcEnergy()
    {
        double prev_speed = prev_cell != null? prev_cell.getLength() / (prev_cell_time / 3600.0) : 0;
        double curr_cell_time = curr_cell != null? Simulator.time - cell_enter : 6;
        double speed = curr_cell != null? curr_cell.getLength() / (curr_cell_time / 3600.0) : 0;
        double accel = (speed - prev_speed) / curr_cell_time;
        double grade = curr_cell != null? curr_cell.getLink().getGrade() : 0;
        
        
        
        double energy = effFactor * vehClass.calcEnergy(curr_cell_time, speed, accel, grade);
        
        double dist = speed * (curr_cell_time/3600.0);
        


        return energy;
    }
    
    
    
    /**
     * Returns difference in energy if vehicle moves this timestep versus moving next timestep
     * @return Difference in energy consumption for both scenarios
     */
    public double calcDeltaEnergy()
    {
        double e1 = 0, e2 = 0;
        
        // current case
        double prev_speed = prev_cell != null? prev_cell.getLength() / (prev_cell_time / 3600.0) : 0;
        double curr_cell_time = Simulator.time + Network.dt - cell_enter;
        double speed = curr_cell != null? curr_cell.getLength() / (curr_cell_time / 3600.0) : 0;
        double accel = (speed - prev_speed) / curr_cell_time;
        double grade = curr_cell != null? curr_cell.getLink().getGrade() : 0;
        
        e1 += effFactor * vehClass.calcEnergy(curr_cell_time, speed, accel, grade);

        prev_speed = curr_cell != null? curr_cell.getLength() / (curr_cell_time / 3600.0) : 0;
        speed = getNextLink().isCentroidConnector()? 0 : ((CTMLink)getNextLink()).getFirstCellSpeed();
        curr_cell_time = getNextLink().isCentroidConnector()? Network.dt : (getNextLink().getFFSpeed() / speed) * Network.dt;
        accel = (speed - prev_speed) / curr_cell_time;
        grade = getNextLink().getGrade();
        
        e1 += effFactor * vehClass.calcEnergy(curr_cell_time, speed, accel, grade);

        
        // delayed case
        prev_speed = prev_cell != null? prev_cell.getLength() / (prev_cell_time / 3600.0) : 0;
        curr_cell_time = Simulator.time - cell_enter + 2*Network.dt;
        speed = curr_cell != null? curr_cell.getLength() / (curr_cell_time / 3600.0) : 0;
        accel = (speed - prev_speed) / curr_cell_time;
        grade = curr_cell != null? curr_cell.getLink().getGrade() : 0;
        
        e2 += effFactor * vehClass.calcEnergy(curr_cell_time, speed, accel, grade);
        
        prev_speed = curr_cell != null? curr_cell.getLength() / (curr_cell_time / 3600.0) : 0;
        speed = getNextLink().isCentroidConnector()? 0 : ((CTMLink)getNextLink()).getFirstCellSpeed();
        curr_cell_time = getNextLink().isCentroidConnector()? Network.dt : (getNextLink().getFFSpeed() / speed) * Network.dt;
        accel = (speed - prev_speed) / curr_cell_time;
        grade = getNextLink().getGrade();
        
        e2 += effFactor * vehClass.calcEnergy(curr_cell_time, speed, accel, grade);
        

        return e2-e1;
    }
    
    /**
     * Returns the total energy consumption for this {@link Vehicle}'s trip
     * @return the total energy consumption for this {@link Vehicle}'s trip
     */
    public double getTotalEnergy()
    {
        return total_energy;
    }
    
    /**
     * Calculates the miles per gallon based on distance traveled
     * @return miles per gallon
     */
    public double getMPG()
    {
        return total_distance / (total_energy / VehicleClass.E_PER_GALLON);
    }
    
    /**
     * Called when this {@link Vehicle} is ready to enter the network
     */
    public void entered()
    {
        net_enter_time = Simulator.time;
        routeChoice.activate();
    }
    
    /**
     * Returns the id
     * @return the id
     */
    public String toString()
    {
        return ""+id;
    }
    /**
     * Records the exit time of the vehicle from a link and updates 
     * {@code path_idx}, which is the number of links traversed on the route.
     */
    public void exited()
    {
        exit_time = Simulator.time;

        /*
        if(Simulator.isRecording())
        {
            getPrevLink().exit[Simulator.time / Simulator.ast_duration]++;
        }
        */ 
        
        routeChoice.exited();
        curr = null;
    }
    /**
     * Resets the vehicle properties.
     */
    public void reset()
    {
        curr = null;
        wallet.reset();
        exit_time = -1;
        time_waiting = 0;
        
        if(routeChoice != null)
        {
            routeChoice.reset();
        }
        cell_enter = 0;
        prev_cell_time = 0;
        prev_cell = null;
        curr_cell = null;
        
        total_energy = 0;
        prev_cell_time = -1;
        prev_cell = null;
        cell_enter = -1;
        curr_cell = null;
        
        net_enter_time = -1;
    }
    /**
     * Returns the number of links left to be traversed on the route of the vehicle.
     * @return Returns an integer indicating #remaining links to  be traversed 
     * on the vehicle route.
     */
    
    public int getNumRemainingLinks()
    {
        if(routeChoice instanceof FixedPath)
        {
            return ((FixedPath)routeChoice).getNumRemainingLinks();
        }
        else
        {
            return 1;
        }
    }
    
    
    /**
     * Update the path of this {@link Vehicle}
     * @param p the new {@link Path}
     */
    public void setPath(Path p)
    {
        routeChoice = new FixedPath(p);
        path = p;
    }
    
    public void setRouteChoice(RouteChoice r)
    {
        routeChoice = r;
    }
    

    /**
     * Compare to another vehicle: order by ids.
     * @param rhs the vehicle to be compared to
     * @return order by id
     */
    public int compareTo(Vehicle rhs)
    {
        int dtime1 = getDepTime();
        int dtime2 = rhs.getDepTime();
        
        
        if(dtime1 != dtime2)
        {
            return dtime1 - dtime2;
        }
        else
        {
            return id - rhs.id;
        }
    }
    /**
     * Gets the exit time of the vehicle if the vehicle has reached its 
     * destination, otherwise gets the duration of the simulation run. 
     * @return Returns the exit time or the simulation duration.
     */
    public int getExitTime()
    {
        if(exit_time < 0)
        {
            return Simulator.duration;
        }
        return exit_time;
    }
    /**
     * Returns true if the vehicle has exited the network during the duration of 
     * the simulation.
     * @return Returns {@code true} if the vehicle has exited the network during the duration of 
     * the simulation, {@code false} otherwise.
     */
    public boolean isExited()
    {
        return exit_time > 0 && exit_time < Simulator.duration;
    }
    /**
     * Gets the travel time if the vehicle has exited the network and the time 
     * spent in the network if the vehicle is still in the network.
     * @return Returns the travel time or the time spent in the network if the 
     * vehicle is still in the network.
     */
    public int getTT()
    {
        return getExitTime() - getNetEnterTime();
    }
    /**
     * Returns the {@code id} of the vehicle.
     * @return Returns an integer representing the {@code id} of the vehicle.
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Return the path.
     * @return the path
     */
    
    public Path getPath()
    {
        return path;
    }
    
    public RouteChoice getRouteChoice()
    {
        return routeChoice;
    }
    
    
    /**
     * Gets the next link in the route of the vehicle.
     * @return Returns the next {@link Link} in the route of the vehicle, if 
     * there exists one otherwise returns {@code null}.
     */
    public Link getNextLink()
    {
        if(curr == null)
        {
            try
            {
                return routeChoice.getFirstLink(getOrigin());
            }
            catch(Exception ex)
            {
                System.out.println(Simulator.time);
                System.out.println("error id="+getId());
                System.out.println("curr link="+getCurrLink());
                throw ex;
            }
        }
        else
        {
            return routeChoice.getNextLink(curr, incident);
        }
    }
    
    /**
     * Gets the previous link in the route of the vehicle.
     * @return Returns the previous {@link Link} in the route of the vehicle, if 
     * there exists one otherwise returns {@code null}.
     */
    public Link getCurrLink()
    {
        return curr;
    }
    
    /**
     * Returns the incoming link when this {@link Vehicle} reaches an intersection.
     * @return {@link Vehicle#getCurrLink()}
     */
    public Link getPrevLink()
    {
        return getCurrLink();
    }
    
    /**
     * Called when this {@link Vehicle} enters a new {@link Link}. 
     * This method updates the waiting time, reservation time, and prints cell records if necessary.
     * @param l the {@link Link} entered
     */
    public void enteredLink(Link l)
    {
        curr = l;
        
        if(!(routeChoice instanceof FixedPath))
        {
            path.add(l);
        }
        
        Link i = getPrevLink();

        if(Simulator.active instanceof DUERSimulator)
        {
            DUERSimulator sim = (DUERSimulator)Simulator.active;
            Incident actual = sim.getIncident();

            if(actual != incident)
            {
                VMS vms = l.getVMS();
                if(Math.random() < vms.getProbOfInformation(actual))
                {
                    incident = actual;
                }
                else if(sim.isObservable(l, actual, Simulator.time))
                {
                    incident = actual;
                }
            }
        }
        
        routeChoice.enteredLink(l);
        

        time_waiting += Simulator.time + Network.dt - arr_time;
        
        arr_time = -1;
        enter_time = Simulator.time;
        
        reservation_time = -1;
        
        addToll(l.getToll(Simulator.time));
        
        if(Simulator.vat != null)
        {
            Simulator.vat.println(getId()+"\t"+l+"\t"+Simulator.time);
        }
        if(curr_cell != null && l.isCentroidConnector() && Simulator.fileout != null)
        {
            printCellRecord(curr_cell, cell_enter, Simulator.time);
        }
    }
    

    /**
     * Returns the total waiting time
     * @return the total waiting time
     */
    public int getTimeWaiting()
    {
        return time_waiting;
    }
    
    /**
     * Returns the total time spent traveling
     * @return the total time spent traveling 
     */
    public int getTimeTraveling()
    {
        return getTT() - getTimeWaiting();
    }
    
    /**
     * Returns the {@link Wallet} used for bidding
     * @return the {@link Wallet} used for bidding
     */
    public Wallet getWallet()
    {
        return wallet;
    }
    
    
}
