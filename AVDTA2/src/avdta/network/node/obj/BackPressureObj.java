/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

import avdta.network.ReadNetwork;
import avdta.network.link.CTMLink;
import avdta.network.link.cell.Cell;
import avdta.network.link.CentroidConnector;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import avdta.network.node.TBR;
import avdta.network.node.TBR;
import java.util.List;

/**
 * This is the objective function used for the backpressure policy. 
 * 
 * WARNING: don't use this with LTM.
 * 
 * @author Michael
 */
public class BackPressureObj implements ObjFunction
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
     * Initialization work before scanning the vehicle list. This calculates the pressure for incoming and outgoing links.
     */
    public void initialize(Node n)
    {
        for(Link l : n.getIncoming())
        {
            l.pressure = calculatePressure(l);
        }
        
        for(Link l : n.getOutgoing())
        {
            l.pressure = calculatePressure(l);
        }
    }
    
    private int calculatePressure(Link l)
    {
        if(l instanceof CentroidConnector)
        {
            return l.getNumSendingFlow();
        }
        else if(l instanceof CTMLink)
        {
            CTMLink link = (CTMLink)l;
            
            Cell[] cells = link.getCells();
            
            int output = 0;
            
            for(int i = cells.length-1; i>=0; i--)
            {
                output += cells[i].getOccupancy();
                if(cells[i].isCongested())
                {
                    break;
                }
            }
            
            if(cells[0].isCongested())
            {
                for(Link inc : l.getSource().getIncoming())
                {
                    output += calculatePressureBack(inc, l);
                }
            }
            
            return output;
        }
        else if(l instanceof LTMLink)
        {
            return l.getNumSendingFlow();
        }
        else
        {
            return 0;
        }
    }
    
    private int calculatePressureBack(Link l, Link used)
    {
        if(l.lastLinkCheck == used.getId())
        {
            return 0;
        }
        
        l.lastLinkCheck = used.getId();
        
        if(l instanceof CentroidConnector)
        {
            int output = 0;
            
            for(Vehicle v : l.getSendingFlow())
            {
                if(v.getPath().contains(l))
                {
                    output++;
                }
            }
            
            return output;
        }
        else if(l instanceof CTMLink)
        {
            CTMLink link = (CTMLink)l;
            
            Cell[] cells = link.getCells();
            
            int output = 0;
            
            for(int i = cells.length-1; i>=0; i--)
            {
                for(Vehicle v : cells[i].getOccupants())
                {
                    if(v.getPath().contains(used))
                    {
                        output++;
                    }
                }
                
                if(cells[i].isCongested())
                {
                    break;
                }
            }
            
            if(cells[0].isCongested())
            {
                for(Link inc : l.getSource().getIncoming())
                {
                    output += calculatePressureBack(inc, used);
                }
            }
            
            return output;
        }
        else
        {
            return 0;
        }
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
        
        double pressure_i = 0, pressure_j = 0;  
        
        double m_ij = Math.min(i.getCapacity(), j.getCapacity());
        
        return (m_ij * (i.pressure - j.pressure));
    }
    /*
    public double value(Vehicle vehicle, TBR node)
    {
        Link i = vehicle.getPrevLink();
        Link j = vehicle.getNextLink();
        
        double pressure_i = 0, pressure_j = 0;
        
        //pressure_i = i.S / ( i.getCapacity()  * Simulator.dt/3600);
        //pressure_i = i.S / i.getNumLanes();
        
        double d_i = 0, d_j = 0;
        
        if(i instanceof CTMLink)
        {
            d_i = ((CTMLink)i).getEffQueueLength();
        }
        else
        {
            d_i = i.getNumSendingFlow();
        }
        
        if(j instanceof CTMLink)
        {
            d_j = ((CTMLink)j).getEffQueueLength();
        }
        else
        {
            d_j = j.getNumSendingFlow();
        }
        
        double C_i = i.getLength()*i.getNumLanes()*i.getJamDensity();
        double C_j = j.getLength()*j.getNumLanes()*j.getJamDensity();
        
        double m_ij = Math.min(i.getCapacity(), j.getCapacity());
        
        pressure_i = d_i + 1/(C_i-d_i);
        pressure_j = d_j + 1/(C_j-d_j);
        //pressure_i = d_i;
        //pressure_j = d_j;
        
        return (m_ij * (pressure_i - pressure_j));
    }
    */
    
    /**
     * Returns the type code associated with the {@link BackPressureObj}
     * @return {@link ReadNetwork#PRESSURE}
     */
    public int getType()
    {
        return ReadNetwork.PRESSURE;
    }
}
