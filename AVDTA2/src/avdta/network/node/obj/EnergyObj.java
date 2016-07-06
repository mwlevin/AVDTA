/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.ReadNetwork;
import avdta.network.node.Node;
import avdta.network.node.obj.ObjFunction;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;

/**
 *
 * @author ut
 */
public class EnergyObj implements ObjFunction
{
    // add instance variables here
    public EnergyObj()
    {
        
    }
    
    public void initialize(Node n){}
    
    public boolean isMinimize()
    {
        return false;
    }
    
    // coefficient for vehicle v in objective function
    public double value(Vehicle v, TBR n)
    {
        if(v.getNextLink().isCentroidConnector())
        {
            return 100000;
        }
        
        //Link i = v.getPrevLink();
        //Link j = v.getNextLink();
        
        //return Math.pow(v.getEfficiency() * (Simulator.time - v.arr_time), 4);
        //return Math.pow(i.getQueueLength() / i.getNumLanes(), 2) - Math.pow(j.getQueueLength() / j.getNumLanes(), 2) ;
        //return 1.0 / v.getEfficiency() * ( (Simulator.time - v.arr_time)/60.0 + (Simulator.dt / (Simulator.time + Simulator.dt - v.arr_time) - Simulator.dt / (Simulator.time + 2*Simulator.dt - v.arr_time))*i.getFFSpeed() );
        
        //return 10*Math.pow(Math.max(v.calcDeltaEnergy()*100, 0), 2)+ Math.pow(i.getQueueLength() / i.getNumLanes(), 2) * 0.001;
        //return 1.0 / v.getEfficiency() * (1+Math.pow(v.getPrevLink().getQueueLength() / v.getPrevLink().getNumLanes(), 2) * 0.25);
        
        
        /* THIS IS WHAT WE WERE USING BEFORE*/
        //return 1+Math.pow(Math.max(v.calcDeltaEnergy()*1000, 0), 4);
        
        /*if(v.getPrevLink().getFFSpeed()>=60 && v.arr_time > Simulator.time)
        {
            
        }*/
        
        return v.calcDeltaEnergy()*1000;
    }
    
    public int getType()
    {
        return ReadNetwork.DE4;
    }
}
