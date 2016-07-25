/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import java.util.Scanner;


/**
 *
 * @author ml26893
 */
public class LinkRecord 
{

    private int id, source, dest, numLanes, type;
    private double capacity, ffspd, wavespd, length;
    
    // id\ttype\tsource\tdest\tlength (ft)\tffspd (mph)\tw (mph)\tcapacity\tnum_lanes
    public LinkRecord(int id, int type, int source, int dest, double length, double ffspd, double wavespd, double capacity, int numLanes)
    {
        this.id = id;
        this.type = type;
        this.source = source;
        this.dest = dest;
        this.capacity = capacity;
        this.ffspd = ffspd;
        this.wavespd = wavespd;
        this.length = length;
        this.numLanes = numLanes;
    }
    
    public LinkRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        
        id = chopper.nextInt();
        type = chopper.nextInt();
        source = chopper.nextInt();
        dest = chopper.nextInt();
        length = chopper.nextDouble();
        ffspd = chopper.nextDouble();
        wavespd = chopper.nextDouble();
        capacity = chopper.nextDouble();
        numLanes = chopper.nextInt();
    }
    
    public String toString()
    {
        return id+"\t"+type+"\t"+source+"\t"+dest+"\t"+length+"\t"+ffspd+"\t"+wavespd+"\t"+capacity+"\t"+numLanes;
    }
    
    public void setWavespd(double wavespd)
    {
        this.wavespd = wavespd;
    }
    
    public double getWavespd()
    {
        return wavespd;
    }
    
    public void setLength(double length)
    {
        this.length = length;
    }
    
    public double getLength()
    {
        return length;
    }
    
    public void setCapacity(double capacity)
    {
        this.capacity = capacity;
    }
    
    public double getCapacity()
    {
        return capacity;
    }
    
    public void setFFSpd(double ffspd)
    {
        this.ffspd = ffspd;
    }
    
    public double getFFSpd()
    {
        return ffspd;
    }
    
    public void setNumLanes(int numLanes)
    {
        this.numLanes = numLanes;
    }
    
    public int getNumLanes()
    {
        return numLanes;
    }
    
    public int getId()
    {
        return id;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    public int getType()
    {
        return type;
    }
    
    public void setType(int type)
    {
        this.type = type;
    }
    
    public int getSource()
    {
        return source;
    }
    
    public void setSource(int source)
    {
        this.source = source;
    }
    
    public void setDest(int dest)
    {
        this.dest = dest;
    }
}
