/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.gui.editor.visual.DisplayManager;
import avdta.network.link.TransitLink;
import avdta.network.link.Link;
import avdta.vehicle.DriverType;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.openstreetmap.gui.jmapviewer.Layer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import static org.openstreetmap.gui.jmapviewer.MapMarkerDot.DOT_RADIUS;
import static org.openstreetmap.gui.jmapviewer.MapMarkerDot.getDefaultStyle;
import static org.openstreetmap.gui.jmapviewer.MapObjectImpl.getDefaultFont;
import org.openstreetmap.gui.jmapviewer.Style;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

/**
 * An abstract node in the traffic network. Nodes can be {@link Intersection}s 
 * or {@link Zone}s. <br>
 * {@code CR} stands for conflict regions. <br>
 * {@code SIGNALS} whether an intersection has signal or not. <br>
 * {@code IP} integer program. <br>
 * {@code MCKS} heuristic for IP; multiple constrained knapsack problem. <br>
 * {@code STOP} for stop sign. <br>
 * {@code incoming} and {@code outgoing} for a set of incoming {@link Link}s
 * and outgoing {@link Link}s.
 * @author Michael
 */
public abstract class Node extends Location implements Serializable, Comparable<Node>, MapMarker
{
    private static final Layer NODES = new Layer("Nodes");
    private static final Font NODE_FONT = getDefaultFont();
    private static final Style NODE_STYLE = new Style(Color.BLACK, Color.YELLOW, null, NODE_FONT);
    private static final Style SELECTED_STYLE = new Style(Color.BLACK, Color.RED, null, NODE_FONT);
    private static final Style CENTROID_STYLE = new Style(Color.BLACK, Color.GREEN, null, NODE_FONT);
    
    private boolean selected;
    
    private Set<Link> incoming, outgoing;
    private int id;
    
    
    
    public double label;
    public int arr_time;
    public Link prev;
    public TransitLink transit_prev;
    
    public double vc;
    
    private Set<TransitLink> transitInc, transitOut;
    
    /**
     * Instantiates a node with location (0, 0)
     * @param id A unique id for the node.
     */
    public Node(int id)
    {
        this(id, new Location(0, 0));
    }
    
    /**
     * 
     * @param id A unique id for the node
     * @param loc take {@link Location} as input
     */
    public Node(int id, Location loc)
    {
        super(loc);
        this.id = id;
        
        incoming = new HashSet<Link>();
        outgoing = new HashSet<Link>();

        transitInc = new HashSet<TransitLink>();
        transitOut = new HashSet<TransitLink>();
        
        selected = false;
    }
    
    public boolean isSelected()
    {
        return selected;
    }
    
    public void setSelected(boolean s)
    {
        selected = s;
    }
    
    public STYLE getMarkerStyle() {
        return STYLE.FIXED;
    }
    
    public double getRadius() {
        return 0;
    }
    
    public boolean isVisible()
    {
        return !isZone();
    }
    
    public String getName()
    {
        return ""+getId();
    }
    
    public void setIncoming(Set<Link> inc)
    {
        incoming = inc;
    }
    
    public void setOutgoing(Set<Link> out)
    {
        outgoing = out;
    }
    
    public int compareTo(Node rhs)
    {
        return id - rhs.id;
    }
    
    /**
     * 
     * @return if this node is a centroid
     */
    public boolean isZone()
    {
        return false;
    }
    
    /**
     * 
     * @return if this node uses conflict regions for reservations
     */
    public abstract boolean hasConflictRegions();
    
    /**
     * 
     * @param i An incoming {@link Link} to the intersection.
     * @param j An outgoing {@link Link} from the intersection.
     * @param driver For knowing the {@link DriverType}.
     * @return whether vehicles of type driver can move from i to j across 
     * this node.
     * @see IntersectionControl
     */
    public abstract boolean canMove(Link i, Link j, DriverType driver);
    
    /**
     * Resets this node for a new simulation
     */
    public void reset(){}
    
    /**
     * Initialize this node to start simulating
     */
    public void initialize(){}
    
    public int getId()
    {
        return id;
    }
    

    /**
     * 
     * @return id
     */
    public String toString()
    {
        return ""+id;
    }
    
    public NodeRecord createNodeRecord()
    {
        return new NodeRecord(getId(), getType(), getX(), getY(), getElevation());
    }
    
    
    public int hashCode()
    {
        return id;
    }
    
    public abstract int step();
    
    /**
     * Updates incoming or outgoing links, appropriately
     * @param l {@link Link} to be updated.
     */
    public void addLink(Link l)
    {
        if(l.getSource() == this)
        {
            outgoing.add(l);
        }
        else if(l.getDest() == this)
        {
            incoming.add(l);
        }
    }
    
    /**
     * Updates incoming or outgoing links, appropriately
     * @param l {@link TransitLink} to be updated.
     */
    public void addLink(TransitLink l)
    {
        if(l.getSource() == this)
        {
            transitOut.add(l);
        }
        else if(l.getDest() == this)
        {
            transitInc.add(l);
        }
    }
    
    /**
     * 
     * @return incoming links
     */
    public Set<Link> getIncoming()
    {
        return incoming;
    }
    
    /**
     * 
     * @return outgoing links
     */
    public Set<Link> getOutgoing()
    {
        return outgoing;
    }
    
    /**
     * 
     * @return incoming transit links
     */
    public Set<TransitLink> getTransitInc()
    {
        return transitInc;
    }
    
    public abstract int getType();
    /**
     * 
     * @return outgoing transit links
     */
    public Set<TransitLink> getTransitOut()
    {
        return transitOut;
    }
    
    public abstract Signalized getSignal();
    
    public void paint(Graphics g, Point position, int radius) {
        
        int sizeH = radius;
        int size = sizeH * 2;

        if (g instanceof Graphics2D && getBackColor() != null) {
            Graphics2D g2 = (Graphics2D) g;
            Composite oldComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2.setPaint(getBackColor());
            g.fillOval(position.x - sizeH, position.y - sizeH, size, size);
            g2.setComposite(oldComposite);
        }
        g.setColor(getColor());
        g.drawOval(position.x - sizeH, position.y - sizeH, size, size);

        if (getLayer() == null || getLayer().isVisibleTexts()) paintText(g, position);
    }
    
    public void paintText(Graphics g, Point position) {
        String name = getName();
        if (name != null && g != null && position != null) {
            g.setColor(Color.DARK_GRAY);
            g.setFont(getFont());
            g.drawString(name, position.x+MapMarkerDot.DOT_RADIUS+2, position.y+MapMarkerDot.DOT_RADIUS);
        }
    }
    
    public Font getFont()
    {
        return NODE_FONT;
    }
    
    public Layer getLayer()
    {
        return NODES;
    }

    public void setLayer(Layer layer){}

    public Style getStyle()
    {
        return NODE_STYLE;
    }

    public Style getStyleAssigned()
    {
        return isSelected()? SELECTED_STYLE : isZone()? CENTROID_STYLE : NODE_STYLE;
    }

    public Color getColor()
    {
        return getStyleAssigned().getColor();
    }

    public Color getBackColor()
    {
        return getStyleAssigned().getBackColor();
    }

    public Stroke getStroke()
    {
        return getStyleAssigned().getStroke();
    }
}
