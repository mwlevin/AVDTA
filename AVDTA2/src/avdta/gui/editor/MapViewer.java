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
import static org.openstreetmap.gui.jmapviewer.JMapViewer.MIN_ZOOM;
import org.openstreetmap.gui.jmapviewer.MemoryTileCache;
import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileController;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoaderListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
 *
 * @author ml26893
 */
public class MapViewer extends JComponent implements Scrollable, MouseWheelListener, MouseListener, MouseMotionListener, TileLoaderListener
{
    private double minX, maxX, minY, maxY, xwidth, ywidth;
    
    private HashMap<Integer, Node> nodes;
    private HashMap<Integer, Link> links;
    
    private DisplayManager display;
    
    private int viewWidth, viewHeight;
    
    private int mouseX, mouseY;
    
    private JScrollPane scrollPane;
    
    private int zoom;
    
    private static final int MIN_ZOOM = 0;
    private static final int MAX_ZOOM = 22;
    /**
     * Vectors for clock-wise tile painting
     */
    private static final Point[] move = {new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1)};
    
    private TileController tileController;
    private TileSource tileSource;
    
    private boolean osmEnabled;
    private Point center;
    
    public MapViewer(DisplayManager display, int viewWidth, int viewHeight)
    {
        this.display = display;
        
        zoom = 1;
        
        nodes = new HashMap<Integer, Node>();
        links = new HashMap<Integer, Link>();
        setRange(-10, 10, -10, 10);
        
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
        
        osmEnabled = false;
        
        
        tileSource = new OsmTileSource.Mapnik();
        tileController = new TileController(tileSource, new MemoryTileCache(), this);
        
        
        setPreferredSize(new Dimension(viewWidth, viewHeight));
        
        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        
    }
    
    public MapViewer(DisplayManager display, int viewWidth, int viewHeight, Network network)
    {
        this(display, viewWidth, viewHeight);
        setNetwork(network);
    }
    
    public void setScrollPane(JScrollPane scrollPane)
    {
        this.scrollPane = scrollPane;
    }
    
    public void setOSMEnabled(boolean o)
    {
        osmEnabled = o;
    }
    
    public boolean isOSMEnabled()
    {
        return osmEnabled;
    }
    
    public void tileLoadingFinished(Tile tile, boolean success) {
        tile.setLoaded(success);
        repaint();
    }
    
    public void mouseMoved(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    

    public void mouseDragged(MouseEvent e)
    {
        if(e.getButton() != MouseEvent.BUTTON3)
        {
            return;
        }
        
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
        if(e.getButton() != MouseEvent.BUTTON3)
        {
            return;
        }
        
        mouseX = e.getX();
        mouseY = e.getY();
        
    }
    
    
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        int rotation = -e.getWheelRotation();
        
        
        
        setZoom(zoom + rotation);
    }
    
    public void setZoom(int zoom_param)
    {
        zoom = (int)Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom_param));
        
        double scale = Math.pow(1.2, zoom);
        
        setPreferredSize(new Dimension((int)Math.round(viewWidth * scale), (int)Math.round(viewHeight * scale)));
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
        
        int width = getWidth();
        int height = getHeight();
        
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        
        g.setColor(Color.black);
        g.drawRect(0, 0, width, height);
        
        // drawing OSM
        if(osmEnabled)
        {
            int iMove = 0;

            int tilesize = tileSource.getTileSize();
            int tilex = center.x / tilesize;
            int tiley = center.y / tilesize;
            int offsx = center.x % tilesize;
            int offsy = center.y % tilesize;

            int w2 = getWidth() / 2;
            int h2 = getHeight() / 2;
            int posx = w2 - offsx;
            int posy = h2 - offsy;

            int diffLeft = offsx;
            int diffRight = tilesize - offsx;
            int diffTop = offsy;
            int diffBottom = tilesize - offsy;

            boolean startLeft = diffLeft < diffRight;
            boolean startTop = diffTop < diffBottom;

            
            int xMin = -tilesize;
            int yMin = -tilesize;
            int xMax = width;
            int yMax = height;
            
            boolean painted = true;
            int x = 0;
            while (painted) {
                painted = false;
                for (int i = 0; i < 4; i++) {
                    if (i % 2 == 0) {
                        x++;
                    }
                    for (int j = 0; j < x; j++) {
                        if (xMin <= posx && posx <= xMax && yMin <= posy && posy <= yMax) {
                            // tile is visible
                            Tile tile = tileController.getTile(tilex, tiley, zoom);
                            
                            if (tile != null) {
                                tile.paint(g, posx, posy, tilesize, tilesize);

                            }
                            painted = true;
                        }
                        Point p = move[iMove];
                        posx += p.x * tilesize;
                        posy += p.y * tilesize;
                        tilex += p.x;
                        tiley += p.y;
                    }
                    iMove = (iMove + 1) % move.length;
                }
            }
        }
        
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
                
                int nwidth = display.getWidth(n, this);
                
                if(width > 0)
                {
                    g.setColor(display.getColor(n, this));
                    g.fillOval(c.getX() - nwidth/2, c.getY() - nwidth/2, nwidth, nwidth);
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
    
    public void setDisplayPosition(Point mapPoint, int x, int y) 
    {
        // Get the plain tile number
        Point p = new Point();
        p.x = x - mapPoint.x + getWidth() / 2;
        p.y = y - mapPoint.y + getHeight() / 2;
        center = p;
    }
}
