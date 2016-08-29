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
 *
 * @author micha
 */
public class NodeTypeRule extends NodeRule
{
    private int type, control, policy;
    private Color color;
    private int width;
    
    public NodeTypeRule()
    {
        this(EditNode.CENTROID, -1, -1, Color.black, 3);
    }
    
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
    
    
    public NodeTypeRule(int type, int control, int policy, Color color, int width)
    {
        this.type = type;
        this.control = control;
        this.policy = policy;
        this.color = color;
        this.width = width;
    }
    
    public void setRadius(int width)
    {
        this.width = width;
    }
    
    public void setColor(Color c)
    {
        color = c;
    }
    
    public void setType(int t)
    {
        type = t;
    }
    
    public int getType()
    {
        return type;
    }
    
    public void setControl(int c)
    {
        control = c;
    }
    
    public int getControl()
    {
        return control;
    }
    
    public void setPolicy(int p)
    {
        policy = p;
    }
    
    public int getPolicy()
    {
        return policy;
    }
    
    public Color getBackColor(Node n, int t)
    {
        return color;
    }
    
    public Color getColor(Node n, int t)
    {
        return Color.black;
    }
    
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
    
    public int getRadius(Node n, int t)
    {
        return width;
    }
    
    public Color getColor()
    {
        return color;
    }
    
    
    
    public int getRadius()
    {
        return width;
    }
}

