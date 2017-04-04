/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.gui.editor.visual.DisplayManager;
import avdta.network.Network;
import avdta.network.link.Link;
import avdta.network.link.TransitLane;
import avdta.network.node.Location;
import avdta.network.node.Node;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
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
public class MapViewer extends avdta.gui.editor.JMapViewer implements MouseListener, MouseMotionListener
{
    private static final Point[] move = {new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};

    private Set<Node> nodes;
    private Set<Link> links;
    
    
    
    private DisplayManager display;
    
    private int time;
    
    private int scale;
    
    private Location mouseStart, mouseEnd;
    private boolean drawSelection;
    
    public MapViewer(DisplayManager display, int viewWidth, int viewHeight)
    {
        this.display = display;
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        
        nodes = new HashSet<Node>();
        links = new HashSet<Link>();
        
        scale = 1;
        drawSelection = false;
        
        setFont(new Font("Arial", Font.BOLD, 14));
        
        addMouseListener(this);
        addMouseMotionListener(this);
    }
    
    public MapViewer(DisplayManager display, int viewWidth, int viewHeight, Network network)
    {
        this(display, viewWidth, viewHeight);
        setNetwork(network);
    }
    
    public void mousePressed(MouseEvent e)
    {
        if(e.getButton() == MouseEvent.BUTTON1)
        {
            Location loc = new Location(getPosition(e.getX(), e.getY()));
            mouseStart = loc;
            mouseEnd = loc;
            
            drawSelection = true;

            repaint();
        }
    }
    
    public void mouseReleased(MouseEvent e)
    {
        if(e.getButton() == MouseEvent.BUTTON1)
        {
            drawSelection = false;
            
            // select nodes and links
            
            double xmin = Math.min(mouseStart.getX(), mouseEnd.getX());
            double ymin = Math.min(mouseStart.getY(), mouseEnd.getY());
            double xmax = Math.max(mouseStart.getX(), mouseEnd.getX());
            double ymax = Math.max(mouseStart.getY(), mouseEnd.getY());
            
            for(Node n : nodes)
            {
                if(n.getX() >= xmin && n.getX() <= xmax &&
                        n.getY() >= ymin && n.getY() <= ymax)
                {
                    n.setSelected(true);
                }
            }
            
            for(Link l : links)
            {
                Node r = l.getSource();
                Node s = l.getDest();
                
                if(r.getX() >= xmin && r.getX() <= xmax &&
                        r.getY() >= ymin && r.getY() <= ymax &&
                        s.getX() >= xmin && s.getX() <= xmax &&
                        s.getY() >= ymin && s.getY() <= ymax)
                {
                    l.setSelected(true);
                }
            }

            repaint();
        }
    }
    
    public void mouseDragged(MouseEvent e)
    {

        mouseEnd = new Location(getPosition(e.getX(), e.getY()));


        repaint();
        
    }
    
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void mouseMoved(MouseEvent e){}
    
    public void setScale(int scale)
    {
        this.scale = scale;
    }
    
    public void setTime(int t)
    {
        this.time = t;
        repaint();
    }
    
    public int getTime()
    {
        return time;
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
        recenter(getZoom());
    }
    public void recenter(int zoom)
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
        
        setDisplayPosition(new Point(getWidth()/2, getHeight()/2), new Coordinate(center_y, center_x), zoom);
        repaint();
    }
    
    public void setZoomControlsVisible(boolean visible) {
        super.setZoomContolsVisible(visible);
    }
    
    protected void paintComponent(Graphics window) 
    {
        Graphics2D g = (Graphics2D)window;
        
        super.paintComponent(g);

        // display non-special links first
        for(Link l : links)
        {
            if(display.hasSpecialDisplay(l, time))
            {
                continue;
            }
            if(!(l instanceof TransitLane) && 
                    (!l.isCentroidConnector() || display.isDisplayCentroids()) )
            {
                paintLink(g, l);
            } 
        }
        
        for(Link l : links)
        {
            if(!display.hasSpecialDisplay(l, time))
            {
                continue;
            }
            if(!(l instanceof TransitLane) && 
                    (!l.isCentroidConnector() || display.isDisplayCentroids()) )
            {
                paintLink(g, l);
            } 
        }

        for(Node n : nodes)
        {
            if( (n.isZone() && display.isDisplayCentroids()) || 
                    (!n.isZone() && display.isDisplayNonCentroids()))
            {
                paintNode(g, n);
            }
        }
        
        if(drawSelection)
        {
            Point start = this.getMapPosition(mouseStart.getLat(), mouseStart.getLon());
            Point end = getMapPosition(mouseEnd.getLat(), mouseEnd.getLon());
            int x = (int)Math.min(start.getX(), end.getX());
            int y = (int)Math.min(start.getY(), end.getY());
            int dx = (int)Math.abs(end.getX() - start.getX());
            int dy = (int)Math.abs(end.getY() - start.getY());

            Stroke dashed = new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
            g.setStroke(dashed);
            g.setColor(Color.DARK_GRAY);

            g.drawRect(x, y, dx, dy);
        }
    }
    
    private static final double shift_r = 0.00005;
    
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

        g.setColor(display.getColor(l, time));
        
        
        g.setStroke(new BasicStroke(display.getWidth(l, time) * scale));

        double angle = l.getDirection() - Math.PI/2;
        
        Location shift = new Location(shift_r * Math.cos(angle), shift_r * Math.sin(angle));
        
        Location prev = coords[0];
        for(int i = 1; i < coords.length; i++)
        {
            Location next = coords[i];

            Point p_start = getMapPosition(prev.add(shift), false);
            Point p_end = getMapPosition(next.add(shift), false);

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
        
        if(n.getId() < 0)
        {
            return;
        }
        
        int sizeH = (int)display.getRadius(n, time) * scale;
        Point position = getMapPosition(n, false);
        int size = sizeH * 2;

        Color color = display.getBackColor(n, time);
        if (color != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(color);
            g.fillOval(position.x - sizeH, position.y - sizeH, size, size);
            g2.setComposite(oldComposite);
        }
        g.setColor(display.getColor(n, time));
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
