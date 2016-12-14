/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.ReadNetwork;
import java.util.Scanner;

/**
 * This class is used to represent and manipulate the data associated with a {@link Node}. 
 * @author Michael
 */
public class NodeRecord 
{
    private int id, type;
    private double longitude, latitude, elevation;
    

    /**
     * 
     * @param id the unique id of the {@link Node}
     * @param type the type code
     * @param lng the longitude
     * @param lat the latitude
     * @param elev the elevation
     */
    public NodeRecord(int id, int type, double lng, double lat, double elev)
    {
        this.id = (int)Math.abs(id);
        this.type = type;
        this.longitude = lng;
        this.latitude = lat;
        this.elevation = elevation;
    }

    /**
     * Constructs this {@link NodeRecord} from the specified line of input data
     * @param line input data
     */
    public NodeRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        
        id = chopper.nextInt();
        type = chopper.nextInt();
        longitude = chopper.nextDouble();
        latitude = chopper.nextDouble();
        elevation = chopper.nextDouble();
    }
    
    /**
     * Returns the id
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Updates the id
     * @param id the new id
     */
    public void setId(int id)
    {
        this.id = id;
    }
    
    /**
     * Returns the type.
     * @return the type
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * Returns whether this represents a zone by checking the type against {@link ReadNetwork#CENTROID}.
     * @return whether this represents a zone
     */
    public boolean isZone()
    {
        return type/100 == ReadNetwork.CENTROID/100;
    }
    
    /**
     * Updates the type. The type code is used to specify whether this {@link Node} is a {@link Intersection} or {@link Zone}, and what the {@link IntersectionControl} is
     * @param type the new type
     */
    public void setType(int type)
    {
        this.type = type;
    }
    
    /**
     * Returns the longitude
     * @return the longitude
     */
    public double getLongitude()
    {
        return longitude;
    }
    
    /**
     * Updates the longitude
     * @param lng the new longitude
     */
    public void setLongitude(double lng)
    {
        longitude = lng;
    }
    
    /**
     * Returns the latitude
     * @return the latitude
     */
    public double getLatitude()
    {
        return latitude;
    }
    
    /**
     * Updates the latitude
     * @param lat the new latitude
     */
    public void setLatitude(double lat)
    {
        this.latitude = lat;
    }
    
    /**
     * Returns the elevation
     * @return the elevation
     */
    public double getElevation()
    {
        return elevation;
    }
    
    /**
     * Updates the elevation
     * @param elev the new elevation
     */
    public void setElevation(double elev)
    {
        this.elevation = elev;
    }
    
    /**
     * A {@link String} form that can be written into the data file
     * @return the {@link String} form of the {@link Node} data
     */
    public String toString()
    {
        return id+"\t"+type+"\t"+longitude+"\t"+latitude+"\t"+elevation;
    }
}
