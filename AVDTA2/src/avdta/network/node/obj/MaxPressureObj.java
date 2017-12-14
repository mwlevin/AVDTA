/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.ReadNetwork;
import avdta.network.link.CTMLink;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.link.cell.Cell;
import avdta.network.node.Node;
import avdta.network.node.TBR;
import avdta.vehicle.Vehicle;
import java.util.HashMap;
import java.util.Map;
import avdta.network.type.Type;

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

        double w_ij = i.pressure_terms.get(j).x;
        
        for(Link k : j.pressure_terms.keySet())
        {
            PressureTerm temp = j.pressure_terms.get(k);
            
            w_ij -= temp.x * temp.p;
        }
        
        double m_ij = Math.min(i.getCapacity(), j.getCapacity());
        
        return (m_ij * w_ij);
    }
    
    
    
    /**
     * Initialization work before scanning the vehicle list. This calculates the pressure for incoming and outgoing links.
     */
    public void initialize(Node n)
    {
        for(Link l : n.getIncoming())
        {
            l.pressure_terms = calculatePressure(l);
        }
        
        for(Link l : n.getOutgoing())
        {
            l.pressure_terms = calculatePressure(l);
        }
    }
    
    
    public Map<Link, PressureTerm> calculatePressure(Link i)
    {
        Map<Link, PressureTerm> output = new HashMap<>();
        
        int total_x = 0;
        
        for(Vehicle v : i.getSendingFlow())
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
        
        return output;
    }
    
    public Type getType()
    {
        return ReadNetwork.MAX_PRESSURE;
    }
}