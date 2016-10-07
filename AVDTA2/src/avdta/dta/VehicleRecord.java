/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import java.util.Scanner;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;

/**
 * This represents an entry in the demand file, and is used to easily manipulate data associated with individual vehicle trips.
 * @author Michael
 */
public class VehicleRecord implements Comparable<VehicleRecord>
{
    private int id, dtime, origin, dest, type;
    
    /**
     * Constructs this {@link VehicleRecord} from a line of input data
     * @param line the input data
     */
    public VehicleRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        
        id = chopper.nextInt();
        type = chopper.nextInt();
        origin = chopper.nextInt();
        dest = chopper.nextInt();
        dtime = chopper.nextInt();
    }
    
    /**
     * Constructs this {@link VehicleRecord} with the given parameters.
     * @param id the id of the {@link Vehicle}
     * @param type the type code of the {@link Vehicle}
     * @param origin the id of the origin {@link Node}
     * @param dest the id of the  destination {@link Node}
     * @param dtime the departure time (s)
     */
    public VehicleRecord(int id, int type, int origin, int dest, int dtime)
    {
        this.id = id;
        this.dtime = dtime;
        this.origin = origin;
        this.dest = dest;
        this.type = type;
    }
    
    /**
     * Returns a {@link String} form that may be written to the data file.
     * @return the {@link String} form
     */
    public String toString()
    {
        return id+"\t"+type+"\t"+origin+"\t"+dest+"\t"+dtime;
    }
    
    /**
     * Orders {@link VehicleRecord}s by departure time, then id.
     * @param rhs the {@link VehicleRecord} to compare against
     * @return orders {@link VehicleRecord}s by departure time, then id
     */
    public int compareTo(VehicleRecord rhs)
    {
        if(dtime != rhs.dtime)
        {
            return dtime - rhs.dtime;
        }
        else
        {
            return id - rhs.id;
        }
    }
    
    /**
     * Returns the origin id
     * @return the origin id
     */
    public int getOrigin()
    {
        return origin;
    }
    
    /**
     * Returns the destination id
     * @return the destination id
     */
    public int getDestination()
    {
        return dest;
    }
    
    /**
     * Returns the departure time
     * @return the departure time (s)
     */
    public int getDepTime()
    {
        return dtime;
    }
    
    /**
     * Returns the type code
     * @return the type code
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * Returns the id
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Updates the id
     * @param id the new id
     */
    public void setId(int id)
    {
        this.id = id;
    }
    
    /**
     * Updates the origin node id
     * @param origin the new origin node id
     */
    public void setOrigin(int origin)
    {
        this.origin = origin;
    }
    
    /**
     * Updates the destination node id
     * @param dest the new destination node id
     */
    public void setDestination(int dest)
    {
        this.dest = dest;
    }
    
    /**
     * Updates the departure time
     * @param dtime the new departure time (s)
     */
    public void setDepTime(int dtime)
    {
        this.dtime = dtime;
    }
}
