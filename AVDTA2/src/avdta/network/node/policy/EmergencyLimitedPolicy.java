/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.TBR;
import avdta.vehicle.EmergencyVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.route.FixedPath;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class EmergencyLimitedPolicy extends FCFSPolicy
{
    /**
    * Initializes {@link Vehicle} reservation time the first time a {@link Vehicle} is initialized
    * @param node the node to be initialized at
    * @param v the vehicle to initialize
    */
    public void initialize(TBR node, Vehicle v)
    {
        super.initialize(node, v);


        
        if(v instanceof EmergencyVehicle)
        {
            v.reservation_time = 0;
            return;
        }
        
        
        
    }
}
