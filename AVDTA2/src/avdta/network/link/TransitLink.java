/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.node.Node;
import java.io.Serializable;

/**
 *
 * @author Michael
 */
public abstract class TransitLink implements Serializable
{
    private Node source, dest;
    
    public TransitLink(Node source, Node dest)
    {
        this.source = source;
        this.dest = dest;
        
        source.addLink(this);
        dest.addLink(this);
    }
    
    public abstract double getTT(int dtime);
    public abstract void reset();
    
    public Node getSource()
    {
        return source;
    }
    
    public Node getDest()
    {
        return dest;
    }
    
    
}
