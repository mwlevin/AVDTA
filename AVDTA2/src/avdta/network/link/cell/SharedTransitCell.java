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
    private double R, max_S;
    
    
    public SharedTransitCell(SharedTransitCTMLink link)
    {
        super(link);
        
        currTransit = new ArrayList<Vehicle>();
        nextTransit = new ArrayList<Vehicle>();
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
        R = R - Math.floor(R);
        max_S = max_S - Math.floor(max_S);
        
        R += Math.min(capacity, scaleWaveSpeed(getLink().getTransitWaveSpeed()) / 
                getLink().getFFSpeed() * (getLink().getTransitCellJamdPerLane() - curr.size()));
        max_S += capacity;
        
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
        super.setNumLanes(n-1);
    }
    
    public int getNumTransitSendingFlow()
    {
        return (int)Math.min(max_S, currTransit.size());
    }
    
    public double getTransitReceivingFlow()
    {
        return R;
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
            return super.getNumLanes()-1;
        }
        else
        {
            return super.getNumLanes();
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
}
