/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parking;

/**
 *
 * @author micha
 */
public class Link 
{
    public static final int dt = 6;
    
    private int tt;
    private int id;
    private Node source, dest;
    
    private static int next_id = 1;
    
    public Link(Node source, Node dest, double time)
    {
        this.id = next_id++;
        this.source = source;
        this.dest = dest;
        tt = (int)Math.max(1, Math.round(time/dt));
        
        source.addLink(this);
        dest.addLink(this);
    }
    
    public Node getSource()
    {
        return source;
    }
    
    public Node getDest()
    {
        return dest;
    }
    
    public int getTT()
    {
        return tt;
    }
    
    public String toString()
    {
        return ""+id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public int getId()
    {
        return id;
    }
}
