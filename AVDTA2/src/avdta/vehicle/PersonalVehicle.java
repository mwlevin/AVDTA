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
import avdta.traveler.Traveler;
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
    private Traveler traveler;
    

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
    public PersonalVehicle(Traveler traveler, Wallet wallet)
    {
        this(traveler, wallet, VehicleClass.icv, DriverType.AV);
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
    public PersonalVehicle(Traveler traveler)
    {
        this(traveler, Wallet.EMPTY, VehicleClass.icv, DriverType.AV);
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
    public PersonalVehicle(Traveler traveler, DriverType driver)
    {
        this(traveler, Wallet.EMPTY, VehicleClass.icv, driver);
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
    public PersonalVehicle(Traveler traveler, VehicleClass vehClass, DriverType driver)
    {
        this(traveler, Wallet.EMPTY, vehClass, driver);
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
    public PersonalVehicle(Traveler traveler, Wallet wallet, VehicleClass vehClass, DriverType driver)
    {
        super(traveler.getId(), wallet, vehClass, driver);
        
        this.traveler = traveler;

        arr_time = 0;

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
    public PersonalVehicle(Traveler traveler, Wallet wallet, Path path, VehicleClass vehClass, DriverType driver)
    {
        this(traveler, wallet, vehClass, driver);
        setPath(path);
    }
    
    /**
     * Returns the value of time.
     * @return the value of time ($/hr)
     */
    public double getVOT()
    {
        return traveler.getVOT();
    }
    
    /**
     * Converts the departure time to an assignment interval
     * @return the assignment interval of the vehicle
     */
    public int getAST()
    {
        return traveler.getDepTime() / DTASimulator.ast_duration;
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
     * Checks whether the path travels from the origin to the destination
     * @param p the new path
     * @return whether the path is valid for this vehicle
     */
    public boolean checkInvalidPath(Path p)
    {
        return p == null || p.size() == 0 || p.getOrigin() != getOrigin() || p.getDest() != getDest();
    }

    
    /**
     * Returns the departure time
     * @return the departure time
     */
    public int getDepTime()
    {
        return traveler.getDepTime();
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
        return traveler.getOrigin();
    }
    
    /**
     * Returns the destination {@link Node}
     * @return the destination {@link Node}
     */
    public Node getDest()
    {
        return traveler.getDest();
    }
    
}
