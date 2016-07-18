/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import java.util.Scanner;

/**
 *
 * @author Michael
 */
public class NodeRecord 
{
    private int id, type;
    private double longitude, latitude, elevation;
    
    public NodeRecord(int id, int type, double lng, double lat, double elev)
    {
        this.id = id;
        this.type = type;
        this.longitude = longitude;
        this.latitude = latitude;
        this.elevation = elevation;
    }
    
    public NodeRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        
        id = chopper.nextInt();
        type = chopper.nextInt();
        longitude = chopper.nextDouble();
        latitude = chopper.nextDouble();
        elevation = chopper.nextDouble();
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
    
    public double getLongitude()
    {
        return longitude;
    }
    
    public void setLongitude(double lng)
    {
        longitude = lng;
    }
    
    public double getLatitude()
    {
        return latitude;
    }
    
    public void setLatitude(double lat)
    {
        this.latitude = lat;
    }
    
    public double getElevation()
    {
        return elevation;
    }
    
    public void setElevation(double elev)
    {
        this.elevation = elev;
    }
    
    public String toString()
    {
        return id+"\t"+type+"\t"+longitude+"\t"+latitude+"\t"+elevation;
    }
}
