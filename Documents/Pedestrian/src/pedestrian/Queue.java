/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

/**
 *
 * @author ml26893
 */
public class Queue 
{
    private int size;
    private int max;
    
    private int index;
    private Poisson dist;
    
    public int y;
    
    public Queue(int max, double rate)
    {
        size = 0;
        this.max = max;
        dist = new Poisson(rate);
    }
    
    public Poisson getDemand()
    {
        return dist;
    }
    
    public int getMax()
    {
        return max;
    }
    
    public void setIndex(int idx)
    {
        this.index = idx;
    }
    
    public int getIndex()
    {
        return index;
    }
    
    public void add(int x)
    {
        setSize(size+x);
    }
    
    public int getSize()
    {
        return size;
    }
    
    public void setSize(int x)
    {
        size = (int)Math.min(max, x);
    }
}
