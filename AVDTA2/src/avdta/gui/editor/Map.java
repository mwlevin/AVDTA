/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.Network;
import avdta.network.link.Link;
import avdta.network.node.Location;
import avdta.network.node.Node;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.Timer;

/**
 *
 * @author ml26893
 */
public class Map extends JComponent implements Scrollable
{
    private double minX, maxX, minY, maxY, xwidth, ywidth;
    
    private Set<Node> nodes;
    private Set<Link> links;
    
    private DisplayManager display;
    
    public Map(DisplayManager display)
    {
        this.display = display;
        
        nodes = new HashSet<Node>();
        links = new HashSet<Link>();
        setRange(-10, 10, -10, 10);
        
        setPreferredSize(new Dimension(1200, 1200));
        
    }
    public Map(DisplayManager display, Network network)
    {
        this(display);
        setNetwork(network);
    }
    
    public Dimension getPreferredScrollableViewportSize()
    {
        return new Dimension(800, 800);
    }
    
    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }
    
    public boolean getScrollableTracksViewportHeight()
    {
        return false;
    }
    
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 50;
    }
    
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction)
    {
        return 50;
    }
    
    
    public void paint(Graphics window)
    {
        
        Graphics2D g = (Graphics2D)window;
        
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        for(Link l : links)
        {
            if(l.isCentroidConnector())
            {
                continue;
            }
            Location[] coords = l.getCoordinates();
            
            if(coords.length < 2)
            {
                continue;
            }
            
            g.setColor(display.getColor(l, this));
            g.setStroke(new BasicStroke(display.getWidth(l, this)));
            
            Coordinate prev = getCoordinate(coords[0]);
            for(int i = 1; i < coords.length; i++)
            {
                Coordinate next = getCoordinate(coords[i]);
                
                if(isValid(prev) && isValid(next))
                {
                    g.draw(new Line2D.Float(prev.getX(), prev.getY(), next.getX(), next.getY()));
                }
                prev = next;
            }
        }
        
        for(Node n : nodes)
        {
            if(n.isZone())
            {
                continue;
            }
            Coordinate c = getCoordinate(n);
            
            if(isValid(c))
            {
                g.setColor(display.getColor(n, this));
                int width = display.getWidth(n, this);

                g.fillOval(c.getX() - width/2, c.getY() - width/2, width, width);
            }
        }
    }
    
    
    public void setNetwork(Network net)
    {
        nodes = net.getNodes();
        links = net.getLinks();
        
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE;
        double maxY = Integer.MIN_VALUE;
        
        for(Node n : nodes)
        {
            if(n.getX() < minX)
            {
                minX = n.getX();
            }
            
            if(n.getX() > maxX)
            {
                maxX = n.getX();
            }
            
            if(n.getY() < minY)
            {
                minY = n.getY();
            }
            
            if(n.getY() > maxY)
            {
                maxY = n.getY();
            }
            
        }
        
        double xdiff = maxX - minX;
        double ydiff = maxY - minY;
        
        minX -= xdiff*0.2;
        maxX += xdiff*0.2;
        
        minY -= ydiff*0.2;
        maxY += ydiff*0.2;

        setRange(minX, maxX, minY, maxY);
    }
    
    public void setRange(double minX, double maxX, double minY, double maxY)
    {
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        
        xwidth = maxX - minX;
        ywidth = maxY - minY;
        
        if(minX >= maxX)
        {
            throw new RuntimeException("minX >= maxX");
        }
        else if(minY >= maxY)
        {
            throw new RuntimeException("minY >= maxY");
        }
    }
    
    public Coordinate getCoordinate(Location loc)
    {
        double x = loc.getX();
        double y = loc.getY();
        
        return new Coordinate((int)Math.round( (x-minX) / xwidth * getWidth()) , (int)Math.round( (y-minY)/ywidth * getHeight()));
    }
    
    public boolean isValid(Coordinate c)
    {
        int x = c.getX();
        int y = c.getY();
        
        return (x >= 0 && x <= getWidth() && y >= 0 && y <= getHeight());
    }
    
    public boolean isValid(Location l)
    {
        double x = l.getX();
        double y = l.getY();
        
        return (x >= minX && x <= maxX && y >= minY && y <= maxY);
    }
}
