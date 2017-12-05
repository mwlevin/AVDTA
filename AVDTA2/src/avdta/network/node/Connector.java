/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.ReadNetwork;
import avdta.network.link.Link;
import avdta.network.type.Type;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.util.List;

/**
 *
 * @author ml26893
 */
public class Connector extends IntersectionControl
{
    public Type getType()
    {
        return ReadNetwork.CONNECTOR;
    }
    
    public void reset()
    {
        // nothing to do
    }
    
    public boolean hasConflictRegions()
    {
        return false;
    }
    
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return true;
    }
    
    public void initialize()
    {
        // nothing to do
    }
    
    public Signalized getSignal()
    {
        return null;
    }
    
    public int step()
    {
        int exited = 0;
        
        Node node = getNode();
        
        Link i = node.getIncoming().iterator().next();
        
        
        List<Vehicle> sending = i.getSendingFlow();

        for(Vehicle v : sending)
        {
            Link j = v.getNextLink();
            if(j == null)
            {
                i.removeVehicle(v);
                v.exited();
                exited++;
            }
            else
            {

                double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());
                
                if(j.R >= equiv_flow)
                {
                    i.removeVehicle(v);
                    j.addVehicle(v);
                    
                    j.R -= equiv_flow;
                }
            }
        }
        
        return exited;
    }
}
