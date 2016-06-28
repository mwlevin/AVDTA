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
import avdta.util.RunningAvg;
import avdta.network.Simulator;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Michael
 */
public class CTMLink extends Link
{
    
    
    public Cell[] cells;
    
    private double mesoDelta;
    
    
    
    
    
    public CTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);
        
        mesoDelta = wavespd / ffspd;
        
        
                
    }
    
    public void setMesoDelta(double delta)
    {
        mesoDelta = delta;
        
        setWaveSpeed(getFFSpeed() * delta);
    }
    
    public Cell[] getCells()
    {
        return cells;
    }
    
    public void initialize()
    {
        cells = new Cell[(int)Math.max(2, Math.round((getTrueLength() / getFFSpeed()) / (Network.dt / 3600.0) ))];
        
        cells[0] = createStartCell();
                
        for(int i = 1; i < cells.length - 1; i++)
        {
            cells[i] = createCell(cells[i-1]);
        }
        
        cells[cells.length -1] = createEndCell(cells[cells.length-2]);
    }
    
    public Cell createEndCell(Cell prev)
    {
        return new EndCell(prev, this);
    }
    
    public Cell createStartCell()
    {
        return new StartCell(this);
    }
    
    public Cell createCell(Cell prev)
    {
        return new LinkCell(prev, this);
    }
    
    public int getNumCells()
    {
        return cells.length;
    }
    
    
    
    public double getTrueLength()
    {
        return super.getLength();
    }
    
    public double getLength()
    {
        return cells.length * getCellLength();
    }
    
    public double getFFTime()
    {
        return cells.length * Network.dt;
    }
    
    
    
    public int getType()
    {
        return CTM;
    }
    
    public double getFirstCellSpeed()
    {
        return cells[0].getSpeed();
    }
    
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
    
    public double getMesoDelta()
    {
        return mesoDelta;
    }
    
    public void reset()
    {
        for(Cell c : cells)
        {
            c.reset();
            
            c.setNumLanes(getNumLanes());
        }
        
        
        
        super.reset();
    }
    
    public int getOccupancy()
    {
        int output = 0;
        
        for(Cell c : cells)
        {
            output += c.getOccupancy();
        }
        
        return output;
    }

    
    public void addVehicle(Vehicle v)
    {
        v.enteredLink(this);
        
        cells[0].addVehicle(v);
        
    }
    
    public int getNumSendingFlow()
    {
        return cells[cells.length-1].getNumSendingFlow();
    }
    
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
    
    
    public boolean removeVehicle(Vehicle v)
    {
        updateTT(v);
        return cells[cells.length-1].removeVehicle(v);
    }
    
    public int getDsLanes()
    {
        return cells[cells.length-1].getNumLanes();
    }
    
    public int getUsLanes()
    {
        return cells[0].getNumLanes();
    }
    
    public double getReceivingFlow()
    {
        return cells[0].getReceivingFlow();
    }
    
    
    public double getCellCapacityPerLane()
    {
        return getCapacityPerLane() * Network.dt / 3600.0;
    }
    
    public double getCellLength()
    {
        return getFFSpeed() * Network.dt / 3600.0;
    }
    
    public Cell getFirstCell()
    {
        return cells[0];
    }
    
    public Cell getLastCell()
    {
        return cells[cells.length-1];
    }
    
    public double getCellJamd()
    {
        return getCellJamdPerLane() * getNumLanes();
    }
    
    public double getCellJamdPerLane()
    {
        return getCellLength() * getJamDensityPerLane();
    }
    
    
    
    
    
    
    
    public void prepare()
    {
        
        
        for(Cell c : cells)
        {
            c.prepare();
        }
    }
    
    public void step()
    {
        for(Cell c : cells)
        {
            c.step();
        }
        
        
    }
    
    public void update()
    {
        
        
        for(Cell c : cells)
        {
            c.update();
        }
        
    }
}
