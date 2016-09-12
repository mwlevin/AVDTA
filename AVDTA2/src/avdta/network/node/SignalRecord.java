/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import java.io.Serializable;

/**
 *
 * @author ml26893
 */
public class SignalRecord implements Serializable
{
    private int node;
    private double offset;
    
    public SignalRecord(Node node, double offset)
    {
        this(node.getId(), offset);
    }
    
    public SignalRecord(int node, double offset)
    {
        this.node = node;
        this.offset = offset;
    }
    
    public int getNode()
    {
        return node;
    }
    
    public double getOffset()
    {
        return offset;
    }
    
    public void setNode(int n)
    {
        this.node = n;
    }
    
    public void setNode(Node n)
    {
        this.node = n.getId();
    }
    
    public void setOffset(double o)
    {
        offset = o;
    }
    
    public String toString()
    {
        return node+"\t"+offset;
    }
}
