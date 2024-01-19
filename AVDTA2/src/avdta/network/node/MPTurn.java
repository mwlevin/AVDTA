/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;
import avdta.vehicle.Vehicle;
import avdta.network.link.CentroidConnector;
import avdta.network.link.MPLink;

/**
 * Designed to be used with MPLinks
 * @author mlevin
 */
public class MPTurn extends Turn
{
    private int queue;
    private double p_ij;
    
    
    public double num;
    public double denom;
    
    //// RC 7/23/2019 ////
    public double average_waiting_time;
    public int total_waiting_time;
    //////////////////////
    
    public MPTurn(Link i, Link j)
    {
        super(i, j);
    }
    
    public void setTurningProportion(double p)
    {
        p_ij = p;
    }
    
    public double getWeight(MPWeight func)
    {
        //System.out.println(" prop: "+ this.p_ij+ " MPTurn: "+ this.i+ ","+this.j);
        double weight = func.calcMPWeight(this); // x_ij or T_ij
        
        Node n = j.getDest();
        
        if(n instanceof Intersection)
        {
            MaxPressure control = (MaxPressure)((Intersection)n).getControl();
            
            for(MPTurn t : control.getTurns())
            {
                if(t.i == j)
                {
                    weight -= t.getTurningProportion() * func.calcMPWeight(t); // - p_jk x_jk or -p_jk T_jk
                }
            }
        }
        
        return weight;
    }
    

    ///// update queue length, average waiting time, and the total waiting time
    public void calculateQueue()
    {
        int queue = 0;
        int total_waiting = 0;
        double average_waiting = 0;
        
        
        Iterable<Vehicle> sending;
        
        if(i instanceof CentroidConnector)
        {
            sending = i.getVehicles();
        }
        else
        {
            sending = ((MPLink)i).getLastCell().getOccupants();
        }
        
        for(Vehicle v : sending)
        {
            if(v.getNextLink() == j)
            {
                queue++;
                total_waiting = total_waiting + v.getDelayInCell();
            }
        }
        if (queue > 0){
            average_waiting = total_waiting*1.0 / queue;
        }
        this.queue = queue;
        this.average_waiting_time = average_waiting;
        this.total_waiting_time = total_waiting;
    }
    
    public int getQueue()
    {
        return queue;
    }
    
    public double getTurningProportion()
    {
        return p_ij;
    }
    
    public int getTotalWaitingTime(){
        return this.total_waiting_time;
    }
    
    public double getAverageWaitingTime(){
        return this.average_waiting_time;
    }
}
