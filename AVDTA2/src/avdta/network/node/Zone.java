/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;

/**
 *
 * @author ut
 */
public class Zone extends Node
{
    private double productions, attractions, parkingFee, preferredArrivalTime;
    
    public double mu, C;
    
    // this is for when origin/destination zones are distinct
    private Zone linkedZone;
    public Zone(int id)
    {
        this(id, new Location(0, 0));
    }
    public Zone(int id, Location loc)
    {
        this(id, loc, 0, 0);
    }
    
    public Zone(int id, Location loc, double productions, double attractions)
    {
        super(id, loc);
        
        this.productions = productions;
        this.attractions = attractions;
        
        parkingFee = 5;
    }
    
    public void setLinkedZone(Zone z)
    {
        linkedZone = z;
    }
    
    public Zone getLinkedZone()
    {
        return linkedZone;
    }
    
    public boolean isZone()
    {
        return true;
    }
    
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return false;
    }

    public void setPreferredArrivalTime(double a)
    {
        preferredArrivalTime = a;
    }
    
    public double getPreferredArrivalTime()
    {
        return preferredArrivalTime;
    }
    
    public void addProductions(double p)
    {
        productions += p;
    }
    
    public void addAttractions(double a)
    {
        attractions += a;
    }
    
    public double getParkingCost()
    {
        return parkingFee;
    }
    
    public double getProductions()
    {
        return productions;
    }
    
    public double getAttractions()
    {
        return attractions;
    }
    
    public void setProductions(double p)
    {
        this.productions = p;
    }
    
    public void setAttractions(double a)
    {
        this.attractions = a;
    }
    
    public void setParkingFee(double f)
    {
        parkingFee = f;
    }
    
    public boolean hasConflictRegions()
    {
        return false;
    }
    
    public void reset(){}
    public void initialize(){}
    
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
