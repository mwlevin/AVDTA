/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.link.cell.Cell;
import avdta.network.link.cell.EndCell;
import avdta.network.link.cell.LinkCell;
import avdta.network.link.cell.StartCell;
import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.util.RunningAvg;
import avdta.network.Simulator;
import avdta.network.node.Node;
import avdta.network.type.Type;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Michael
 */
public class CTMLink extends Link
{
    
    
    public Cell[] cells;
    public RunningAvg avgTrafficVolume;
    private double mesoDelta;
    
    
    
    
    /**
     * Constructs the link with the given parameters 
     * @param id the link id
     * @param source the source node
     * @param dest the destination node
     * @param capacity the capacity per lane (veh/hr)
     * @param ffspd the free flow speed (mi/hr)
     * @param wavespd the congested wave speed (mi/hr)
     * @param jamd the jam density (veh/mi)
     * @param length the length (mi)
     * @param numLanes the number of lanes
     */
    public CTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);
        avgTrafficVolume = new RunningAvg();
        mesoDelta = wavespd / ffspd;
        
    }
    
    
    public Iterable<Vehicle> getVehicles()
    {
        return new CTMIterable();
    }
    
    public void updateAvgTrafficVolume() {
        int count = 0;
        for (Cell c : cells) {
            count = count + c.getOccupancy();
        }
        avgTrafficVolume.add(count);
    }
    
    /**
     * Sets the congested wave speed as delta * {@link Link#getFFSpeed()}
     * @param delta the ratio of congested wave speed to free flow speed
     */
    public void setMesoDelta(double delta)
    {
        mesoDelta = delta;
        
        setWaveSpeed(getFFSpeed() * delta);
    }
    
    /**
     * Returns the array of {@link Cell}s
     * @return the array of {@link Cell}s
     */
    public Cell[] getCells()
    {
        return cells;
    }
    
    /**
     * Called after all data is read. This method constructs the list of cells. 
     * Subclasses should override {@link CTMLink#createStartCell()}, {@link CTMLink#createCell(avdta.network.link.cell.Cell)}, and {@link CTMLink#createEndCell(avdta.network.link.cell.Cell)} to change the cells created.
     */
    public void initialize()
    {
        
        cells = new Cell[getNumCells()];
        
        cells[0] = createStartCell();
                
        for(int i = 1; i < cells.length - 1; i++)
        {
            cells[i] = createCell(cells[i-1]);
        }
        
        cells[cells.length -1] = createEndCell(cells[cells.length-2]);
    }
    
    /**
     * Creates the last cell on this link. This is used in {@link CTMLink#initialize()}.
     * @param prev the next to last cell on the link
     * @return the end cell
     */
    public Cell createEndCell(Cell prev)
    {
        return new EndCell(prev, this);
    }
    
    /**
     * Creates the first cell on this link. This is used in {@link CTMLink#initialize()}.
     * @return the first cell
     */
    public Cell createStartCell()
    {
        return new StartCell(this);
    }
    
    /**
     * Creates a  cell on this link. This is used in {@link CTMLink#initialize()}.
     * @param prev the preceding cell
     * @return the new cell
     */
    public Cell createCell(Cell prev)
    {
        return new LinkCell(prev, this);
    }
    
    /**
     * Returns the number of cells
     * @return the number of cells
     */
    public int getNumCells()
    {
        if(cells != null)
        {
            return cells.length;
        }
        else
        {
            return (int)Math.max(2, Math.round((getTrueLength() / getFFSpeed()) / (Network.dt / 3600.0) ));
        }
    }
    
    
    /**
     * Returns the length of the real link
     * @return the length of the real link (mi)
     */
    public double getTrueLength()
    {
        return super.getLength();
    }
    
    /**
     * Returns the length of the cell transmission model representation
     * @return the number of cells * {@link CTMLink#getCellLength()}
     */
    public double getLength()
    {
        return cells.length * getCellLength();
    }
    
    /**
     * Returns the free flow time of the cell transmission model representation
     * @return the number of cells * time step
     */
    public double getFFTime()
    {
        return cells.length * Network.dt;
    }
    
    
    /**
     * Returns the type code for this link
     * @return {@link ReadNetwork#CTM}
     */
    public Type getType()
    {
        return ReadNetwork.CTM;
    }
    
    /**
     * Returns the speed of the first cell
     * @return the speed of the first cell
     */
    public double getFirstCellSpeed()
    {
        return cells[0].getSpeed();
    }
    
    /**
     * Returns the length of the queue, which is based on whether cells are congested or not. 
     * The method starts from the last cell on the link, and continues until it reaches an uncongested cell.
     * The output is the sum of cell occupancies within the congested region.
     * @return the number of vehicles in the queue
     * @see Cell#isCongested() 
     */
    public int getQueueLength()
    {
        int queue = 0;
        
        for(int i = cells.length-1; i >= 0; i--)
        {
            if(cells[i].isCongested())
            {
                queue += cells[i].getOccupancy();
            }
            else
            {
                break;
            }
        }
        
        return queue;
    }
    
    public int getLastCellOccupancy()
    {
        return cells[cells.length-1].getOccupancy();
    }
    
    /**
     * Returns the efficiency queue length in the congested region.
     * @return the efficiency queue length
     * @see CTMLink#getQueueLength() 
     */
    public int getEffQueueLength()
    {
        int queue = 0;
        
        for(int i = cells.length-1; i >= 0; i--)
        {
            if(cells[i].isCongested())
            {
                for(Vehicle v : cells[i].getOccupants())
                {
                    queue += v.getEfficiency();
                }
            }
            else
            {
                break;
            }
        }
        
        return queue;
    }
    
    /**
     * Returns the ratio of free flow speed to congested wave speed
     * @return the ratio of free flow speed to congested wave speed
     */
    public double getMesoDelta()
    {
        return mesoDelta;
    }
    
    /**
     * Resets this link to restart the simulation.
     * This rests the number of lanes in each cell.
     */
    public void reset()
    {
        for(Cell c : cells)
        {
            c.reset();
        }
        
        avgTrafficVolume.reset();
        
        
        super.reset();
    }
    
    /**
     * Returns the number of vehicles on this link.
     * @return the number of vehicles on this link
     */
    public int getOccupancy()
    {
        int output = 0;
        
        for(Cell c : cells)
        {
            output += c.getOccupancy();
        }
        
        return output;
    }

    /**
     * Adds a vehicle to the first cell on this link
     * @param v the vehicle to be added
     */
    public void addVehicle(Vehicle v)
    {
        v.enteredLink(this);
        
        cells[0].addVehicle(v);
        
    }
    
    /**
     * Returns the number of vehicles that could exit this link this time step.
     * @return the size of the sending flow
     */
    public int getNumSendingFlow()
    {
        return cells[cells.length-1].getNumSendingFlow();
    }
    
    /**
     * Returns the sending flow from the last cell on this link. 
     * All vehicles in the sending flow have their arrival time at the end of the link updated to be the current simulation time.
     * @return the sending flow
     */
    public List<Vehicle> getSendingFlow()
    {
        List<Vehicle> output = cells[cells.length-1].getSendingFlow();
        
        for(Vehicle v : output)
        {
            if(v.arr_time < 0)
            {
                v.arr_time = Simulator.time;
            }
        }
        
        
        return output;
    }
    
    /**
     * Returns the sending flow from the last cell on this link. 
     * All vehicles in the sending flow have their arrival time at the end of the link updated to be the current simulation time.
     * @return the sending flow
     */
    public List<Vehicle> getVehiclesCanMove()
    {
        List<Vehicle> output = new ArrayList<Vehicle>();
        
        for(Vehicle v : cells[cells.length-1].getOccupants())
        {
            if(v.arr_time < 0)
            {
                v.arr_time = Simulator.time;
            }
            output.add(v);
        }
        
        return output;
    }
    
    
    /**
     * Removes a vehicle from this link
     * @param v the vehicle to be removed
     * @return if the vehicle was on this link
     */
    public boolean removeVehicle(Vehicle v)
    {
        updateTT(v);
        return cells[cells.length-1].removeVehicle(v);
    }
    
    /**
     * Returns the number of lanes on the downstream end of the link.
     * @return the number of lanes of the last cell on the link
     */
    public int getDsLanes()
    {
        return cells[cells.length-1].getNumLanes();
    }
    
    /**
     * Returns the number of lanes on the upstream end of the link.
     * @return the number of lanes of the first cell on the link
     */
    public int getUsLanes()
    {
        return cells[0].getNumLanes();
    }
    
    /**
     * Returns the number of vehicles that could enter this link this time step
     * @return the receiving flow
     */
    public double getReceivingFlow()
    {
        return cells[0].getReceivingFlow();
    }
    
    /**
     * Returns the cell capacity per lane
     * @return the cell capacity per lane (veh/s)
     */
    public double getCellCapacityPerLane()
    {
        return getCapacityPerLane() * Network.dt / 3600.0;
    }
    
    /**
     * Returns the length of one cell
     * @return the length of one cell (mi)
     */
    public double getCellLength()
    {
        return getFFSpeed() * Network.dt / 3600.0;
    }
    
    /**
     * Returns the first cell on this link
     * @return the first cell on this link
     */
    public Cell getFirstCell()
    {
        return cells[0];
    }
    
    /**
     * Returns the last cell on this link
     * @return the last cell on this link
     */
    public Cell getLastCell()
    {
        return cells[cells.length-1];
    }
    
    /**
     * Returns the jam density for a cell
     * @return {@link CTMLink#getCellJamdPerLane()}*{@link CTMLink#getNumLanes()}
     */
    public double getCellJamd()
    {
        return getCellJamdPerLane() * getNumLanes();
    }
    
    /**
     * Returns the number of vehicles that can fit in a cell per lane
     * @return {@link CTMLink#getCellLength()}*{@link CTMLink#getJamDensityPerLane()}
     */
    public double getCellJamdPerLane()
    {
        return getCellLength() * getJamDensityPerLane();
    }
    
    
    
    
    
    
    /**
     * Tells each cell to prepare to calculate transit flows
     */
    public void prepare()
    {
        
        
        for(Cell c : cells)
        {
            c.prepare();
        }
    }
    
    /**
     * Executes one time step of simulation for each cell
     */
    public void step()
    {
        for(Cell c : cells)
        {
            c.step();
        }
        
//        if (getId() == 105993) {
//            for (int i = 0; i < cells.length; i++) {
//                System.out.println("Link 105993. Cell #" + i + " occupancy: " + cells[i].getOccupancy());
//            }
//        }
//        
//        if (getId() == 5993) {
//            for (int i = 0; i < cells.length; i++) {
//                System.out.println("Link 5993. Cell #" + i + " occupancy: " + cells[i].getOccupancy());
//            }
//        }
    }
    
    /**
     * Updates cell occupancies for the next time step
     */
    public void update()
    {
        for(Cell c : cells)
        {
            c.update();
        }
        
    }
    
    
    class CTMIterable implements Iterable<Vehicle>
    {
        public Iterator<Vehicle> iterator()
        {
            return new CTMIterator(cells);
        }
    }
}

/**
 * Iterate through vehicles starting in the last cell, but iterate vehicles in order. 
 * @author mlevin
 */
class CTMIterator implements Iterator<Vehicle>
{
    private Cell[] cells;
    
    
    private int idx;
    private Iterator<Vehicle> currIter;
    
    public CTMIterator(Cell[] cells)
    {
        this.cells = cells;
        idx = cells.length-1;
        currIter = cells[idx].getOccupants().iterator();
    }
    
    public Vehicle next()
    {
        while(!currIter.hasNext())
        {
            idx--;
            currIter = cells[idx].getOccupants().iterator();
        }
        
        return currIter.next();
    }
    
    public boolean hasNext()
    {
        if(currIter.hasNext())
        {
            return true;
        }
        
        while(!currIter.hasNext())
        {
            idx--;
            if(idx < 0)
            {
                return false;
            }
            currIter = cells[idx].getOccupants().iterator();
        }
        return currIter.hasNext();
    }
    
    public void remove()
    {
        currIter.remove();
    }
}