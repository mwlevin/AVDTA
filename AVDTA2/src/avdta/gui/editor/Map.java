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

/**
 *
 * @author ml26893
 */
public class Map extends JComponent implements Scrollable, MouseWheelListener, MouseListener, MouseMotionListener
{
    private double minX, maxX, minY, maxY, xwidth, ywidth;
    
    private HashMap<Integer, Node> nodes;
    private HashMap<Integer, Link> links;
    
    private DisplayManager display;
    
    private int viewWidth, viewHeight;
    
    private int mouseX, mouseY;
    
    private JScrollPane scrollPane;
    
    public Map(DisplayManager display, int viewWidth, int viewHeight)
    {
        this.display = display;
        
        nodes = new HashMap<Integer, Node>();
        links = new HashMap<Integer, Link>();
        setRange(-10, 10, -10, 10);
        
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        
        
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        
    }
    
    public Map(DisplayManager display, int viewWidth, int viewHeight, Network network)
    {
        this(display, viewWidth, viewHeight);
        setNetwork(network);
    }
    
    public void setScrollPane(JScrollPane scrollPane)
    {
        this.scrollPane = scrollPane;
    }
    
    
    public void mouseMoved(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    

    public void mouseDragged(MouseEvent e)
    {
        int xDiff = e.getX() - mouseX;
        int yDiff = e.getY() - mouseY;
        
        if(Math.sqrt(xDiff*xDiff + yDiff*yDiff) < 30)
        {
            return;
        }
        
        mouseX = e.getX();
        mouseY = e.getY();
        
        xDiff = -xDiff;
        yDiff = -yDiff;
        

        Point point = scrollPane.getViewport().getViewPosition();
        

        int newX = (int)Math.min(Math.max(0, point.getX()+xDiff), getWidth()-viewWidth);
        int newY = (int)Math.min(Math.max(0, point.getY()+yDiff), getHeight()-viewHeight);

        scrollPane.getViewport().setViewPosition(new Point(newX, newY));
    }
    
    public void mouseClicked(MouseEvent e){}
    
    public void mousePressed(MouseEvent e)
    {
        mouseX = e.getX();
        mouseY = e.getY();
        
    }
    
    
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        int scale = -e.getWheelRotation();

        int width = getWidth();
        int height = getHeight();
        
        width = (int)Math.max(viewWidth, Math.round(width * Math.pow(1.2, scale)));
        height = (int)Math.max(viewHeight, Math.round(height * Math.pow(1.2, scale)));
        
        setPreferredSize(new Dimension(width, height));
        revalidate();


    }
    
    public Dimension getPreferredScrollableViewportSize()
    {
        return new Dimension(viewWidth, viewHeight);
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
    
    
    public void paint(Graphics g)
    {
        Graphics offgc;
        Image offscreen = null;
        Dimension d = getSize();

        // create the offscreen buffer and associated Graphics
        offscreen = createImage(d.width, d.height);
        offgc = offscreen.getGraphics();
        // clear the exposed area

        // do normal redraw
        paint_help(offgc);
        // transfer offscreen to window
        g.drawImage(offscreen, 0, 0, this);
    }
    
    public void paint_help(Graphics window)
    {
        
        Graphics2D g = (Graphics2D)window;
        
        g.setColor(Color.white);
        g.fillRect(0, 0, getWidth(), getHeight());
        
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
        
        for(int id : nodes.keySet())
        {
            Node n = nodes.get(id);
            
            if(n.isZone())
            {
                continue;
            }
            Coordinate c = getCoordinate(n);
            
            if(isValid(c))
            {
                
                int width = display.getWidth(n, this);
                
                if(width > 0)
                {
                    g.setColor(display.getColor(n, this));
                    g.fillOval(c.getX() - width/2, c.getY() - width/2, width, width);
                }
            }
        }
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
