/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import java.io.Serializable;

/**
 * This class represents and manipulates stored signal data ({@link Node} and offset).
 * @author Michael
 */
public class SignalRecord implements Serializable
{
    private int node;
    private double offset;
    
    /**
     * Constructs the {@link SignalRecord} with the specified {@link Node} and offset
     * @param node the {@link Node} this {@link SignalRecord} applies to
     * @param offset the offset for the signal cycle
     */
    public SignalRecord(Node node, double offset)
    {
        this(node.getId(), offset);
    }
    
    /**
     * Constructs the {@link SignalRecord} with the specified {@link Node} and offset
     * @param node the id of the {@link Node} this {@link SignalRecord} applies to
     * @param offset the offset for the signal cycle
     */
    public SignalRecord(int node, double offset)
    {
        this.node = node;
        this.offset = offset;
    }
    
    /**
     * Returns the id of the {@link Node} this {@link SignalRecord} applies to
     * @return the id of the {@link Node} this {@link SignalRecord} applies to
     */
    public int getNode()
    {
        return node;
    }
    
    /**
     * Returns the offset of the signal cycle
     * @return the offset of the signal cycle
     */
    public double getOffset()
    {
        return offset;
    }
    
    /**
     * Sets the {@link Node} this signal applies to
     * @param n the new {@link Node}
     */
    public void setNode(int n)
    {
        this.node = n;
    }
    
    /**
     * Sets the {@link Node} this signal applies to
     * @param n the id of the new {@link Node}
     */
    public void setNode(Node n)
    {
        this.node = n.getId();
    }
    
    /**
     * Updates the offset of the signal cycle
     * @param o the new signal cycle offset
     */
    public void setOffset(double o)
    {
        offset = o;
    }
    
    /**
     * Returns a {@link String} form of signals in data files
     * @return a {@link String} form of signals in data files
     */
    public String toString()
    {
        return node+"\t"+offset;
    }
}
