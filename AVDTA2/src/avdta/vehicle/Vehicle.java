/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

//import ilog.concert.IloIntVar;
import avdta.network.Network;
import avdta.vehicle.fuel.VehicleClass;
import avdta.network.link.CTMLink;
import avdta.network.link.cell.Cell;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.Path;
import avdta.network.Simulator;
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
    public static final int CAR = 1;
    public static final int AV = 10;
    public static final int TAXI = 100;
    public static final int BUS = 501;
    
    public static final double vehicle_length = 19;
    
    private static final Random rand = new Random(9000);
    
    private Wallet wallet;
    private int id;

    private Path route;
    
    public int path_idx;
    private int exit_time, net_enter_time;
    
    private double total_toll;
    private double vot;
    
    public int arr_time, enter_time, reservation_time; // arrival time at intersection
    public double pressure;
    
    public double bid;
    
    private int time_waiting;
    
    // energy
    private int prev_cell_time;
    private Cell prev_cell;
    public int cell_enter;
    private Cell curr_cell;
    private double total_energy;
    private VehicleClass vehClass;
    private DriverType driver;
    
    // IP
    //protected IloIntVar x;
    
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
    
    public Vehicle(int id, Wallet wallet, VehicleClass vehClass, DriverType driver)
    {
        this.vehClass = vehClass;
        this.id = id;
        this.driver = driver;
        this.wallet = wallet;

        exit_time = -1;
        
        path_idx = -1;
        arr_time = 0;
        
        effFactor = .5 + Math.round(rand.nextDouble());
    }
    
    public boolean isTransit()
    {
        return false;
    }
    
    public VehicleClass getVehicleClass()
    {
        return vehClass;
    }

    public void setVOT(double vot)
    {
        this.vot = vot;
    }
    
    public double getVOT()
    {
        return vot;
    }
    public abstract int getType();
    
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
    
    public double getEfficiency()
    {
        return effFactor;
    }
    
    public double getTotalToll()
    {
        return total_toll;
    }
    
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
    
    public void printCellRecord(Cell curr, int enter, int exit)
    {
        Simulator.fileout.println(getId()+"\t"+curr.getLink().getId()+"\t"+enter+"\t"+exit);
    }
    
    double effFactor;
    
    public double calcEnergy()
    {
        double prev_speed = prev_cell != null? prev_cell.getLength() / (prev_cell_time / 3600.0) : 0;
        double curr_cell_time = curr_cell != null? Simulator.time - cell_enter : 6;
        double speed = curr_cell != null? curr_cell.getLength() / (curr_cell_time / 3600.0) : 0;
        double accel = (speed - prev_speed) / curr_cell_time;
        double grade = curr_cell != null? curr_cell.getLink().getGrade() : 0;

        return effFactor * vehClass.calcEnergy(curr_cell_time, speed, accel, grade);
    }
    
     // return difference in energy if vehicle moves this timestep vs. moving next timestep
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
    
    public VehicleClass getVehClass()
    {
        return vehClass;
    }
    
    public double getTotalEnergy()
    {
        return total_energy;
    }
    
    public double getMPG()
    {
        return route.getLength() / (total_energy / VehicleClass.E_PER_GALLON);
    }
    
    public void entered()
    {

    }
    
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
        
        path_idx++;
    }
    /**
     * Resets the vehicle properties.
     */
    public void reset()
    {
        wallet.reset();
        exit_time = -1;
        time_waiting = 0;
        path_idx = -1;
        
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
    public int numRemainingLinks()
    {
        return route.size() - path_idx;
    }
    
    public void setPath(Path p)
    {
        route = p;
        path_idx = -1;
        
    }
    
    
    
    public int compareTo(Vehicle rhs)
    {
        return id - rhs.id;
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
    
    public Path getPath()
    {
        return route;
    }
    /**
     * Gets the next link in the route of the vehicle.
     * @return Returns the next {@link Link} in the route of the vehicle, if 
     * there exists one otherwise returns {@code null}.
     */
    public Link getNextLink()
    {
        if(path_idx < route.size() - 1)
        {
            return route.get(path_idx+1);
        }
        else
        {
            return null;
        }
    }
    /**
     * Gets the previous link in the route of the vehicle.
     * @return Returns the previous {@link Link} in the route of the vehicle, if 
     * there exists one otherwise returns {@code null}.
     */
    public Link getPrevLink()
    {
        if(path_idx >= 0 && path_idx < route.size())
        {
            return route.get(path_idx);
        }
        else
        {
            return null;
        }
    }
    
    public void enteredLink(Link l)
    {
        
        Link i = getPrevLink();


        if(i == null)
        {
            net_enter_time = Simulator.time;
        }
        
        
        path_idx++;
        
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
    

    public int getTimeWaiting()
    {
        return time_waiting;
    }
    
    public int getTimeTraveling()
    {
        return getTT() - getTimeWaiting();
    }
    
    public Wallet getWallet()
    {
        return wallet;
    }
    
    public Node getOrigin()
    {
        return route.get(0).getSource();
    }
    
    public Node getDest()
    {
        return route.get(route.size()-1).getDest();
    }
}
