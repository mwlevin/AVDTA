/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.Network;
import avdta.network.link.Link;
import avdta.network.node.Location;
import avdta.network.node.Node;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.Timer;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import static org.openstreetmap.gui.jmapviewer.JMapViewer.MIN_ZOOM;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
 *
 * @author ml26893
 */
public class MapViewer extends avdta.gui.editor.JMapViewer
{


    private Set<Node> nodes;
    private Set<Link> links;
    
    
    
    private DisplayManager display;
    
    private int time;
    

    public MapViewer(DisplayManager display, int viewWidth, int viewHeight)
    {
        this.display = display;
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        
        nodes = new HashSet<Node>();
        links = new HashSet<Link>();
        
        
    }
    
    public MapViewer(DisplayManager display, int viewWidth, int viewHeight, Network network)
    {
        this(display, viewWidth, viewHeight);
        setNetwork(network);
    }
    
    public void setTime(int t)
    {
        this.time = t;
        repaint();
    }
    
    public void center(Node n)
    {
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), n.getCoordinate(), getZoom());
    }
    
    public void center(Link l)
    {
        Node source = l.getSource();
        Node dest = l.getDest();
        double x = (source.getX() + dest.getX())/2;
        double y = (source.getY() + dest.getY())/2;
        
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), new Coordinate(y, x), getZoom());
    }
    
    public void setNetwork(Network net)
    {
        if(net != null)
        {
            nodes = net.getNodes();
            links = net.getLinks();
        }
        else
        {
            nodes = new HashSet<Node>();
            links = new HashSet<Link>();
        }
    }
    
    
    public void recenter()
    {
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE;
        double maxY = Integer.MIN_VALUE;
        
        for(Node n : nodes)
        {
            if(n.isZone())
            {
                continue;
            }
            
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
        
        double center_x = (maxX + minX)/2;
        double center_y = (maxY + minY)/2;
        
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), new Coordinate(center_y, center_x), 15);


    }
    
    protected void paintComponent(Graphics window) 
    {
        Graphics2D g = (Graphics2D)window;
        
        super.paintComponent(g);
        

        for(Link l : links)
        {
            if(!l.isCentroidConnector() || display.isDisplayCentroids())
            {
                paintLink(g, l);
            }
            
        }

        for(Node n : nodes)
        {
            if(!n.isZone() || display.isDisplayCentroids())
            {
                paintNode(g, n);
            }
        }
    }
    
    protected void paintLink(Graphics2D g, Link l)
    {
        if(!display.isDisplayLinks())
        {
            return;
        }
        
        Location[] coords = l.getCoordinates();

        if(coords.length < 2)
        {
            return;
        }

        g.setColor(display.getColor(l));
        g.setStroke(new BasicStroke(display.getWidth(l)));

        ICoordinate prev = coords[0];
        for(int i = 1; i < coords.length; i++)
        {
            ICoordinate next = coords[i];

            Point p_start = getMapPosition(prev, false);
            Point p_end = getMapPosition(next, false);

            g.draw(new Line2D.Float(p_start, p_end));

            prev = next;
        }
    }
    
    public void paintNode(Graphics2D g, Node n)
    {
        if(!display.isDisplayNodes())
        {
            return;
        }
        
        int sizeH = (int)display.getRadius(n);
        Point position = getMapPosition(n, false);
        int size = sizeH * 2;

        if (display.getBackColor(n) != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(n.getBackColor());
            g.fillOval(position.x - sizeH, position.y - sizeH, size, size);
            g2.setComposite(oldComposite);
        }
        g.setColor(display.getColor(n));
        g.drawOval(position.x - sizeH, position.y - sizeH, size, size);

        paintText(g, n.getName(), position, sizeH);
    
    }
    
    public void paintText(Graphics g, String name, Point position, int radius) {

        if (name != null && g != null && position != null) {
            g.setColor(Color.DARK_GRAY);
            g.setFont(getFont());
            g.drawString(name, position.x+radius+2, position.y+radius);
        }
    }
    
    public void setDisplayManager(DisplayManager display)
    {
        this.display = display;
        repaint();
    }
}
