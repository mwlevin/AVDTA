/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import java.io.Serializable;
import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * This class stores the <b>location</b> and <b>elevation</b> of a node. <br>
 * {@code x} Stores the <b>abscissa</b> of the location of a node. <br>
 * {@code y} Stores the <b>ordinate</b> of the location of a node. <br>
 * {@code elevation} Stores the <b>elevation</b> of a node.
 * @see Node
 * @author Michael
 */
public class Location implements Serializable, ICoordinate
{
    private double x, y;
    private double elevation;
    /**
     * Instantiates the location of a node with (x, y).
     * @param x Input for abscissa of the location of a node.
     * @param y Input for ordinate of the location of a node.
     */
    public Location(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
    /**
     * Instantiates the location of a node with ({@code rhs.x}, {@code rhs.y})
     * @param rhs Input for location coordinates of type {@link Location}.
     */
    public Location(Location rhs)
    {
        this(rhs.x, rhs.y);
    }
    
    public Location(ICoordinate rhs)
    {
        this(rhs.getLon(), rhs.getLat());
    }
    
    public double angleTo(Location rhs)
    {
        double output = Math.atan2(rhs.getY() - getY(), rhs.getX() - getX());
        
        if(output < 0)
        {
            output += 2*Math.PI;
        }
        
        return output;
    }
    
    public double distanceTo(Location rhs)
    {
        return Math.sqrt((rhs.x - x) * (rhs.x - x) + (rhs.y - y) * (rhs.y - y));
    }
    
    public Coordinate getCoordinate()
    {
        return new Coordinate(y, x);
    }
    /**
     * Gets the elevation of a {@link Node}.
     * @return A double value for the elevation.
     */
    public double getElevation()
    {
        return elevation;
    }
    /**
     * Sets the elevation of a {@link Node}.
     * @param h Input representing elevation.
     */
    public void setElevation(double h)
    {
        elevation = h;
    }
    /**
     * Returns the abscissa of the location of the node.
     * @return A double representing the abscissa of the location of the node.
     */
    public double getX()
    {
        return x;
    }
    /**
     * Returns the ordinate of the location of the node.
     * @return A double value representing the ordinate of the node.
     */
    public double getY()
    {
        return y;
    }
    /**
     * Sets the abscissa of the location of the node.
     * @param x A double value representing the abscissa of the location of a node.
     */
    public void setX(double x)
    {
        this.x = x;
    }
    /**
     * Sets the ordinate of the location of the node.
     * @param y A double value representing the ordinate of the location of a node.
     */
    public void setY(double y)
    {
        this.y = y;
    }
    
    public double getLat()
    {
        return getY();
    }

    public void setLat(double lat)
    {
        setY(lat);
    }

    public double getLon()
    {
        return getX();
    }

    public void setLon(double lon)
    {
        setX(lon);
    }
}
