/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.Network;
import avdta.network.link.CTMLink;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Michael
 */
public abstract class Cell implements Comparable<Cell>
{
    private static int id_count;
    private int id;
    
    
    private CTMLink link;
    
    

    protected List<Vehicle> curr, next;
    
    private int numLanes;
    
    private double R, max_S;

    public Cell(CTMLink link)
    {
        id = id_count++;
        
        
        this.link = link;

        curr = new ArrayList<Vehicle>();
        next = new ArrayList<Vehicle>();
        
        numLanes = link.getNumLanes();
    }
    
    public String toString()
    {
        return ""+id;
    }
    
    public int compareTo(Cell rhs)
    {
        return id - rhs.id;
    }
    
    public int getId()
    {
        return id;
    }
    
    public int getNumLanes()
    {
        return numLanes;
    }
    
    public void setNumLanes(int n)
    {
        numLanes = n;
    }
    
    
    
    
    
    

    public CTMLink getLink()
    {
        return link;
    }
    
    public boolean isCongested()
    {
        return (curr.size() > scaleCapacity(getCapacity()) * Network.dt / 3600.0);
    }

    public void reset()
    {
        curr = new ArrayList<Vehicle>();
        next = new ArrayList<Vehicle>();
    }

    public List<Vehicle> getOccupants()
    {
        return curr;
    }
    
    public int getOccupancy()
    {
        return curr.size();
    }

    public double getLength()
    {
        return link.getCellLength();
    }

    public abstract void step();

    public void addVehicle(Vehicle v)
    {
        next.add(v);
    }

    public void update()
    {
        for(Vehicle v : next)
        {
            curr.add(v);
        }

        next.clear();
        
    }
    
    public void prepare()
    {
        double capacity = scaleCapacity(getCapacity()) * Network.dt / 3600.0;
        R = R - Math.floor(R);
        max_S = max_S - Math.floor(max_S);
        
        R += Math.min(capacity, scaleWaveSpeed(link.getWaveSpeed()) / link.getFFSpeed() * 
                (getJamD() - curr.size()));
        max_S += capacity;
       
    }
    
    public double getCapacity()
    {
        return link.getCapacityPerLane() * getNumLanes();
    }
    
    public double getJamD()
    {
        return link.getCellJamdPerLane() * getNumLanes();
    }

    public double getAvgReactionTime()
    {
        if(curr.size() == 0)
        {
            return DriverType.HV.getReactionTime();
        }

        double avg_reaction_time = 0;

        for(Vehicle v : curr)
        {
            avg_reaction_time += v.getDriver().getReactionTime();
        }

        return avg_reaction_time / curr.size();
    }

    public double scaleCapacity(double c)
    {
        return c * (link.getFFSpeed() * DriverType.HV.getReactionTime() + Vehicle.vehicle_length) / 
                (link.getFFSpeed() * getAvgReactionTime() + Vehicle.vehicle_length);
    }

    public double scaleWaveSpeed(double w)
    {
        return w * DriverType.HV.getReactionTime() / getAvgReactionTime();
    }

    public int getNumSendingFlow()
    {
        return (int)Math.min(max_S, curr.size());
    }

    public List<Vehicle> getSendingFlow()
    {
        List<Vehicle> output = new ArrayList<Vehicle>();

        int max = getNumSendingFlow();

        for(Vehicle v : curr)
        {
            if(max-- > 0)
            {
                output.add(v);
            }
            else
            {
                break;
            }
        }

        return output;
    }

    public double getReceivingFlow()
    {
        return R;
    }
    
    public double getReceivingFlow(int numLanes)
    {
        return Math.min(scaleCapacity(getCapacity()) * Network.dt / 3600.0, 
                scaleWaveSpeed(link.getWaveSpeed()) / link.getFFSpeed() * (getJamD() - curr.size()));
    }

    public boolean removeVehicle(Vehicle v)
    {
        Iterator<Vehicle> iter = curr.iterator();

        while(iter.hasNext())
        {
            if(iter.next().equals(v))
            {
                iter.remove();
                return true;
            }
        }

        return false;
    }
    
    public double getSpeed()
    {
        // return q / k
        if(curr.size() > 0)
        {
            return getReceivingFlow() / curr.size();
        }
        else
        {
            return link.getFFSpeed();
        }
    }
}
