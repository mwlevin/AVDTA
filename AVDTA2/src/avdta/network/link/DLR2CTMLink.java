/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.cell.Cell;
import avdta.network.link.cell.DLRCell;
import avdta.network.node.Intersection;
import avdta.network.node.IntersectionControl;
import avdta.network.node.Node;
import avdta.network.node.PriorityTBR;
import avdta.network.node.obj.MaxPressureObj;
import avdta.network.node.obj.PressureTerm;
import avdta.network.node.policy.ObjPolicy;
import avdta.network.type.Type;
import avdta.vehicle.Vehicle;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mlevin
 */
public class DLR2CTMLink extends DLRCTMLink
{

    
    
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
    public DLR2CTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);
    }
    
    public void prepare()
    {
        //pressureCalc.calculatePressure(this, getDest());
        
        for(Link l : getDest().getOutgoing())
        {
            pressureCalc.calculatePressure(l, getDest());
        }
        
        for(Link l : getSource().getOutgoing())
        {
            pressureCalc.calculatePressure(l, getDest());
        }
        super.prepare();
    }
    
    public DLR2CTMLink getOpposite()
    {
        return (DLR2CTMLink)super.getOpposite();
    }
    
/*
    public int[] solveMDP_all()
    {
        return null;
    }
    
    public void dlr()
    {
        DLR2CTMLink opposite = getOpposite();
        if(opposite == null || opposite.getId() > getId())
        {
            return;
        }
        
        // only calculate lanes per intersection once.
        if(us_lanes == -1)
        {
            Node source = getSource();

            if(source instanceof Intersection)
            {
                IntersectionControl control = ((Intersection)source).getControl();
                if(control instanceof PriorityTBR)
                {
                    MaxPressureObj obj = (MaxPressureObj)((ObjPolicy) ((PriorityTBR)control).getPolicy()).getObj();

                    obj.assignLanesDLR(source);
                }
                else
                {
                    us_lanes = getNumLanes();
                }
            }
            else
            {
                us_lanes = getNumLanes();
            }
        }
        
        if(ds_lanes == -1)
        {
            Node dest = getDest();

            if(dest instanceof Intersection)
            {
                IntersectionControl control = ((Intersection)dest).getControl();
                if(control instanceof PriorityTBR)
                {
                    MaxPressureObj obj = (MaxPressureObj)((ObjPolicy) ((PriorityTBR)control).getPolicy()).getObj();

                    obj.assignLanesDLR(dest);
                }
                else
                {
                    ds_lanes = getNumLanes();
                }
            }
            else
            {
                ds_lanes = getNumLanes();
            }
        }


        
        
        // distribute lanes along link
        // guarantee: ds_lanes is valid for last cell, us_lanes is valid for first cell
        // |ds_lanes - us_lanes| > 1 is possible.
        
        
        int[] lanes = new int[getNumCells()];
        
        while(Math.abs(us_lanes - ds_lanes) >= lanes.length)
        {
            int diff_us = Math.abs(us_lanes - getNumLanes());
            int diff_ds = Math.abs(ds_lanes - getNumLanes());
            
            // add or subtract 1, depending on whether it's higher or lower than default number of lanes
            if(diff_us > diff_ds)
            {
                us_lanes += (int)Math.ceil((getNumLanes() - us_lanes)/100.0);
            }
            else
            {
                ds_lanes += (int)Math.ceil((getNumLanes() - ds_lanes)/100.0);
            }
        }
        
        double split = (double) lanes.length / ((ds_lanes - us_lanes)+1);
        
        
        // about split cells for each lane
        
        // first assign # of lanes that we want
        
        for(int i = 0; i < lanes.length; i++)
        {
            lanes[i] = (int)Math.floor(i/split)+us_lanes;
            
        }
        
        if(getId() == 23 || getId() == 34)
        {
            //System.out.println(Simulator.time+" "+getId()+" "+us_lanes+" "+ds_lanes+" "+getFirstCell().getOccupancy()+" "+getLastCell().getOccupancy());
        }
        
        if(opposite.getId() == 23 || opposite.getId() == 34)
        {
            //System.out.println(Simulator.time+" "+opposite.getId()+" "+opposite.us_lanes+" "+opposite.ds_lanes+" "+opposite.getFirstCell().getOccupancy()+" "+opposite.getLastCell().getOccupancy());
        }
        
        // now check validity
        Cell[] cells = getCells();
        
        int total_lanes = getTotalLanes();
        
        for(int i = 0; i < lanes.length; i++)
        {
            int opp_lanes = total_lanes - lanes[i];
            DLRCell cell = (DLRCell)cells[i];
            DLRCell opp = cell.getOpposite();
            
            if(!cell.isValid(lanes[i]) || !opp.isValid(opp_lanes))
            {
  
                if(lanes[i] < cell.getMinLanes() || opp_lanes > opp.getMaxLanes())
                {
                    lanes[i]++;
                }
                else if(lanes[i] > cell.getMaxLanes() || opp_lanes < opp.getMinLanes())
                {
                    lanes[i]--;
                }
            }
        }
        
        for(int i = 0; i < lanes.length; i++)
        {
            DLRCell cell = (DLRCell)cells[i];
            DLRCell opp = cell.getOpposite();
            
            cell.setNumLanes(lanes[i]);
            opp.setNumLanes(total_lanes - lanes[i]);
        }
        
        
        
        
        
        
        
        
        
        
    }
    */
    
    
    private static MaxPressureObj pressureCalc = new MaxPressureObj();
    
    public int[] solveMDP_all()
    {
        int total_lanes = getTotalLanes();
        int l1 = cells[1].getNumLanes();
        int l2 = total_lanes - l1;
        
        DLRCTMLink opposite = getOpposite();
        
        int l1_new = l1;
        int l2_new = l2;
        double xw_1 = 0.0;
        double xw_2 = 0.0;
        
        // using the same map for both directions.
        /*
        Map<Link, Double> w_ij = new HashMap<>();
        
        for(Vehicle v : getVehicles())
        {
            Link j = v.getNextLink();
            
            if(!w_ij.containsKey(j))
            {
                w_ij.put(j, pressureCalc.value_DLR(this, j));
            }
            xw_1 += w_ij.get(j);
        }
        
        for(Vehicle v : opposite.getVehicles())
        {
            Link j = v.getNextLink();
            
            if(!w_ij.containsKey(j))
            {
                w_ij.put(j, pressureCalc.value_DLR(opposite, j));
            }
            xw_2 += w_ij.get(j);
        }
        */
        xw_1 = pressureCalc.value2(this);
        xw_2 = pressureCalc.value2(opposite);
        
        double best_w = Math.min(xw_1, getCapacityPerLane()*Network.dt/3600.0 * l1 * getNumCells()) + 
                Math.min(xw_2, opposite.getCapacityPerLane()*Network.dt/3600.0 * l2 * getNumCells());
        

        
        
        // 3 possibilities: l1-1, l1, l1+1

        if(max_lanes_1() >= l1+1)
        {
            double new_w = Math.min(xw_1, getCapacityPerLane()*Network.dt/3600.0 * (l1+1) * getNumCells()) + 
                    Math.min(xw_2, opposite.getCapacityPerLane()*Network.dt/3600.0 * (l2-1) * getNumCells());
            if(new_w > best_w)
            {
                l1_new = l1+1;
                l2_new = l2-1;
                best_w = new_w;
            }

        }
        
        if(max_lanes_2() >= l2+1)
        {
            double new_w = Math.min(xw_1, getCapacityPerLane()*Network.dt/3600.0 * (l1-1) * getNumCells()) 
                    + Math.min(xw_2, opposite.getCapacityPerLane()*Network.dt/3600.0 * (l2+1) * getNumCells());
            if(new_w > best_w)
            {
                l1_new = l1-1;
                l2_new = l2+1;
                best_w = new_w;
            }
        }
        
        /*
        if(getId() == 34)
        {
            System.out.println("34: "+l1_new+"\t43: "+l2_new);
            
            if(l2_new < 3)
            {
                System.out.println("x weight: 34: "+xw_1+"\t43: "+xw_2);
                System.out.println("Occupancy: 34: "+getOccupancy()+"\t43: "+opposite.getOccupancy());
                System.out.println("Pressure term: 34: "+pressure_terms);
                System.out.println("\t43: "+opposite.pressure_terms);
                
                for(Link l : getSource().getOutgoing())
                {
                   // if(l.getId() == 32)
                    {
                        System.out.println("\t"+l.getId()+": "+l.pressure_terms);
                    }
                }
            }
        }
*/
        
        int l1_D = l1_new;
        int l2_D = l2_new;
        
        /*
        double omega_ds_1 = 0.0;
        double omega_ds_1_a = 0.0;
        double omega_ds_1_b = 0.0;
        
        double omega_ds_2 = 0.0;
        double omega_ds_2_a = 0.0;
        double omega_ds_2_b = 0.0;
        
        if(l1_new <= l1)
        {
            DLRCell lastCell = (DLRCell)getCells()[getNumCells()-1];
            for(Vehicle v : lastCell.getOccupants())
            {
                Link j = v.getNextLink();

                if(!w_ij.containsKey(j))
                {
                    w_ij.put(j, pressureCalc.value_DLR(this, j));
                }
                omega_ds_1_a += w_ij.get(j);
            }

            DLRCell oppCell = lastCell.getOpposite();

            for(Vehicle v : oppCell.getOccupants())
            {
                Link j = v.getNextLink();

                if(!w_ij.containsKey(j))
                {
                    w_ij.put(j, pressureCalc.value_DLR(opposite, j));
                }
                omega_ds_1_b += w_ij.get(j);
            }

            omega_ds_1 = omega_ds_1_a * Math.min(1, getCapacityPerLane() * (l1_new+1) / lastCell.getOccupancy()) -
                    omega_ds_1_b * Math.min(1, opposite.getCapacityPerLane() * (l2_new-1) / oppCell.getOccupancy());
            
            if(omega_ds_1 > 0)
            {
                l1_D = l1_new+1;
            }
        }
        
        if(l2_new <= l2)
        {
            DLRCell lastCell = (DLRCell)opposite.getCells()[opposite.getNumCells()-1];
            for(Vehicle v : lastCell.getOccupants())
            {
                Link j = v.getNextLink();

                if(!w_ij.containsKey(j))
                {
                    w_ij.put(j, pressureCalc.value_DLR(opposite, j));
                }
                omega_ds_2_a += w_ij.get(j);
            }

            DLRCell oppCell = lastCell.getOpposite();

            for(Vehicle v : oppCell.getOccupants())
            {
                Link j = v.getNextLink();

                if(!w_ij.containsKey(j))
                {
                    w_ij.put(j, pressureCalc.value_DLR(this, j));
                }
                omega_ds_2_b += w_ij.get(j);
            }

            omega_ds_2 = omega_ds_2_a * Math.min(1, opposite.getCapacityPerLane() * (l2_new+1) / lastCell.getOccupancy()) -
                    omega_ds_2_b * Math.min(1, getCapacityPerLane() * (l1_new-1) / oppCell.getOccupancy());
            
            if(omega_ds_2 > 0)
            {
                l2_D = l2_new+1;
            }
        }
        */
        

        return new int[]{l1_new, l2_new, l1_new, l2_new};
    }
    
    
    
    
    /**
     * Returns the type code of this link
     * @return {@link ReadNetwork#CTM}+{@link ReadNetwork#DLR}
     */
    public Type getType()
    {
        return ReadNetwork.DLR2;
    }
}
