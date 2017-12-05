/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.ReadNetwork;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.type.Type;
import avdta.vehicle.Vehicle;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.DriverType;


/**
 * This class represents a centroid, and is a type of {@link Node}. 
 * This class also adds utilities used by the four-step planning model, including the productions, attractions, parking fees, and preferred arrival time.
 * All {@link PersonalVehicle}s enter and exit at {@link Zone}s.
 * @author Michael
 */
public class Zone extends Node
{
    private double productions, attractions, parkingFee, preferredArrivalTime;
    
    public double mu, C;
    
    // this is for when origin/destination zones are distinct
    private Zone linkedZone;
    
    /**
     * Constructs this {@link Zone} with the given id and a {@link Location} of (0, 0).
     * @param id the id of this {@link Zone}
     */
    public Zone(int id)
    {
        this(id, new Location(0, 0));
    }
    
    /**
     * Constructs this {@link Zone} with the given id and {@link Location}.
     * @param id the id of this {@link Zone}
     * @param loc the {@link Location} of this {@link Zone}
     */
    public Zone(int id, Location loc)
    {
        this(id, loc, 0, 0);
    }
    
    /**
     * Constructs this {@link Zone} with the given id, {@link Location}, and productions and attractions values.
     * 
     * @param id the id of this {@link Zone}
     * @param loc the {@link Location} of this {@link Zone}
     * @param productions the number of trips departing from this {@link Zone}
     * @param attractions the number of trips arriving at this {@link Zone}
     */
    public Zone(int id, Location loc, double productions, double attractions)
    {
        super(id, loc);
        
        this.productions = productions;
        this.attractions = attractions;
        
        parkingFee = 5;
    }
    
    public boolean isOrigin()
    {
        return getId() > 0;
    }
    
    public boolean isDest()
    {
        return getId() < 0;
    }
    
    /**
     * Returns null because a {@link Zone} is never a {@link Signalized}
     * @return null
     */
    public Signalized getSignal()
    {
        return null;
    }
    
    /**
     * Links this {@link Zone} to another. 
     * This is typically used when separate {@link Zone} objects are created for origins and destinations.
     * Equivalent origins and destinations need to be linked to identify repositioning trips.
     * Note that this does not call {@link Zone#setLinkedZone(avdta.network.node.Zone)} for {@code z}.
     * @param z the linked zone
     */
    public void setLinkedZone(Zone z)
    {
        linkedZone = z;
    }
    
    /**
     * Returns the linked zone.
     * This is typically used when separate {@link Zone} objects are created for origins and destinations.
     * Equivalent origins and destinations need to be linked to identify repositioning trips.
     * 
     * @return the linked zone
     */
    public Zone getLinkedZone()
    {
        return linkedZone;
    }
    
    /**
     * Returns whether this is a centroid, which it is.
     * @return true
     */
    public boolean isZone()
    {
        return true;
    }
    
    /**
     * Returns whether vehicles can move across this zone, which is always false. 
     * Vehicles can only enter or exit at {@link Zone}s.
     * 
     * @param i the incoming {@link Link}
     * @param j the outgoing {@link Link}
     * @param driver the {@link DriverType}
     * @return false
     */
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return false;
    }

    /**
     * Updates the preferred arrival time used in the four-step model with endogenous departure time choice.
     * @param a the new preferred arrival time
     */
    public void setPreferredArrivalTime(double a)
    {
        preferredArrivalTime = a;
    }
    
    /**
     * Returns the preferred arrival time used in the four-step model with endogenous departure time choice.
     * @return the  preferred arrival time
     */
    public double getPreferredArrivalTime()
    {
        return preferredArrivalTime;
    }
    
    /**
     * Updates the productions. This is used when setting the productions based on DTA demand.
     * @param p the productions to be added.
     */
    public void addProductions(double p)
    {
        productions += p;
    }
    
    /**
     * Updates the attractions. This is used when setting the attractions based on DTA demand.
     * @param a the attractions to be added
     */
    public void addAttractions(double a)
    {
        attractions += a;
    }
    
    /**
     * Returns the cost of parking at this {@link Zone}
     * @return the cost of parking at this {@link Zone}
     */
    public double getParkingCost()
    {
        return parkingFee;
    }
    
    /**
     * Returns the productions, which is the number of trips departing from this {@link Zone}.
     * @return the productions for this {@link Zone}
     */
    public double getProductions()
    {
        return productions;
    }
    
    /**
     * Returns the attractions, which is the number of trips arriving at this {@link Zone}.
     * @return the attractions for this {@link Zone}
     */
    public double getAttractions()
    {
        return attractions;
    }
    
    /**
     * Updates the productions.
     * @param p the new number of productions
     */
    public void setProductions(double p)
    {
        this.productions = p;
    }
    
    /**
     * Updates the attractions.
     * @param a the new number of attractions
     */
    public void setAttractions(double a)
    {
        this.attractions = a;
    }
    
    /**
     * Returns the type code of {@link Zone}.
     * @return {@link ReadNetwork#CENTROID}
     */
    public Type getType()
    {
        return ReadNetwork.CENTROID;
    }
    
    /**
     * Updates the cost of parking at this {@link Zone}
     * @param f the new cost of parking
     */
    public void setParkingFee(double f)
    {
        parkingFee = f;
    }
    
    /**
     * Returns whether this {@link Node} uses {@link ConflictRegion}s
     * @return false
     */
    public boolean hasConflictRegions()
    {
        return false;
    }
    
    /**
     * Resets this {@link Zone} to restart simulation. Nothing needs to be done.
     */
    public void reset(){}
    
    /**
     * Initialization this {@link Zone} after all data is read. Nothing needs to be done.
     */
    public void initialize(){}
    
    /**
     * Executes one time step of simulation. {@link Vehicle}s reaching this {@link Zone} exit the network.
     * @return the number of exiting vehicles
     */
    public int step()
    {
        int output = 0;
        
        for(Link l : getIncoming())
        {
            for(Vehicle v : l.getSendingFlow())
            {
                v.exited();
                l.removeVehicle(v);
                
                output++;
            }
        }

        return output;
    }
}
