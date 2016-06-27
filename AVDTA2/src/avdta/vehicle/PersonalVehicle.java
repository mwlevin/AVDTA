/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.dta.DTASimulator;
import avdta.network.node.Node;
import avdta.network.Path;
import avdta.network.Simulator;
import avdta.vehicle.fuel.VehicleClass;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import avdta.vehicle.Wallet;
import static avdta.vehicle.Vehicle.CAR;

/**
 *
 * @author Michael
 */
public class PersonalVehicle extends Vehicle 
{
    private Node origin, dest;
    private int dep_time;
    

    public PersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, Wallet wallet)
    {
        this(id, origin, dest, dtime, vot, wallet, VehicleClass.icv, DriverType.AV);
    }
    
    public PersonalVehicle(int id, Node origin, Node dest, int dtime)
    {
        this(id, origin, dest, dtime, 1, Wallet.EMPTY, VehicleClass.icv, DriverType.AV);
    }
    
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, DriverType driver)
    {
        this(id, origin, dest, dtime, 1, Wallet.EMPTY, VehicleClass.icv, driver);
    }
    
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, VehicleClass vehClass, DriverType driver)
    {
        this(id, origin, dest, dtime, 1, Wallet.EMPTY, vehClass, driver);
    }
    
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, VehicleClass vehClass, DriverType driver)
    {
        this(id, origin, dest, dtime, vot, Wallet.EMPTY, vehClass, driver);
    }
    
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, Wallet wallet, VehicleClass vehClass, DriverType driver)
    {
        super(id, wallet, vehClass, driver);
        
        this.origin = origin;
        this.dest = dest;
        this.dep_time = dtime;

        arr_time = 0;
        setVOT(vot);
        
        effFactor = .5 + Math.round(Math.random());
    }
    
    public PersonalVehicle(int id, Node origin, Node dest, int dtime, double vot, Wallet wallet, Path path)
    {
        this(id, origin, dest, dtime, vot, wallet);
        setPath(path);
    }
    
    
    public int getAST()
    {
        return dep_time / DTASimulator.ast_duration;
    }
    
    public int getType()
    {
        return CAR;
    }
    
    

    
    public void setPath(Path p)
    {
        if(checkInvalidPath(p))
        {
            throw new RuntimeException("Invalid path for "+getId()+" "+p+" "+origin+" "+dest);
        }
        
        super.setPath(p);

    }
    
    public boolean checkInvalidPath(Path p)
    {
        return p == null || p.size() == 0 || p.getOrigin() != origin || p.getDest() != dest;
    }
    
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
    
    public int getDepTime()
    {
        return dep_time;
    }
    
    public int getTT()
    {
        return getExitTime() - getDepTime();
    }

    
    public Node getOrigin()
    {
        return origin;
    }
    
    public Node getDest()
    {
        return dest;
    }
    
}
