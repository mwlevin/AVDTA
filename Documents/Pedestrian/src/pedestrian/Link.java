/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

/**
 *
 * @author ml26893
 */
public abstract class Link 
{
    private int id;
    private Node node;
    
    private static int next_id = 1;
    
    private double capacity;
    
    public Link(Node n, double capacity)
    {
        id = next_id++;
        
        node = n;
        this.capacity = capacity;
        
        n.addLink(this);
    }
    
    public Node getNode()
    {
        return node;
    }
    
    public double getCapacity()
    {
        return capacity;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public int getId()
    {
        return id;
    }
    
    public String toString()
    {
        return ""+id;
    }
}
