/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Simulator;
import avdta.vehicle.Vehicle;
import avdta.vehicle.DriverType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Michael
 */
public class Cell implements Comparable<Cell>
{
    private static int id_count;
    private int id;
    
    
    private CTMLink link;
    private Cell prev;
    
    // for lane sharing
    private Cell opposite; 

    private List<Vehicle> curr, next;
    
    private int numLanes;
    
    private double R, max_S;

    public Cell(Cell prev, CTMLink link)
    {
        id = id_count++;
        
        this.prev = prev;
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
    
    public void setOppositeCell(Cell o)
    {
        opposite = o;
    }
    
    public Cell getOpposite()
    {
        return opposite;
    }
    
    // minimum lanes for this timestep
    public double getMinLanes()
    {
        if(curr.size() == 0)
        {
            return 0;
        }
        else if(curr.size() > link.getCellJamdPerLane() * (numLanes - 1))
        {
            return numLanes;
        }
        else
        {
            return numLanes - 1;
        }
    }
    
    public double getMaxLanes()
    {
        int total_road_lanes = link.getNumLanes();
        
        if(opposite != null)
        {
            total_road_lanes += opposite.link.getNumLanes();
        }
        
        if(curr.size() > 0)
        {
            return (int)Math.min(numLanes + 1, total_road_lanes);
        }
        else
        {
            return total_road_lanes;
        }
    }

    public CTMLink getLink()
    {
        return link;
    }
    
    public boolean isCongested()
    {
        return (curr.size() > link.getCapacityPerLane() * numLanes * Simulator.dt / 3600.0);
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

    public void step()
    {
        double y = Math.min(prev.getNumSendingFlow(), getReceivingFlow());

        Iterator<Vehicle> iter = prev.curr.iterator();
        

        while(y >= 1)
        {
            y -= 1;

            Vehicle v = iter.next();
            addVehicle(v);
            iter.remove();

            v.updatePosition(this);

        }
        
    }

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
        double capacity = scaleCapacity(link.getCapacityPerLane()) * numLanes * Simulator.dt / 3600.0;
        R = R - Math.floor(R);
        max_S = max_S - Math.floor(max_S);
        
        R += Math.min(capacity, scaleWaveSpeed(link.getWaveSpeed()) / link.getFFSpeed() * (link.getCellJamdPerLane() * numLanes - curr.size()));
        max_S += capacity;
       
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
        return c * (link.getFFSpeed() * DriverType.HV.getReactionTime() + Vehicle.vehicle_length) / (link.getFFSpeed() * getAvgReactionTime() + Vehicle.vehicle_length);
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
        return Math.min(scaleCapacity(link.getCapacityPerLane()) * numLanes * Simulator.dt / 3600.0, scaleWaveSpeed(link.getWaveSpeed()) / link.getFFSpeed() * (link.getCellJamdPerLane() * numLanes - curr.size()));
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
