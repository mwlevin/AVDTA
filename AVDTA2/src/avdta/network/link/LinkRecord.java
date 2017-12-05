/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.ReadNetwork;
import java.util.Scanner;
import avdta.network.node.Node;
import avdta.network.type.Type;


/**
 * This class represents and manipulates the data used to construct {@link Link}s
 * @author Michael
 */
public class LinkRecord 
{

    private int id, source, dest, numLanes, type;
    private double capacity, ffspd, wavespd, length;
    
    // id\ttype\tsource\tdest\tlength (ft)\tffspd (mph)\tw (mph)\tcapacity\tnum_lanes
    /**
     * Constructs this {@link LinkRecord} with the given parameters
     * @param id the id of the {@link Link}
     * @param type the type code
     * @param source the id of the upstream {@link Node}
     * @param dest the id of the downstream {@link Node}
     * @param length the length (mi)
     * @param ffspd the free flow speed (mi/hr) 
     * @param wavespd the congested wave speed (mi/hr)
     * @param capacity the capacity (veh/hr)
     * @param numLanes the number of lanes
     */
    public LinkRecord(int id, int type, int source, int dest, double length, double ffspd, double wavespd, double capacity, int numLanes)
    {
        this.id = id;
        this.type = type;
        this.source = (int)Math.abs(source);
        this.dest = (int)Math.abs(dest);
        this.capacity = capacity;
        this.ffspd = ffspd;
        this.wavespd = wavespd;
        this.length = length;
        this.numLanes = numLanes;
    }
    
    /**
     * Constructs a clone of this {@link LinkRecord}
     * @return a clone
     */
    public LinkRecord clone()
    {
        return new LinkRecord(id, type, source, dest, length, ffspd, wavespd, capacity, numLanes);
    }
    
    /**
     * Constructs a {@link LinkRecord} from the line of input data
     * @param line the line of input data
     */
    public LinkRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        
        id = chopper.nextInt();
        type = chopper.nextInt();
        source = chopper.nextInt();
        dest = chopper.nextInt();
        length = chopper.nextDouble()/5280;
        ffspd = chopper.nextDouble();
        wavespd = chopper.nextDouble();
        capacity = chopper.nextDouble();
        numLanes = chopper.nextInt();
    }
    
    /**
     * A {@link String} representation that can be written to the data file
     * @return a {@link String} representation
     */
    public String toString()
    {
        return id+"\t"+type+"\t"+source+"\t"+dest+"\t"+(length*5280)+"\t"+ffspd+"\t"+wavespd+"\t"+capacity+"\t"+numLanes;
    }
    
    /**
     * Updates the congested wave speed
     * @param wavespd the new congested wave speed (mi/hr)
     */
    public void setWavespd(double wavespd)
    {
        this.wavespd = wavespd;
    }
    
    /**
     * Returns the congested wave speed
     * @return the congested wave speed (mi/hr)
     */
    public double getWaveSpd()
    {
        return wavespd;
    }

    /**
     * Returns if this link represents a centroid connector by comparing the type with {@link ReadNetwork#CENTROID}.
     * @return if this link represents a centroid connector.
     */
    public boolean isCentroidConnector()
    {
        return type/100 == ReadNetwork.CENTROID.getCode()/100;
    }
    
    /**
     * Updates the length
     * @param length the new length (mi)
     */
    public void setLength(double length)
    {
        this.length = length;
    }
    
    /**
     * Returns the length
     * @return the length (mi)
     */
    public double getLength()
    {
        return length;
    }
    
    /**
     * Updates the capacity
     * @param capacity the new capacity (veh/hr)
     */
    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }
    
    /**
     * Returns the capacity
     * @return the capacity (veh/hr)
     */
    public double getCapacity()
    {
        return capacity;
    }
    
    /**
     * Updates the free flow speed
     * @param ffspd the new free flow speed (mi/hr)
     */
    public void setFFSpd(double ffspd)
    {
        this.ffspd = ffspd;
    }
    
    /**
     * Returns the free flow speed
     * @return the free flow speed (mi/hr)
     */
    public double getFFSpd()
    {
        return ffspd;
    }
    
    /**
     * Updates the number of lanes
     * @param numLanes the new number of lanes
     */
    public void setNumLanes(int numLanes)
    {
        this.numLanes = numLanes;
    }
    
    /**
     * Returns the number of lanes
     * @return the number of lanes
     */
    public int getNumLanes()
    {
        return numLanes;
    }
    
    /**
     * Returns the id of the {@link Link}
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Updates the id of the {@link Link}
     * @param id new id
     */
    public void setId(int id)
    {
        this.id = id;
    }
    
    /**
     * Returns the type code
     * @return the type code
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * Updates the type code
     * @param type the new type code
     */
    public void setType(int type)
    {
        this.type = type;
    }
    
    public void setType(Type type)
    {
        this.type = type.getCode();
    }
    
    /**
     * Returns the id of the upstream {@link Node}
     * @return the id of the upstream {@link Node}
     */
    public int getSource()
    {
        return source;
    }
    /**
     * Returns the id of the downstream {@link Node}
     * @return the id of the downstream {@link Node}
     */
    public int getDest()
    {
        return dest;
    }
    
    /**
     * Updates the id of the upstream {@link Node}
     * @param source the id of the new upstream {@link Node}
     */
    public void setSource(int source)
    {
        this.source = source;
    }
    
    /**
     * Updates the id of the downstream {@link Node}
     * @param dest the id of the new downstream {@link Node}
     */
    public void setDest(int dest)
    {
        this.dest = dest;
    }
}
