/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.TBR;
import avdta.network.type.Type;
import avdta.vehicle.EmergencyVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.route.FixedPath;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class EmergencyPolicy extends FCFSPolicy
{
    /**
    * Initializes {@link Vehicle} reservation time the first time a {@link Vehicle} is initialized
    * @param node the node to be initialized at
    * @param v the vehicle to initialize
    */
    public void initialize(TBR node, Vehicle v)
    {
        super.initialize(node, v);

        
        // prioritize vehicles on the path of an emergency vehicle
        // put low priority on vehicles trying to move on the path of an emergency vehicle
        
        Set<EmergencyVehicle> emergencies = Simulator.active.getEmergencyVehicles();
        
        if(emergencies.size() == 0)
        {
            return;
        }
        
        if(v instanceof EmergencyVehicle)
        {
            v.reservation_time = 0;
            return;
        }
        
        boolean onPath = false;
        boolean intoPath = false;
        
        Link curr = v.getCurrLink();
        Link next = v.getNextLink();
        
        for(EmergencyVehicle ev : emergencies)
        {
            FixedPath path = (FixedPath)ev.getRouteChoice();
            if(path.getPath().indexOf(curr) >= path.getPathIndex())
            {
                onPath = true;
                break;
            }
            else if(path.getPath().indexOf(next) >= path.getPathIndex())
            {
                intoPath = true;
            }
        }
        
        if(onPath)
        {
            v.reservation_time = 0;
        }
        else if(intoPath)
        {
            v.reservation_time = Integer.MAX_VALUE;
        }
        
    }
    
    public Type getType()
    {
        return ReadNetwork.EMERGENCY_FIRST;
    }
}
