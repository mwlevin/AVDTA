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
public class MapViewer extends JMapViewer
{

    private HashMap<Integer, Node> nodes;
    private HashMap<Integer, Link> links;
    
    private DisplayManager display;
    
    private boolean displayLinks, displayNodes;

    public MapViewer(DisplayManager display, int viewWidth, int viewHeight)
    {
        this.display = display;
        
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        
        nodes = new HashMap<Integer, Node>();
        links = new HashMap<Integer, Link>();
        
        displayLinks = true;
        displayNodes = true;
        
    }
    
    public MapViewer(DisplayManager display, int viewWidth, int viewHeight, Network network)
    {
        this(display, viewWidth, viewHeight);
        setNetwork(network);
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
        nodes.clear();
        links.clear();
        
        for(Node n : net.getNodes())
        {
            nodes.put(n.getId(), n);
        }
        
        for(Link l : net.getLinks())
        {
            links.put(l.getId(), l);
        }
        
        double minX = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE;
        double minY = Integer.MAX_VALUE;
        double maxY = Integer.MIN_VALUE;
        
        for(int id : nodes.keySet())
        {
            Node n = nodes.get(id);
            
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
        
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), new Coordinate(center_y, center_x), 12);


    }
    
    protected void paintComponent(Graphics window) 
    {
        Graphics2D g = (Graphics2D)window;
        
        super.paintComponent(g);
        
        if(displayLinks)
        {
            for(int id : links.keySet())
            {
                Link l = links.get(id);

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
        }
        if(displayNodes)
        {
            for(int id : nodes.keySet())
            {
                Node n = nodes.get(id);
                
                if(n.isVisible())
                {
                    paintMarker(g, n);
                }
            }
        }
    }
    

}
