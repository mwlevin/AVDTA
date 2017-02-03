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
    
    public int y;
    
    public Queue(int max)
    {
        size = 0;
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
