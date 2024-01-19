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
import avdta.network.node.obj.BackPressureObj;
import avdta.network.link.Link;
import avdta.vehicle.EmergencyVehicle;

/**
 * This class represents a cell in the cell transmission model of a link ({@link CTMLink}). 
 * It stores {@link Vehicle}s currently in the cell, and determines sending and receiving flows.
 * This class is abstract; see {@link LinkCell}.
 * Cells at the start and ends of links are slightly different; see {@link StartCell} and {@link EndCell}
 * 
 * @author Michael
 */
public abstract class Cell implements Comparable<Cell>
{
    private static int id_count;
    private int id;
    
    
    private CTMLink link;
    
    
    
    private int num_emergency_vehicles;

    protected List<Vehicle> curr, next;
    
    private int numLanes;
    
    private double R, max_S;

    /**
     * Constructs this {@link Cell} as part of the specified link
     * @param link the link this cell is part of
     */
    public Cell(CTMLink link)
    {
        id = id_count++;
        
        
        this.link = link;

        curr = new ArrayList<Vehicle>();
        next = new ArrayList<Vehicle>();
        
        numLanes = link.getNumLanes();
        
        num_emergency_vehicles = 0;
    }
    
    /**
     * Returns the id of this {@link Cell}
     * @return the id of this {@link Cell} 
     */
    public String toString()
    {
        return ""+id;
    }
    
    /**
     * Orders cells based on id
     * @return order based on id
     */
    public int compareTo(Cell rhs)
    {
        return id - rhs.id;
    }
    
    /**
     * Returns the id of this {@link Cell}
     * @return the id of this {@link Cell} 
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Returns the number of lanes this {@link Cell} has. Note that this can change over time in dynamic lane reversal or dynamic transit lanes.
     * @return the number of lanes
     */
    public int getNumLanes()
    {
        return numLanes;
    }
    
    /**
     * Updates the number of lanes that this {@link Cell} has
     * @param n the new number of lanes
     */
    public void setNumLanes(int n)
    {
        if(n >= getMinLanes())
        {
            numLanes = n;
        }
        else
        {
            System.out.println("Occupancy: "+getOccupancy()+"\t"+"Jam D: "+getJamDPerLane()+" Num lanes: "+numLanes);
            
            int cell_num = 0;
            
            for(int i = 0; i < link.cells.length; i++)
            {
                if(link.cells[i] == this)
                {
                    cell_num = i;
                    break;
                }
            }
            System.out.println(link.getId()+" cell "+(cell_num +1)+" of "+link.cells.length);
            throw new RuntimeException("Invalid cell # of lanes - lanes: "+n+" link: "+getLink()+" min lanes: "+getMinLanes()+" max lanes: "+getMaxLanes()+" curr lanes:"+getNumLanes());
        }
    }
    
    public int getMinLanes()
    {
        return numLanes;
    }
    
    public int getMaxLanes()
    {
        return numLanes;
    }
    
    public boolean isValid(int numLanes)
    {
        return true;
    }
    
    
    
    
    
    
    /**
     * Returns the link this {@link Cell} is part of
     * @return the link this {@link Cell} is part of
     */
    public CTMLink getLink()
    {
        return link;
    }
    
    /**
     * This method estimates whether this {@link Cell} for the {@link BackPressureObj}. 
     * This returns true if the occupancy exceeds the capacity.
     * @return whether this {@link Cell} is congested
     */
    public boolean isCongested()
    {
        return (curr.size() > scaleCapacity(getCapacity()) * Network.dt / 3600.0);
    }

    /**
     * Resets the occupancies of this {@link Cell}. This is called to restart simulation.
     */
    public void reset()
    {
        curr = new ArrayList<Vehicle>();
        next = new ArrayList<Vehicle>();
        numLanes = link.getNumLanes();
    }

    /**
     * Returns a {@link List} containing the {@link Vehicle}s occupying this {@link Cell}.
     * @return a {@link List} containing the {@link Vehicle}s occupying this {@link Cell}
     */
    public List<Vehicle> getOccupants()
    {
        return curr;
    }
    
    /**
     * Returns the number of {@link Vehicle}s occupying this {@link Cell}.
     * @return the number of {@link Vehicle}s occupying this {@link Cell}
     */
    public int getOccupancy()
    {
        return curr.size();
    }

    /**
     * Returns the length associated with this cell.
     * @return {@link CTMLink#getCellLength()}
     */
    public double getLength()
    {
        return link.getCellLength();
    }

    /**
     * Executes one time step of simulation.
     */
    public abstract void step();

    /**
     * Add a {@link Vehicle} to this {@link Cell} for the next time step. The {@link Vehicle} will not be part of this {@link Cell} in the current time step.
     * @param v the {@link Vehicle} to be added.
     */
    public void addVehicle(Vehicle v)
    {
        next.add(v);
        
        if(v instanceof EmergencyVehicle)
        {
            num_emergency_vehicles++;
        }
    }

    /**
     * Updates this {@link Cell} to the next time step. {@link Vehicle}s that were added will become part of the {@link Cell} occupancy.
     */
    public void update()
    {
        // move emergency vehicles to the front of the link if possible
        if(num_emergency_vehicles > 0 && numLanes > 1 && curr.size() > getJamDPerLane() * (numLanes - 1))
        {
            List<Vehicle> emergencyVehicles = new ArrayList<Vehicle>();
            
            Iterator<Vehicle> iter = curr.iterator();
            
            while(iter.hasNext())
            {
                Vehicle v = iter.next();
                if(v instanceof EmergencyVehicle)
                {
                    emergencyVehicles.add(v);
                    iter.remove();
                }
            }

            for(Vehicle v : emergencyVehicles)
            {
                curr.add(0, v);
            }
            
            for(Vehicle v : next)
            {
                if(v instanceof EmergencyVehicle)
                {
                    curr.add(0, v);
                }
                else
                {
                    curr.add(v);
                }
            }
            
            next.clear();
        }
        else
        {
            for(Vehicle v : next)
            {
                curr.add(v);
            }

            next.clear();
        }
        
        
    }
    
    /**
     * Initialization work done every time step to prepare this {@link Cell} for calculating transition flows.
     */
    public void prepare()
    {
        int numLanes = getNumLanes();
        
        if(num_emergency_vehicles > 0 && numLanes > 1)
        {
            numLanes --;
        }
        
        double capacity = scaleCapacity(getCapacityPerLane() * numLanes) * Network.dt / 3600.0;

        R = R - Math.floor(R);
        max_S = max_S - Math.floor(max_S);

        
        R += Math.min(R+capacity, scaleWaveSpeed(link.getWaveSpeed()) / link.getFFSpeed() * 
                (getJamD() - curr.size()));

        
        max_S += capacity;
       
    }
    
    /**
     * Returns the capacity of this {@link Cell}. The capacity depends on the current number of lanes.
     * @return {@link CTMLink#getCapacityPerLane()}*{@link Cell#getNumLanes()}
     */
    public double getCapacity()
    {
        return link.getCapacityPerLane() * getNumLanes();
    }
    
    public double getCapacityPerLane()
    {
        return link.getCapacityPerLane();
    }
    
    /**
     * Returns the jam density of this {@link Cell}. The jam density depends on the current number of lanes.
     * @return {@link CTMLink#getCellJamdPerLane()}*{@link Cell#getNumLanes()}
     */
    public double getJamD()
    {
        return link.getCellJamdPerLane() * getNumLanes();
    }
    
    public double getJamDPerLane()
    {
        return link.getCellJamdPerLane();
    }

    /**
     * Calculates the average reaction time of {@link Vehicle}s currently in this {@link Cell}. 
     * This is used to determine the capacity and congested wave speed.
     * @return average reaction time
     */
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

    /**
     * Calculates the scaled capacity based on the reaction time model
     * @param c the input capacity
     * @return the scaled capacity
     */
    public double scaleCapacity(double c)
    {
        return c * (link.getFFSpeed() * DriverType.HV.getReactionTime() + Vehicle.vehicle_length) / 
                (link.getFFSpeed() * getAvgReactionTime() + Vehicle.vehicle_length);
    }

    /**
     * Calculates the scaled congested wave speed based on the reaction time model
     * @param w the input congested wave speed
     * @return the scaled congested wave speed
     */
    public double scaleWaveSpeed(double w)
    {
        return w * DriverType.HV.getReactionTime() / getAvgReactionTime();
    }

    /**
     * Returns the size of the sending flow set of {@link Vehicle}s
     * @return the size of the sending flow set of {@link Vehicle}s
     */
    public int getNumSendingFlow()
    {
        return (int)Math.min(max_S, curr.size());
    }

    /**
     * Calculates the set of {@link Vehicle}s that could exit the cell this time step
     * @return sending flow as set of {@link Vehicle}s
     */
    public List<Vehicle> getSendingFlow()
    {
        List<Vehicle> output = new ArrayList<Vehicle>();

        int max = getNumSendingFlow();

        for(int i = 0; i < max; i++)
        {
            output.add(curr.get(i));
        }


        return output;
    }

    /**
     * Returns the receiving flow for this time step. 
     * Note that this changes between time steps because the fractional part of the capacity is saved.
     * @return the receiving flow for this time step
     */
    public double getReceivingFlow()
    {
        return R;
    }
    
    /**
     * Calculates the receiving flow for the given number of lanes. This is based on the scaled capacity and congested wave speed.
     * @param numLanes the number of lanes available
     * @return the resulting receiving flow
     */
    public double getReceivingFlow(int numLanes)
    {
        if(num_emergency_vehicles > 0 && numLanes > 1)
        {
            numLanes --;
        }
        return Math.min(scaleCapacity(getCapacity()) * Network.dt / 3600.0, 
                scaleWaveSpeed(link.getWaveSpeed()) / link.getFFSpeed() * (getJamD() - curr.size()));
    }

    /**
     * Removes a {@link Vehicle} from this {@link Cell}
     * @param v the {@link Vehicle} to be removed
     * @return if the {@link Vehicle} was on the {@link Cell}
     */
    public boolean removeVehicle(Vehicle v)
    {
        if(curr.remove(v))
        {
            if(v instanceof EmergencyVehicle)
            {
                num_emergency_vehicles--;
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Estimates the speed for vehicles on this cell as flow/density
     * @return {@link Cell#getReceivingFlow()}/{@link Cell#getOccupancy()}, or {@link Link#getFFSpeed()} if empty
     */
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
