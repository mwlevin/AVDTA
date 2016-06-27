/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Network;
import avdta.network.RunningAvg;
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
    private static final double alpha = 0.99;
    
    public Cell[] cells;
    
    private double mesoDelta;
    
    private CTMLink opposite;
    
    // average sending, receiving flow every 15 min from last assignment
    private RunningAvg[] usSendingFlow, dsReceivingFlow;
    // store for next timestep
    private RunningAvg[] next_usSendingFlow, next_dsReceivingFlow;
    
    private int usSendingFlow_fordt;
    
    public CTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);
        
        mesoDelta = wavespd / ffspd;
        
        usSendingFlow_fordt = 0;
                
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
        
        cells[0] = new StartCell(this);
                
        for(int i = 1; i < cells.length - 1; i++)
        {
            cells[i] = new Cell(cells[i-1], this);
        }
        
        cells[cells.length -1] = new EndCell(cells[cells.length-2], this);
    }
    
    public int getNumCells()
    {
        return cells.length;
    }
    
    public boolean tieCells(CTMLink rhs)
    {
        if(rhs == null || opposite != null || !(getSource() == rhs.getDest() && getDest() == rhs.getSource()) || cells.length != rhs.cells.length)
        {
            return false;
        }
        
        this.opposite = rhs;
        rhs.opposite = this;

        for(int i = 0; i < cells.length; i++)
        {
            
            cells[i].setOppositeCell(opposite.cells[opposite.cells.length - 1 - i]);
            opposite.cells[i].setOppositeCell(cells[cells.length - 1 - i]);
        }
        
        return true;

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
    
    public boolean isTied()
    {
        return opposite != null;
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
        
        if(Network.isDLR())
        {
            usSendingFlow = next_usSendingFlow;
            dsReceivingFlow = next_dsReceivingFlow;

            next_usSendingFlow = new RunningAvg[Simulator.duration / Simulator.ast_duration];
            next_dsReceivingFlow = new RunningAvg[next_usSendingFlow.length];

            for(int i = 0; i < next_usSendingFlow.length; i++)
            {
                next_usSendingFlow[i] = new RunningAvg();
                next_dsReceivingFlow[i] = new RunningAvg();
            }
            
            usSendingFlow_fordt = 0;
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
    
    
    public void updateUsSendingFlowCounts()
    {
        next_usSendingFlow[Simulator.time / Simulator.ast_duration].add(usSendingFlow_fordt);
        usSendingFlow_fordt = 0;
    }
    
    
    public void addToUsSendingFlow()
    {
        usSendingFlow_fordt ++;
    }
    
    public void updateDsReceivingFlowCounts()
    {
        // count how much flow was moved
        next_dsReceivingFlow[Simulator.time / Simulator.ast_duration].add(q);
    }
    
    public void dlr()
    {
        // link with lower ID handles DLR for both, is link 1.
        if(opposite == null || opposite.getId() < getId())
        {
            return;
        }
        
        
        
        int[] lane_choices = solveMDP_all();

        int l1 = lane_choices[0];
        int l2 = lane_choices[1];
        int l1_D = lane_choices[2];
        int l2_D = lane_choices[3];
        
        
        for(int i = 1; i < cells.length-1; i++)
        {
            cells[i].setNumLanes(l1);
        }

        for(int i = 1; i < opposite.cells.length-1; i++)
        {
            opposite.cells[i].setNumLanes(l2);
        }

        int total_lanes = getTotalLanes();
        
        cells[0].setNumLanes(total_lanes - l2_D);
        opposite.cells[opposite.cells.length - 1].setNumLanes(total_lanes - l1_D);

        cells[cells.length-1].setNumLanes(l1_D);
        opposite.cells[0].setNumLanes(l2_D);
        
        
    }
    
    public int[] solveMDP_all()
    {
        
        int total_lanes = getTotalLanes();
        int l1 = cells[1].getNumLanes();
        int l2 = total_lanes - l1;
        
        // estimate over/undersaturation factor
        int T = (int)Math.round(cells.length * 4);
        //int T = 4;
        
        double demand1 = 0.0;
        double demand2 = 0.0;
        
        // demand
        for(int i = 0; i < cells.length; i++)
        {
            demand1 += cells[i].getOccupancy();
        }
        
        for(int i = 0; i < opposite.cells.length; i++)
        {
            demand2 += opposite.cells[i].getOccupancy();
        }
        
        // queues
        double S1 = getUsSendingFlow();
        double S2 = opposite.getUsSendingFlow();
        demand1 += S1;
        demand2 += S2;
        
        double incoming1 = 0;
        double incoming2 = 0;
        
        for(int t_idx = 0; t_idx < T; t_idx++)
        {
            incoming1 += getExpUsSendingFlow(Simulator.time + t_idx * Network.dt);
            incoming2 += opposite.getExpUsSendingFlow(Simulator.time + t_idx * Network.dt);
        }
        
        demand1 += incoming1;
        demand2 += incoming2;
        
        // supply
        double Q_per_dt = getCapacityPerLane() * Network.dt / 3600.0;
        
        
        // expected supply
        double R1_max = 0.0;
        double R2_max = 0.0;
        
        for(int t_idx = 0; t_idx < T; t_idx++)
        {
            R1_max += getExpDsReceivingFlow(Simulator.time + t_idx * Network.dt);
            R2_max += opposite.getExpDsReceivingFlow(Simulator.time + t_idx * Network.dt);
        }
        
        demand1 = Math.min(demand1, R1_max);
        demand2 = Math.min(demand2, R2_max);
        
        
        double supply1 = (T - cells.length) * Q_per_dt * l1;
        double supply2 = (T - opposite.cells.length) * Q_per_dt * l2;
        
        double delta_1 = demand1 - supply1;
        double delta_2 = demand2 - supply2;
        
        
        
        // average per timestep
        delta_1 /= T;
        delta_2 /= T;
        
        int l1_new = l1;
        int l2_new = l2;
        
        // if switching a link to 1 improves supply use
        if(delta_1 > Math.max(0, Q_per_dt + delta_2) && max_lanes_1() >= l1 + 1)
        {
            l1_new = l1 + 1;
            l2_new = l2 - 1;
        }
        else if(delta_2 > Math.max(0, Q_per_dt + delta_1) && max_lanes_2() >= l2 + 1)
        {
            l2_new = l2 + 1;
            l1_new = l1 - 1;
        }
        
        
        
        // option: additional turning lane
        int l1_D = l1_new;
        int l2_D = l2_new;
        
        double R1 = getExpDsReceivingFlow(Simulator.time);
        double R2 = opposite.getExpDsReceivingFlow(Simulator.time);
        
        // limit of at most 1 lane change per timestep
        
        if(l1_new <= l1)
        {
            // 1 downstream is oversaturated by delta_1D, 2 upstream undersaturated by delta_2U
            double delta_1D = Math.min(cells[cells.length-1].getOccupancy(), Math.min(Q_per_dt * (l1_new + 1), R1)) -
                    Math.min(cells[cells.length-1].getOccupancy(), Math.min(Q_per_dt * l1_new, R1));
            
            double delta_2U = Math.min(S2, opposite.cells[0].getReceivingFlow(l2_new)) - Math.min(S2, opposite.cells[0].getReceivingFlow(l2_new-1));
            
            if(delta_1D > 0 && delta_1D >= Math.pow(alpha, getNumCells()) * delta_2U)
            {
                l1_D = l1_new + 1;
            }
        }
        if(l2_new <= l2)
        {
            // 1 downstream is oversaturated by delta_1D, 2 upstream undersaturated by delta_2U
            double delta_2D = Math.min(opposite.cells[opposite.cells.length-1].getOccupancy(), Math.min(Q_per_dt * (l2_new + 1), R2)) -
                    Math.min(opposite.cells[opposite.cells.length-1].getOccupancy(), Math.min(Q_per_dt * l2_new, R2));
            
            double delta_1U = Math.min(S1, cells[0].getReceivingFlow(l1_new)) - Math.min(S1, cells[0].getReceivingFlow(l1_new-1));
            
            if(delta_2D > 0 && delta_2D >= Math.pow(alpha, getNumCells()) * delta_1U)
            {
                l2_D = l2_new + 1;
            }
        }
        
                
        //l1_new = 2;
        //l2_new = 2;
        
        return new int[]{l1_new, l2_new, l1_D, l2_D};
        
        //return new int[]{getNumLanes(), opposite.getNumLanes(), getNumLanes(), opposite.getNumLanes()};
    }
    
    public double getExpUsSendingFlow(int t)
    {
        int idx = t / Simulator.ast_duration;
        
        if(usSendingFlow == null || idx < 0 || idx >= usSendingFlow.length || usSendingFlow[idx].getCount() == 0)
        {
            return 0;
        }
        else
        {
            return usSendingFlow[idx].getAverage();
        }
    }
    
    public double getExpDsReceivingFlow(int t)
    {
        int idx = t / Simulator.ast_duration;
        
        if(dsReceivingFlow == null || idx < 0 || idx >= dsReceivingFlow.length || dsReceivingFlow[idx].getCount() == 0)
        {
            return getCellCapacityPerLane() * getNumLanes();
        }
        else
        {
            return dsReceivingFlow[idx].getAverage();
        }
    }
    
    public int max_lanes_1()
    {
        // this is the fixed lane assignment
        int total_lanes = getTotalLanes();
        int output = total_lanes;
        
        for(Cell c : opposite.cells)
        {
            output = (int)Math.min(output, total_lanes - c.getMinLanes());
        }
        
        //return output;
        return (int)Math.min(output, total_lanes-1);
    }
    
    public int max_lanes_2()
    {
        // this is the fixed lane assignment
        int total_lanes = getTotalLanes();
        int output = total_lanes;
        
        for(Cell c : cells)
        {
            output = (int)Math.min(output, total_lanes - c.getMinLanes());
        }
        
        //return output;
        return (int)Math.min(output, total_lanes-1);
    }
    
    public int getTotalLanes()
    {
        return getNumLanes() + opposite.getNumLanes();
    }
    
    
    public void prepare()
    {
        if(Network.isDLR())
        {
            dlr();
        }
        
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
        if(Network.isDLR())
        {
            updateDsReceivingFlowCounts();
            updateUsSendingFlowCounts();
        }
        
        for(Cell c : cells)
        {
            c.update();
        }
        
    }
}
