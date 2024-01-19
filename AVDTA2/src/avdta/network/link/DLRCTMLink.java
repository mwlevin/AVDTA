/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.link.cell.Cell;
import avdta.network.link.cell.DLRCell;
import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.cell.DLREndCell;
import avdta.network.link.cell.DLRLinkCell;
import avdta.network.link.cell.DLRStartCell;
import avdta.network.node.Node;
import avdta.network.type.Type;
import avdta.util.RunningAvg;

/**
 *
 * @author micha
 */
public class DLRCTMLink extends CTMLink
{
    private static final double alpha = 1;
    
    // average sending, receiving flow every 15 min from last assignment
    private RunningAvg[] usSendingFlow, dsReceivingFlow;
    // store for next timestep
    private RunningAvg[] next_usSendingFlow, next_dsReceivingFlow;
    
    private int usSendingFlow_fordt;
    
    private DLRCTMLink opposite;
    
    
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
    public DLRCTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);
        
        usSendingFlow_fordt = 0;
    }
    
    /**
     * Overrides to create a {@link DLREndCell}
     * @param prev the previous cell
     * @return the appropriate type of end cell
     */
    public Cell createEndCell(Cell prev)
    {
        return new DLREndCell((DLRCell)prev, this);
    }
    
    /**
     * Overrides to create a {@link DLRStartCell}
     * @return the appropriate type of start cell
     */
    public Cell createStartCell()
    {
        return new DLRStartCell(this);
    }
    
    /**
     * Overrides to create a {@link DLRLinkCell}
     * @param prev the previous cell
     * @return the appropriate type of link cell
     */
    public Cell createCell(Cell prev)
    {
        return new DLRLinkCell((DLRCell)prev, this);
    }
    
    /**
     * Returns whether this link is tied to an opposite and parallel link
     * @return whether this link is tied to an opposite and parallel link
     */
    public boolean isTied()
    {
        return opposite != null;
    }
    
    public DLRCell getFirstCell()
    {
        return (DLRCell)super.getFirstCell();
    }
    
    public DLRCell getLastCell()
    {
        return (DLRCell)super.getLastCell();
    }
    
    /**
     * Updates the number of lanes in each cell for the next time step
     */
    public void dlr()
    {
        // link with lower ID handles DLR for both, is link 1.
        if(opposite == null || opposite.getId() < getId())
        {
            return;
        }
        
        
        
        int[] lane_choices = solveMDP_all();

        l1 = lane_choices[0];
        l2 = lane_choices[1];
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
        opposite.cells[opposite.cells.length - 1].setNumLanes(l2_D);

        cells[cells.length-1].setNumLanes(l1_D);
        opposite.cells[0].setNumLanes(total_lanes - l1_D);
        

    }
    
    /**
     * Saturation-based heuristic to calculate the number of lanes in each cell/
     * @return an int array with the number of lanes for each link, as well as the number of lanes for the downstream cells
     */
    public int[] solveMDP_all()
    {
        
        int total_lanes = getTotalLanes();

        

        
        // estimate over/undersaturation factor
        int T = (int)Math.round(cells.length*1);
        //int T = 4;
        
        double demand1 = 0.0;
        double demand2 = 0.0;
        
        // demand
        for(int i = cells.length-1; i >= 0; i--)
        {
            demand1 += cells[i].getOccupancy()* Math.pow(alpha, cells.length-1-i);

        }
        
        for(int i = 0; i < opposite.cells.length; i++)
        {
            demand2 += opposite.cells[i].getOccupancy()* Math.pow(alpha, i);
            

        }
        
        // queues
        double S1 = getUsSendingFlow();
        double S2 = opposite.getUsSendingFlow();
        demand1 += S1;
        demand2 += S2;
        
        double incoming1 = 0;
        double incoming2 = 0;
        
        for(int t_idx = T - cells.length; t_idx < T; t_idx++)
        {
            incoming1 += getExpUsSendingFlow(Simulator.time + t_idx * Network.dt)* Math.pow(alpha, t_idx);
            incoming2 += opposite.getExpUsSendingFlow(Simulator.time + t_idx * Network.dt)* Math.pow(alpha, t_idx);
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
            R1_max += getExpDsReceivingFlow(Simulator.time + t_idx * Network.dt) * Math.pow(alpha, t_idx);
            R2_max += opposite.getExpDsReceivingFlow(Simulator.time + t_idx * Network.dt) * Math.pow(alpha, t_idx);
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
        if(delta_1 > 0 && delta_1 > Math.max(0, Q_per_dt + delta_2) && max_lanes_1() >= l1 + 1)
        {
            l1_new = l1 + 1;
            l2_new = l2 - 1;
        }
        else if(delta_2 > 0 && delta_2 > Math.max(0, Q_per_dt + delta_1) && max_lanes_2() >= l2 + 1)
        {
            l2_new = l2 + 1;
            l1_new = l1 - 1;
        }
        
        if(l1_new != l1)
        {
            //System.out.println(getId()+" "+delta_1+" "+delta_2);
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
            
            if(opposite.cells[0].getMinLanes() <= l2_new-1 && delta_1D > 0 && delta_1D >= Math.pow(alpha, getNumCells()) * delta_2U)
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
            
            if(cells[0].getMinLanes() <= l1_new-1 && delta_2D > 0 && delta_2D >= Math.pow(alpha, getNumCells()) * delta_1U)
            {
                l2_D = l2_new + 1;
            }
        }
        
                
        //l1_new = 2;
        //l2_new = 2;
        
        return new int[]{l1_new, l2_new, l1_D, l2_D};
        
        //return new int[]{getNumLanes(), opposite.getNumLanes(), getNumLanes(), opposite.getNumLanes()};
    }
    
    /**
     * Estimate the expected upstream sending flow at the specified time
     * @param t the specified time (s)
     * @return the expected upstream sending flow
     */
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
    
    /**
     * Estimate the expected downstream sending flow at the specified time
     * @param t the specified time (s)
     * @return the expected downstream sending flow
     */
    public double getExpDsReceivingFlow(int t)
    {
        int idx = t / Simulator.ast_duration;
        
        if(true)
        //if(dsReceivingFlow == null || idx < 0 || idx >= dsReceivingFlow.length || dsReceivingFlow[idx].getCount() == 0)
        {
            return getCellCapacityPerLane() * getNumLanes();
        }
        else
        {
            return dsReceivingFlow[idx].getAverage();
        }
    }
    
    /**
     * Calculate the maximum number of lanes for this link for the next time step
     * @return the maximum number of lanes for this link for the next time step
     */
    public int max_lanes_1()
    {
        // this is the fixed lane assignment
        int total_lanes = getTotalLanes();
        int output = total_lanes;
        
        for(int i = 0; i < cells.length; i++)
        {
            Cell opposite = ((DLRCell)cells[i]).getOpposite();
            output = (int)Math.min(Math.min(cells[i].getMaxLanes(), output), total_lanes - opposite.getMinLanes());
        }
        
        //return output;
        return (int)Math.min(output, total_lanes-1);
    }
    
    /**
     * Calculate the maximum number of lanes for the opposite link for the next time step
     * @return the maximum number of lanes for the opposite  link for the next time step
     */
    public int max_lanes_2()
    {
        // this is the fixed lane assignment
        int total_lanes = getTotalLanes();
        int output = total_lanes;
        
        for(Cell c : cells)
        {
            output = (int)Math.min(output, total_lanes - ((DLRCell)c).getMinLanes());
        }
        
        //return output;
        return (int)Math.min(output, total_lanes-1);
    }
    
    /**
     * Returns the total number of lanes between this link and its opposite
     * @return the total number of lanes between this link and its opposite
     */
    public int getTotalLanes()
    {
        return getNumLanes() + opposite.getNumLanes();
    }
    
    
    protected int l1, l2;
    
    /**
     * Links cells on this link to the corresponding cell on the opposite and parallel link
     * @param rhs the opposite and parallel link
     * @return if cells were tied successfully
     */
    public boolean tieCells(DLRCTMLink rhs)
    {
        if(rhs == null || opposite != null || !(getSource() == rhs.getDest() && getDest() == rhs.getSource()) || cells.length != rhs.cells.length)
        {
            return false;
        }
        
        this.opposite = rhs;
        rhs.opposite = this;

        for(int i = 0; i < cells.length; i++)
        {
            
            ((DLRCell)cells[i]).setOppositeCell((DLRCell)opposite.cells[opposite.cells.length - 1 - i]);
            ((DLRCell)opposite.cells[i]).setOppositeCell((DLRCell)cells[cells.length - 1 - i]);
        }
        
        l1 = getNumLanes();
        l2 = rhs.getNumLanes();
        
        return true;

    }
    
    /**
     * Resets this link to restart simulation.
     * This updates the upstream and downstream sending flow counts
     */
    public void reset()
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
        
        l1 = getNumLanes();
        
        if(opposite != null)
        {
            l2 = opposite.getNumLanes();
        }
        
        super.reset();
    }
    
    /**
     * Updates the upstream sending flow counts for the next time step
     */
    public void updateUsSendingFlowCounts()
    {
        next_usSendingFlow[Simulator.time / Simulator.ast_duration].add(usSendingFlow_fordt);
        usSendingFlow_fordt = 0;
    }
    
    public DLRCTMLink getOpposite()
    {
        return opposite;
    }
    
    /**
     * Adds to the upstream sending flow at the current time step
     */
    public void addToUsSendingFlow()
    {
        usSendingFlow_fordt ++;
    }
    
    /**
     * Updates the downstream sending flow counts for the next time step
     */
    public void updateDsReceivingFlowCounts()
    {
        // count how much flow was moved
        next_dsReceivingFlow[Simulator.time / Simulator.ast_duration].add(q);
    }
    
    /**
     * Updates the number of lanes per cell, then calls {@link CTMLink#prepare()}.
     */
    public void prepare()
    {     
        dlr();

        super.prepare();
        
    }
    
    /**
     * Updates the sending and receiving flow counts, then calls {@link CTMLink#update()}.
     */
    public void update()
    {
        updateDsReceivingFlowCounts();
        updateUsSendingFlowCounts();
        
        
        super.update();
    }
    
    /**
     * Returns the type code of this link
     * @return {@link ReadNetwork#CTM}+{@link ReadNetwork#DLR}
     */
    public Type getType()
    {
        return ReadNetwork.DLR;
    }
}
