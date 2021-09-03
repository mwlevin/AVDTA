/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.duer.VMS;
import avdta.gui.editor.visual.DisplayManager;
import avdta.network.node.Intersection;
import avdta.network.node.Zone;
import avdta.network.link.transit.TransitLink;
import avdta.network.link.Link;
import avdta.network.type.Type;
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
    
    
    
     private boolean SPaT;

    
    public double label;
    public int arr_time;
    public Link prev;
    
    
    public TransitLink transit_prev;
    public boolean added, settled;
    
    public double vc;
    
    
    
    private Set<TransitLink> transitInc, transitOut;
    
    /**
     * Instantiates a {@link Node} with {@link Location} (0, 0)
     * @param id A unique id for the node.
     */
    public Node(int id)
    {
        this(id, new Location(0, 0));
    }
    
        /**
     * Instantiates a {@link Node} with {@link Location} (0, 0)
     * @param id A unique id for the node.
     */
    public Node(int id, boolean sp)
    {
        this(id, new Location(0, 0), sp);
    }
    
    /**
     * 
     * @param id A unique id for this {@link Node}
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
        SPaT = false;
    }
    
     /**
     * 
     * @param id A unique id for this {@link Node}
     * @param loc take {@link Location} as input
     * @param SP indicates if this is a SPaT node
     */
    public Node(int id, Location loc, boolean SP)
    {
        super(loc);
        this.id = id;
        this.SPaT = SP;
                
        incoming = new HashSet<Link>();
        outgoing = new HashSet<Link>();

        transitInc = new HashSet<TransitLink>();
        transitOut = new HashSet<TransitLink>();
        
        selected = false;
        
    }
    
    
    
    
    public void prepare(){}
    
    
    /**
     * Checks whether this {@link Node} is selected for visualization
     * @return whether this {@link Node} is selected
     */
    public boolean isSelected()
    {
        return selected;
    }
    
    /**
     * Updates whether this {@link Node} is selected for visualization
     * @param s whether this {@link Node} should be selected
     */
    public void setSelected(boolean s)
    {
        selected = s;
    }
    
    /**
     * Returns the marker {@link Style} for visualization
     * @return STYLE.FIXED
     */
    public STYLE getMarkerStyle() {
        return STYLE.FIXED;
    }
    
    /**
     * Returns the radius for visualization
     * @return 0 by default 
     */
    public double getRadius() {
        return 0;
    }
    
    /**
     * Returns if this node is SPaT enabled
     * @return 
     */
    public boolean getSPaT(){
        return SPaT;
    }

    /**
     * Sets the SPaT status of the node
     * @param SP SPaT node?
     */
    public void setSPaT(boolean SP){
        this.SPaT = SP;
    }
    
    /**
     * Returns whether this {@link Node} is drawn during visualization
     * @return whether this {@link Node} is a {@link Zone}
     */
    public boolean isVisible()
    {
        return !isZone();
    }
    
    /**
     * Returns a {@link String} containing the id
     * @return a {@link String} containing the id
     */
    public String getName()
    {
        return ""+getId();
    }
    
    /**
     * Updates the set of incoming {@link Link}s
     * @param inc the new set of incoming {@link Link}s
     */
    public void setIncoming(Set<Link> inc)
    {
        incoming = inc;
    }
    
    /**
     * Updates the set of outgoing {@link Link}s
     * @param out the new set of outgoing {@link Link}s
     */
    public void setOutgoing(Set<Link> out)
    {
        outgoing = out;
    }
    
    /**
     * Orders nodes based on id
     * @param rhs the node being compared to
     * @return order based on node id
     */
    public int compareTo(Node rhs)
    {
        return id - rhs.id;
    }
    
    /**
     * Returns whether two nodes are equal
     * @param o the node being compared to
     * @return if the ids match
     */
    public boolean equals(Object o)
    {
        Node rhs = (Node)o;
        
        return rhs.id == id;
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
     * @return if this {@link Node} uses conflict regions for reservations
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
    
    /**
     * Returns the id of this {@link Node}
     * @return the id of this {@link Node}
     */
    public int getId()
    {
        return id;
    }
    

    /**
     * Returns a {@link String} containing the id
     * @return id
     */
    public String toString()
    {
        return ""+id;
    }
    
    /**
     * Creates a {@link NodeRecord} representation of this {@link Node}
     * @return a {@link NodeRecord} representation of this {@link Node}
     */
    public NodeRecord createNodeRecord()
    {
        return new NodeRecord(getId(), getType().getCode(), getX(), getY(), getElevation());
    }
    
    
    public int hashCode()
    {
        return id;
    }
    
    /**
     * Execute one time step for this node
     * @return the number of exiting vehicles
     */
    public abstract int step();
    
    /**
     * Updates incoming or outgoing {@link Link}s, appropriately
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
     * Updates incoming or outgoing {@link Link}s, appropriately
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
     * Returns the set of incoming {@link Link}s
     * @return incoming links
     */
    public Set<Link> getIncoming()
    {
        return incoming;
    }
    
    /**
     * Returns the set of outgoing {@link Link}s
     * @return outgoing links
     */
    public Set<Link> getOutgoing()
    {
        return outgoing;
    }
    
    /**
     * Returns the set of incoming {@link TransitLink}s. {@link TransitLink}s are used to move travelers virtually via transit or walking from transit stops to their destination.
     * @return incoming transit links
     * @see TransitLink
     */
    public Set<TransitLink> getTransitInc()
    {
        return transitInc;
    }
    
    public abstract Type getType();
    
    /**
     * Returns the set of outgoing {@link TransitLink}s. {@link TransitLink}s are used to move travelers virtually via transit or walking from transit stops to their destination.
     * @return outgoing transit links
     * @see TransitLink
     */
    public Set<TransitLink> getTransitOut()
    {
        return transitOut;
    }
    
    /**
     * Returns the {@link Signalized} associated with this {@link Node}, if one exists.
     * @return the {@link Signalized} associated with this {@link Node}, if one exists.
     * @see Signalized
     */
    public abstract Signalized getSignal();
    
    /**
     * Paints this {@link Node} for visualization
     * @param g the {@link Graphics} to paint on
     * @param position the location on the {@link Graphics} 
     * @param radius the radius of the visual
     */
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
    
    /**
     * Paints text describing this node
     * @param g the {@link Graphics} to paint on
     * @param position the location on the {@link Graphics} 
     */
    public void paintText(Graphics g, Point position) {
        String name = getName();
        if (name != null && g != null && position != null) {
            g.setColor(Color.DARK_GRAY);
            g.setFont(getFont());
            g.drawString(name, position.x+MapMarkerDot.DOT_RADIUS+2, position.y+MapMarkerDot.DOT_RADIUS);
        }
    }
    
    /**
     * The {@link Font} to use when drawing text for visualization
     * @return {@link Node#NODE_FONT}
     */
    public Font getFont()
    {
        return NODE_FONT;
    }
    
    /**
     * The {@link Layer} to use in visualization
     * @return {@link Node#NODES}
     */
    public Layer getLayer()
    {
        return NODES;
    }

    /**
     * Update the {@link Layer} used for visualization
     * @param layer the new {@link Layer} 
     */
    public void setLayer(Layer layer){}

    /**
     * Returns the {@link Style} used for visualization
     * @return {@link Node#NODE_STYLE}
     */
    public Style getStyle()
    {
        return NODE_STYLE;
    }

    /**
     * Returns the {@link Style} used for visualization. The {@link Style} used depends on whether this {@link Node} is selected; then whether it is a {@link Zone} or {@link Intersection}
     * @return the {@link Style} for visualization
     */
    public Style getStyleAssigned()
    {
        return isSelected()? SELECTED_STYLE : isZone()? CENTROID_STYLE : NODE_STYLE;
    }

    /**
     * This is the foreground {@link Color} used when drawing this {@link Node} as a filled circle.
     * @return the foreground {@link Color}
     */
    public Color getColor()
    {
        return getStyleAssigned().getColor();
    }

    /**
     * This is the border {@link Color} used when drawing this {@link Node} as a filled circle.
     * @return the border {@link Color}
     */
    public Color getBackColor()
    {
        return getStyleAssigned().getBackColor();
    }

    /**
     * The {@link Stroke} used for visualization
     * @return the {@link Stroke} associated with {@link Node#getStyleAssigned()}
     */
    public Stroke getStroke()
    {
        return getStyleAssigned().getStroke();
    }

    public void setVMS(VMS vms) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
