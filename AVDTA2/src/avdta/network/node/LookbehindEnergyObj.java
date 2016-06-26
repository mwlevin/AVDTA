/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import avdta.network.node.Intersection;
import avdta.network.node.EnergyObj;
import avdta.network.node.TBR;
import java.util.List;

/**
 *
 * @author ut
 */
public class LookbehindEnergyObj extends EnergyObj
{
    // add instance variables here
    public LookbehindEnergyObj()
    {
        
    }
    
    public boolean isMinimize()
    {
        return false;
    }
    
    // coefficient for vehicle v in objective function
    
    public void initialize(Node n)
    {
        
        for(Link i : n.getIncoming())
        {
            double output = 0;

            List<Vehicle> sendingFlow = i.getSendingFlow();

            int k = 0;
            for(Vehicle v2 : sendingFlow)
            {
                output += super.value(v2, (TBR)((Intersection)n).getControl()) * scale(k);
                k++;
            }

            i.pressure = output;
        }
        
    }
    public double value(Vehicle v, TBR n)
    {
        if(v.getNextLink().isCentroidConnector())
        {
            return 1000;
        }
        
        return v.getPrevLink().pressure;
        
    }
    
    public double scale(int position)
    {
        return 1;
    }
}