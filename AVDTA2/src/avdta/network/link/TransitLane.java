/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.network.type.Type;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;

/**
 * A standard {@link CTMLink} that is usable only by transit. {@link TransitLane}s have only 1 lane.
 * @author Michael
 */
public class TransitLane extends CTMLink
{
    /**
     * Constructs the link with the given parameters 
     * @param id the link id
     * @param source the source node
     * @param dest the destination node
     * @param capacity the capacity per lane (veh/hr)
     * @param ffspd the free flow speed (mi/hr)
     * @param wavespd the congested wave speed (mi/hr)
     * @param jamd the jam density (veh/mi)
     * @param length the length (mi)
     */
    public TransitLane(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length)
    {
        super(id, source, dest,capacity, ffspd, wavespd, jamd, length, 1);
    }
    
    /**
     * Only transit can use this link
     * @param driver the {@link DriverType} specifying the type of driver (human/autonomous) and whether it is transit
     * @return whether the driver can use this link
     */
    public boolean canUseLink(DriverType driver)
    {
        return driver.isTransit() && super.canUseLink(driver);
    }
    
    /**
     * Returns the type code of this link
     * @return {@link ReadNetwork#CTM}+{@link ReadNetwork#TRANSIT_LANE}
     */
    public Type getType()
    {
        return ReadNetwork.TRANSIT_LANE;
    }
    
    /**
     * Overrides because {@link TransitLane}s are part of a main link and do not have a {@link LinkRecord}
     * @return null
     */
    public LinkRecord getLinkRecord()
    {
        return null;
    }
    
    /**
     * Check whether the {@link Vehicle} is transit; if not an {@link RuntimeException} is thrown.
     * Otherwise this calls {@link CTMLink#addVehicle(avdta.vehicle.Vehicle)}.
     * @param v the {@link Vehicle} to be added
     */
    public void addVehicle(Vehicle v)
    {
        super.addVehicle(v);
        
        if(!v.isTransit())
        {
            throw new RuntimeException("Adding non-transit vehicle to transit lane");
        }

    }
    
}
