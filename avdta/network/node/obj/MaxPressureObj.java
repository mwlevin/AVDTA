/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.Network;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.CTMLink;
import avdta.network.link.CentroidConnector;
import avdta.network.link.DLR2CTMLink;
import avdta.network.link.Link;
import avdta.network.link.cell.Cell;
import avdta.network.node.Node;
import avdta.network.node.TBR;
import avdta.vehicle.Vehicle;
import java.util.HashMap;
import java.util.Map;
import avdta.network.type.Type;
import avdta.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This differs from BackPressureObj: this is based on the max-pressure DLR/AIM control developed in a separate paper.
 * 
 * @author mlevin
 */
public class MaxPressureObj implements ObjFunction
{
    /**
     * Returns whether the IP is trying to minimize the objective function.
     * @return false
     */
    public boolean isMinimize()
    {
        return false;
    }
    
    
    /**
     * Returns the coefficient for moving vehicle across the intersection
     * The coefficient is based on the pressure from high link queues.
     * 
     * @param vehicle the {@link Vehicle} the weight applies to
     * @param node the intersection at which the weight applies to
     * @return coefficient for moving vehicle across the intersection. 
     */
    public double value(Vehicle vehicle, TBR node)
    {
        Link i = vehicle.getPrevLink();
        Link j = vehicle.getNextLink();

        double w_ij = value_DLR(i, j);
        
        double m_ij = Math.min(i.getCapacityPerLane()*Network.dt/3600.0 * i.getDsLanes(), j.getCapacityPerLane()*Network.dt/3600.0 * j.getUsLanes());
        
        return (m_ij * w_ij);
    }
    
    public double value_DLR(Link i, Link j)
    {
        double w_ij = i.pressure_terms.get(j).x;
        
        for(Link k : j.pressure_terms.keySet())
        {
            PressureTerm temp = j.pressure_terms.get(k);
            
            w_ij -= temp.x * temp.p;
        }
        
        
        return w_ij;
    }
    
    public double value2(Link i)
    {
        double output = 0.0;
        
        for(Link j : i.getDest().getOutgoing())
        {
            double w_ij = value_DLR(i, j);
            output += w_ij * i.pressure_terms.get(j).p;
        }
        return output;
    }
    
    
    private Map<DLR2CTMLink, Integer> createPossibility(ArrayList<Pair<DLR2CTMLink, DLR2CTMLink>> dlr_links)
    {
        Map<DLR2CTMLink, Integer> output = new HashMap<>();
        
        for(Pair<DLR2CTMLink, DLR2CTMLink> pair : dlr_links)
        {
            output.put(pair.first(), pair.first().ds_lanes);
            output.put(pair.second(), pair.second().us_lanes);
        }
        return output;
    }
    
    private boolean isValid(ArrayList<Pair<DLR2CTMLink, DLR2CTMLink>> dlr_links)
    {
        for(Pair<DLR2CTMLink, DLR2CTMLink> pair : dlr_links)
        {
            DLR2CTMLink inc = pair.first();
            DLR2CTMLink out = pair.second();

            if(!inc.getLastCell().isValid(inc.ds_lanes) || !out.getFirstCell().isValid(out.us_lanes))
            {
                return false;
            }
        }
        return true;
    }
            
    public void assignLanesDLR(Node node)         
    {
        
        initialize(node);
        
        /*
        if(node.getId() == 3)
        {
            DLR2CTMLink i = null;
            DLR2CTMLink j = null;
            
            for(Link inc : node.getIncoming())
            {
                if(inc.getId() == 23)
                {
                    i = (DLR2CTMLink)inc;
                    break;
                }
            }
            
            for(Link out : node.getOutgoing())
            {
                if(out.getId() == 34)
                {
                    j = (DLR2CTMLink)out;
                    break;
                }
            }
            
            System.out.println(Simulator.time);
            System.out.println("\t"+i+" "+j+" "+value_DLR(i, j)+" "+i.getLastCell().getOccupancy()+" "+j.getFirstCell().getOccupancy());
            System.out.println("\t"+i.pressure_terms);
            System.out.println("\t"+j.pressure_terms);
        }
        */
        // meta loop through all incoming/outgoing links
        
        
        
        
        // first() is always the incoming link, second() is always the outgoing link
        ArrayList<Pair<DLR2CTMLink, DLR2CTMLink>> dlr_links = new ArrayList<>();
        
        for(Link i : node.getIncoming())
        {
            if(i instanceof DLR2CTMLink)
            {
                DLR2CTMLink link = (DLR2CTMLink)i;
                if(link.isTied())
                {
                    dlr_links.add(new Pair<DLR2CTMLink, DLR2CTMLink>(link, link.getOpposite()));
                    link.ds_lanes = -1;
                    link.getOpposite().us_lanes = -1;
                }
            }
        }
        
        if(dlr_links.isEmpty())
        {
            // do nothing, all links have fixed lanes
            return;
        }
        

        // use the current state as the default possibility
        for(Pair<DLR2CTMLink, DLR2CTMLink> p : dlr_links)
        {
            p.first().ds_lanes = p.first().getLastCell().getNumLanes();
            p.second().us_lanes = p.first().getTotalLanes() - p.first().ds_lanes;
        }


        int[] divisors = new int[dlr_links.size()+1];
        
        for(int i = 0; i < divisors.length; i++)
        {
            divisors[i] = 1;
        }
        
        for(int i = 0; i < dlr_links.size(); i++)
        {
            Pair<DLR2CTMLink, DLR2CTMLink> p = dlr_links.get(i);
            for(int j = i+1; j < divisors.length; j++)
            {
                divisors[j] *= (p.first().getTotalLanes()-1);
            }
        }
        
        int num_possibilities = divisors[divisors.length-1];
        

        Map<DLR2CTMLink, Integer> best = null;
        double best_obj = Integer.MIN_VALUE;
        

        for(int x = 0; x < num_possibilities; x++)
        {
            for(int idx = 0; idx < dlr_links.size(); idx++)
            {
                Pair<DLR2CTMLink, DLR2CTMLink> p = dlr_links.get(idx);
                
                p.first().ds_lanes = (int)((x % divisors[idx+1]) / divisors[idx]) + 1;
                p.second().us_lanes = p.first().getTotalLanes() - p.first().ds_lanes;
                
            }
            

            double new_obj = 0.0;
            
            
            for(Link i : node.getIncoming())
            {
                for(Link j : node.getOutgoing())
                {
                    double w_ij = value_DLR(i, j);

                    
                    new_obj += w_ij * Math.min(i.ds_lanes * i.getCapacityPerLane() * Network.dt/3600.0, j.us_lanes * j.getCapacityPerLane() * Network.dt/3600.0);
                }

            }
            
            
            if(new_obj > best_obj)
            {
                best_obj = new_obj;
                best = createPossibility(dlr_links);
            }
        }


        
        for(DLR2CTMLink l : best.keySet())
        {
            if(best.containsKey(l))
            {
                if(node.getIncoming().contains(l))
                {
                    l.ds_lanes = best.get(l);
                }
                else if(node.getOutgoing().contains(l))
                {
                    l.us_lanes = best.get(l);
                }
            }
            

        }
        

    }
    
    
    
    /**
     * Initialization work before scanning the vehicle list. This calculates the pressure for incoming and outgoing links.
     */
    public void initialize(Node n)
    {
        for(Link l : n.getIncoming())
        {
            calculatePressure(l, n);
        }
        
        for(Link l : n.getOutgoing())
        {
            calculatePressure(l, n);
        }
    }
    
    
    public void calculatePressure(Link i, Node n)
    {
        if(i.pressure_terms != null)
        {
            return;
        }
        
        Map<Link, PressureTerm> output = new HashMap<>();
        
        int total_x = 0;
        
        /*
        Iterable<Vehicle> iter = null;
        
        if(i instanceof CTMLink)
        {
            CTMLink link = (CTMLink)i;
            
            if(link.getDest() == n)
            {
                iter = link.getLastCell().getOccupants();
                //iter = link.getVehicles();
            }
            else if(link.getSource() == n)
            {
                iter = link.getFirstCell().getOccupants();
            }
        }
        else
        {
            iter = i.getVehicles();
        }
*/
        for(Vehicle v : i.getVehicles())
        {
            Link j = v.getNextLink();
            
            if(output.containsKey(j))
            {
                output.get(j).x ++;
            }
            else
            {
                output.put(j, new PressureTerm(1, 0));
            }
            
            total_x++;
        }
        
        for(Link j : output.keySet())
        {
            PressureTerm term = output.get(j);
            term.p = (double)term.x / total_x;
        }
        
        for(Link j : i.getDest().getOutgoing())
        {
            if(!output.containsKey(j))
            {
                output.put(j, new PressureTerm(0, 0));
            }
        }
        
        i.pressure_terms = output;
    }
    
    public Type getType()
    {
        return ReadNetwork.MAX_PRESSURE;
    }
}