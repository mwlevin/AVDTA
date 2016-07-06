/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.Network;
import avdta.network.link.SharedTransitCTMLink;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author micha
 */
public abstract class SharedTransitCell extends Cell
{
    protected List<Vehicle> currTransit, nextTransit;
    
    // for transit
    private double transit_R, transit_max_S;
    
    
    public SharedTransitCell(SharedTransitCTMLink link)
    {
        super(link);
        
        currTransit = new ArrayList<Vehicle>();
        nextTransit = new ArrayList<Vehicle>();
    }
    
    public int getOccupancy()
    {
        return super.getOccupancy() + currTransit.size();
    }
    
    public void addVehicle(Vehicle v)
    {
        if(v.isTransit())
        {
            nextTransit.add(v);
        }
        else
        {
            super.addVehicle(v);
        }
        
    }
    public SharedTransitCTMLink getLink()
    {
        return (SharedTransitCTMLink)super.getLink();
    }
    
    public void prepare()
    {
        double capacity = scaleCapacity(getLink().getTransitCapacityPerLane()) * Network.dt / 3600.0;
        transit_R = transit_R - Math.floor(transit_R);
        transit_max_S = transit_max_S - Math.floor(transit_max_S);
        
        transit_R += Math.min(capacity, scaleWaveSpeed(getLink().getTransitWaveSpeed()) / 
                getLink().getFFSpeed() * (getLink().getTransitCellJamdPerLane() - curr.size()));
        transit_max_S += capacity;
        
        super.prepare();
       
    }
    
    public void update()
    {
        super.update();
        
        for(Vehicle v : nextTransit)
        {
            currTransit.add(v);
        }

        nextTransit.clear();
        
    }
    
    public void setNumLanes(int n)
    {
        super.setNumLanes(n);
    }
    
    public int getNumTransitSendingFlow()
    {
        return (int)Math.min(transit_max_S, currTransit.size());
    }
    
    public double getTransitReceivingFlow()
    {
        return transit_R;
    }
    
    public double getTransitReceivingFlow(int numLanes)
    {
        return Math.min(scaleCapacity(getLink().getTransitCapacityPerLane()) * Network.dt / 3600.0, 
                scaleWaveSpeed(getLink().getTransitWaveSpeed()) / 
                getLink().getFFSpeed() * (getLink().getTransitCellJamdPerLane() - curr.size()));
    }
    
    public int getNumLanes()
    {
        if(currTransit.size() == 0)
        {
            return super.getNumLanes();
        }
        else
        {
            return super.getNumLanes()-1;
        }
    }
    
    public double getJamD()
    {
        return getLink().getCellJamdPerLane() * super.getNumLanes();
    }
    
    public double getAvgReactionTime()
    {
        if(currTransit.size() == 0)
        {
            return DriverType.HV.getReactionTime();
        }

        double avg_reaction_time = 0;

        for(Vehicle v : currTransit)
        {
            avg_reaction_time += v.getDriver().getReactionTime();
        }

        return avg_reaction_time / currTransit.size();
    }
    
    
    public List<Vehicle> getSendingFlow()
    {
        List<Vehicle> output = super.getSendingFlow();
        
        int num_transit = getNumTransitSendingFlow();
        
        for(int i = 0; i < num_transit; i++)
        {
            output.add(currTransit.get(i));
        }
        
        return output;
    }
    
    public boolean removeVehicle(Vehicle v)
    {
        if(v.isTransit())
        {
            return currTransit.remove(v);
        }
        else
        {
            return super.removeVehicle(v);
        }
    }
}
