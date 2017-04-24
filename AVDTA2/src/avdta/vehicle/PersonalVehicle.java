/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.vehicle.fuel.VehicleClass;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import avdta.vehicle.wallet.Wallet;
import avdta.vehicle.wallet.EmptyWallet;
import avdta.vehicle.fuel.ICV;

/**
 * This represents a privately-owned vehicle. 
 * {@link PersonalVehicle}s depart at a predetermined time and travel from an origin {@link Zone} to a destination {@link Zone}.
 * @author Michael
 */
public class PersonalVehicle extends Vehicle 
{
    private Node origin, dest;
    private int dep_time;
    

    /**
     * Constructs the {@link PersonalVehicle} with the given parameters.
     * The {@link VehicleClass} is set to {@link ICV}, and the {@link DriverType} is set to {@link DriverType#AV}.
     * @param id the id of the vehicle
     * @param origin the origin {@link Node}
     * @param dest the destination {@link Node}
     * @param dtime the departure time
     * @param vot the value of time
     * @param wallet the wallet used for bidding at auctions
     */
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, Wallet wallet)
    {
        this(id, origin, dest, dtime, vot, wallet, VehicleClass.icv, DriverType.AV);
    }
    
    /**
     * Constructs the {@link PersonalVehicle} with the given parameters.
     * The {@link VehicleClass} is set to {@link ICV}, and the {@link DriverType} is set to {@link DriverType#AV}.
     * The value of time is 1, and an {@link EmptyWallet} is used.
     * @param id the id of the vehicle
     * @param origin the origin {@link Node}
     * @param dest the destination {@link Node}
     * @param dtime the departure time
     */
    public PersonalVehicle(int id, Node origin, Node dest, int dtime)
    {
        this(id, origin, dest, dtime, 1, Wallet.EMPTY, VehicleClass.icv, DriverType.AV);
    }
    
    /**
     * Constructs the {@link PersonalVehicle} with the given parameters.
     * The {@link VehicleClass} is set to {@link ICV}.
     * The value of time is 1, and an {@link EmptyWallet} is used.
     * @param id the id of the vehicle
     * @param origin the origin {@link Node}
     * @param dest the destination {@link Node}
     * @param dtime the departure time
     * @param driver the driver
     */
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, DriverType driver)
    {
        this(id, origin, dest, dtime, 1, Wallet.EMPTY, VehicleClass.icv, driver);
    }
    
     /**
     * Constructs the {@link PersonalVehicle} with the given parameters.
     * The value of time is 1, and an {@link EmptyWallet} is used.
     * @param id the id of the vehicle
     * @param origin the origin {@link Node}
     * @param dest the destination {@link Node}
     * @param dtime the departure time
     * @param vehClass the {@link VehicleClass} used for calculating energy consumption
     * @param driver the driver
     */
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, VehicleClass vehClass, DriverType driver)
    {
        this(id, origin, dest, dtime, 1, Wallet.EMPTY, vehClass, driver);
    }
    
    /**
     * Constructs the {@link PersonalVehicle} with the given parameters.
     * An {@link EmptyWallet} is used.
     * @param id the id of the vehicle
     * @param origin the origin {@link Node}
     * @param dest the destination {@link Node}
     * @param dtime the departure time
     * @param vot the value of time
     * @param vehClass the {@link VehicleClass} used for calculating energy consumption
     * @param driver the driver
     */
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, VehicleClass vehClass, DriverType driver)
    {
        this(id, origin, dest, dtime, vot, Wallet.EMPTY, vehClass, driver);
    }
    
    /**
     * Constructs the {@link PersonalVehicle} with the given parameters.
     * @param id the id of the vehicle
     * @param origin the origin {@link Node}
     * @param dest the destination {@link Node}
     * @param dtime the departure time
     * @param vehClass the {@link VehicleClass} used for calculating energy consumption
     * @param driver the driver
     * @param vot the value of time
     * @param wallet the wallet used for bidding at auctions
     */
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, Wallet wallet, VehicleClass vehClass, DriverType driver)
    {
        super(id, wallet, vehClass, driver);
        
        this.origin = origin;
        this.dest = dest;
        this.dep_time = dtime;

        arr_time = 0;
        setVOT(vot);

    }
    
    /**
     * Constructs the {@link PersonalVehicle} with the given parameters.
     * @param id the id of the vehicle
     * @param origin the origin {@link Node}
     * @param dest the destination {@link Node}
     * @param dtime the departure time
     * @param vehClass the {@link VehicleClass} used for calculating energy consumption
     * @param driver the driver
     * @param wallet the wallet used for bidding at auctions
     * @param vot the value of time
     * @param path the vehicle path
     */
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, Wallet wallet, Path path, VehicleClass vehClass, DriverType driver)
    {
        this(id, origin, dest, dtime, vot, wallet, vehClass, driver);
        setPath(path);
    }
    
    /**
     * Converts the departure time to an assignment interval
     * @return the assignment interval of the vehicle
     */
    public int getAST()
    {
        return dep_time / DTASimulator.ast_duration;
    }
    
    /**
     * Returns the vehicle type code
     * @return {@link ReadDTANetwork#DA_VEHICLE}+ the driver type + the vehicle class type
     */
    public int getType()
    {
        return ReadDTANetwork.DA_VEHICLE + getDriver().getType() + getVehClass().getType();
    }
    
    

    /**
     * Updates the vehicle path. Also checks that the path is valid.
     * @param p the new path
     */
    public void setPath(Path p)
    {
        if(checkInvalidPath(p))
        {
            throw new RuntimeException("Invalid path for "+getId()+" "+p+" "+origin+" "+dest);
        }
        
        super.setPath(p);

    }
    
    /**
     * Checks whether the path travels from the origin to the destination
     * @param p the new path
     * @return whether the path is valid for this vehicle
     */
    public boolean checkInvalidPath(Path p)
    {
        return p == null || p.size() == 0 || p.getOrigin() != origin || p.getDest() != dest;
    }
    
    /**
     * Orders this vehicle against others based on departure time, then id
     * @param v the vehicle to be compared against
     * @return orders this vehicle against others based on departure time, then id
     */
    public int compareTo(Vehicle v)
    {
        if(v instanceof PersonalVehicle)
        {
            PersonalVehicle rhs = (PersonalVehicle)v;
            
            if(dep_time != rhs.dep_time)
            {
                return dep_time - rhs.dep_time;
            }
            else
            {
                return super.compareTo(v);
            }         
        }
        else
        {
            return super.compareTo(v);
        }

    }
    
    /**
     * Returns the departure time
     * @return the departure time
     */
    public int getDepTime()
    {
        return dep_time;
    }
    
    /**
     * Returns the experienced travel time
     * @return the experienced travel time (s)
     */
    public int getTT()
    {
        return getExitTime() - getDepTime();
    }

    /**
     * Returns the origin {@link Node}
     * @return the origin {@link Node}
     */
    public Node getOrigin()
    {
        return origin;
    }
    
    /**
     * Returns the destination {@link Node}
     * @return the destination {@link Node}
     */
    public Node getDest()
    {
        return dest;
    }
    
}
