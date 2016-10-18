/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.gui.editor.EditNode;
import avdta.network.node.Intersection;
import avdta.network.node.Node;
import avdta.network.node.PhasedTBR;
import avdta.network.node.PriorityTBR;
import avdta.network.node.StopSign;
import avdta.network.node.TrafficSignal;
import avdta.network.node.obj.BackPressureObj;
import avdta.network.node.obj.P0Obj;
import avdta.network.node.policy.AuctionPolicy;
import avdta.network.node.policy.FCFSPolicy;
import avdta.network.node.policy.MCKSTBR;
import avdta.network.node.policy.SignalWeightedTBR;
import java.awt.Color;

/**
 * The {@link NodeTypeRule} color-codes {@link Node}s of a specified type.
 * The type is fairly complicated because it can refer to the {@link Node} class, the intersection control, or the intersection policy.
 * The types used are specified by {@link EditNode}.
 * @author Michael
 */
public class NodeTypeRule extends NodeRule
{
    private int type, control, policy;
    private Color color;
    private int width;
    
    /**
     * Creates a {@link NodeTypeRule} that matches centroids.
     */
    public NodeTypeRule()
    {
        this(EditNode.CENTROID, -1, -1, Color.black, 3);
    }
    
    /**
     * Returns a String specifying the type of {@link Node}s matched by this {@link NodeTypeRule}.
     * This may correspond to the class of {@link Node} (centroid/intersection), the intersection control (signals, reservations, etc.), or the policy (FCFS, backpressure, etc.).
     * @return a String specifying the type of {@link Node}s matched by this {@link NodeTypeRule}
     */
    public String getName()
    {
        if(type == EditNode.CENTROID)
        {
            return EditNode.TYPES[type];
        }
        else if(type == EditNode.INTERSECTION)
        {
            if(control != EditNode.RESERVATIONS)
            {
                return EditNode.CONTROLS[control];
            }
            else
            {
                return EditNode.POLICIES[policy];
            }
        }
        else
        {
            return "null";
        }
    }
    
    /**
     * Constructs this {@link NodeTypeRule} for the given type matching.
     * See {@link EditNode} for types.
     * @param type the class of node (centroid/intersection)
     * @param control the type of control (signals, reservations, etc.)
     * @param policy the policy (FCFS, backpressure, etc.)
     * @param color the fill color used to display these types of nodes
     * @param width the radius (px)
     */
    public NodeTypeRule(int type, int control, int policy, Color color, int width)
    {
        this.type = type;
        this.control = control;
        this.policy = policy;
        this.color = color;
        this.width = width;
    }
    
    /**
     * Updates the draw radius.
     * @param width the new draw radius (px)
     */
    public void setRadius(int width)
    {
        this.width = width;
    }
    
    /**
     * Updates the fill color.
     * @param c the new fill color
     */
    public void setColor(Color c)
    {
        color = c;
    }
    
    /**
     * Updates the type of {@link Node} (centroids/intersections) that matches.
     * See {@link EditNode} for types.
     * @param t the new type
     */
    public void setType(int t)
    {
        type = t;
    }
    
    /**
     * Returns the type of {@link Node} (centroids/intersection) matched.
     * See {@link EditNode} for types.
     * @return the type of {@link Node} matched
     */
    public int getType()
    {
        return type;
    }
    
    /**
     * Updates the type of intersection control (signals, reservations, etc.) matched.
     * See {@link EditNode} for types.
     * @param c the type of intersection control matched
     */
    public void setControl(int c)
    {
        control = c;
    }
    
    /**
     * Returns the type of intersection control (signals, reservations, etc.) matched.
     * See {@link EditNode} for types.
     * @return the type of intersection control matched
     */
    public int getControl()
    {
        return control;
    }
    
    /**
     * Updates the intersection control policy (FCFS, backpressure, etc.) matched.
     * See {@link EditNode} for types.
     * @param p the new intersection control policy matched
     */
    public void setPolicy(int p)
    {
        policy = p;
    }
    
    /**
     * Returns the intersection control policy (FCFS, backpressure, etc.) matched.
     * See {@link EditNode} for types.
     * @return the new intersection control policy matched
     */
    public int getPolicy()
    {
        return policy;
    }
    
    /**
     * Returns the color for the given {@link Node} at the given time.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return {@link NodeTypeRule#getColor()}
     */
    public Color getColor(Node n, int t)
    {
        return color;
    }
    
    /**
     * Returns whether this {@link NodeRule} applies to the given {@link Node} at the specified time
     * @param n the {@link Node}
     * @param t the time (s)
     * @return if the {@link Node} has the matching type, control, and policy
     */
    public boolean matches(Node n, int t)
    {
        if(type == EditNode.CENTROID)
        {
            return n.isZone();
        }
        else if(type == EditNode.INTERSECTION)
        {
            if(!(n instanceof Intersection))
            {
                return false;
            }
            Intersection i = (Intersection)n;
            
            switch(control)
            {
                case EditNode.SIGNALS:
                    return (i.getControl() instanceof TrafficSignal);
                case EditNode.STOP_SIGN:
                    return (i.getControl() instanceof StopSign);
                case EditNode.RESERVATIONS:
                    switch(policy)
                    {
                        case EditNode.PHASED:
                            return (i.getControl() instanceof PhasedTBR);
                        case EditNode.SIGNAL_WEIGHTED:
                            return (i.getControl() instanceof SignalWeightedTBR);
                        case EditNode.FCFS:
                            return (i.getControl() instanceof PriorityTBR) &&
                                    (((PriorityTBR)i.getControl()).getPolicy() instanceof FCFSPolicy);
                        case EditNode.AUCTION:
                            return (i.getControl() instanceof PriorityTBR) &&
                                    (((PriorityTBR)i.getControl()).getPolicy() instanceof AuctionPolicy);
                        case EditNode.BACKPRESSURE:
                            return (i.getControl() instanceof MCKSTBR) &&
                                    (((MCKSTBR)i.getControl()).getObj() instanceof BackPressureObj);
                        case EditNode.P0:
                            return (i.getControl() instanceof MCKSTBR) &&
                                    (((MCKSTBR)i.getControl()).getObj() instanceof P0Obj);
                        default:
                            return false;
                    }
                default:
                    return false;
            }
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns the radius for the given {@link Node} at the given time.
     * @param n the {@link Node}
     * @param t the time (s)
     * @return {@link NodeTypeRule#getRadius()}
     */
    public int getRadius(Node n, int t)
    {
        return width;
    }
    
    /**
     * Returns the fill color for {@link Node}s of this type.
     * @return the fill color
     */
    public Color getColor()
    {
        return color;
    }
    
    
    /**
     * Returns the radius used to draw {@link Node}s of this type.
     * @return the radius (px)
     */
    public int getRadius()
    {
        return width;
    }
}

