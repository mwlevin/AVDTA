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
    
    public Queue()
    {
        size = 0;
    }
    
    public void add(int x)
    {
        size += x;
    }
    
    public int getSize()
    {
        return size;
    }
    
    public void setSize(int x)
    {
        size = x;
    }
}
