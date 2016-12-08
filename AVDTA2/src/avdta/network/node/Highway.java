/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.ReadNetwork;
import avdta.network.link.Link;
import avdta.network.node.policy.FIFOPolicy;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author micha
 */
public class Highway extends IntersectionControl
{
    private IntersectionPolicy policy;
    
    public Highway()
    {
        policy = new FIFOPolicy();
    }
    
    public void initialize(){}
    public void reset(){}
    public boolean canMove(Link i, Link j, DriverType driver)
    {
        return true;
    }
    
    public boolean hasConflictRegions()
    {
        return false;
    }
    
    public int getType()
    {
        return ReadNetwork.HIGHWAY;
    }
    
    public Signalized getSignal()
    {
        return null;
    }
    
    public int step()
    {
        Node node = getNode();
        
        policy.initialize(node);
        
        int exited = 0;
        
        int moved = 0;



        // initializations

        // update receiving flows
        for(Link l : node.getOutgoing())
        {
            l.R = l.getReceivingFlow();
            
        }

        Set<Vehicle> vehicles = new TreeSet<Vehicle>(policy);
        
        for(Link i : node.getIncoming())
        {
            List<Vehicle> S = i.getSendingFlow();
            for(Vehicle v : S)
            {
                vehicles.add(v);
            }
        }
        

        for(Vehicle v : vehicles)
        {
            Link i = v.getPrevLink();
            Link j = v.getNextLink();

            if(j == null)
            {
                i.removeVehicle(v);
                v.exited();
                exited++;
            }

            double equiv_flow = v.getDriver().getEquivFlow(i.getFFSpeed());


            double receivingFlow =  i.scaleReceivingFlow(v);

            if(j.R >= receivingFlow)
            {


                j.R -= receivingFlow;
                moved++;



                i.removeVehicle(v);
                j.addVehicle(v);

            }
            

        }
        
  
       
        
        return exited;
        
    }
}
