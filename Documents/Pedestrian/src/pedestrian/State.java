/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

/**
 *
 * @author ml26893
 */
public class State 
{
    private int[] queueLen;
    
    public double J;
    public Action mu;
    
    public State(Node n)
    {
        queueLen = new int[n.getNumQueues()];
    }
    
    public State(Queue[] queues)
    {
        queueLen = new int[queues.length];
        
        for(int i = 0; i < queues.length; i++)
        {
            queueLen[i] = queues[i].getSize();
        }
    }
    
    public State(int[] copy)
    {
        this.queueLen = new int[copy.length];
        
        for(int i = 0; i < copy.length; i++)
        {
            queueLen[i] = copy[i];
        }
    }
    
    public int[] getQueueLengths()
    {
        return queueLen;
    }
    
    public int[] copyQueueLengths()
    {
        int[] copy = new int[queueLen.length];
        
        for(int i = 0; i < copy.length; i++)
        {
            copy[i] = queueLen[i];
        }
        
        return copy;
    }
    
    public void apply(Queue[] queues)
    {
        for(int i = 0; i < queues.length; i++)
        {
            queues[i].setSize(queueLen[i]);
        }
    }
    
    public boolean equals(State rhs)
    {
        for(int i = 0; i < queueLen.length; i++)
        {
            if(queueLen[i] != rhs.queueLen[i])
            {
                return false;
            }
        }
        
        return true;
    }
    
    public int hashCode()
    {
        int output = 0;
        int mult = 1;
        
        for(int i : queueLen)
        {
            output += i * mult;
            mult *= 10;
        }
        
        return output;
    }
}
