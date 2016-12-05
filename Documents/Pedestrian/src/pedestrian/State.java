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
    
    public void apply(Queue[] queues)
    {
        for(int i = 0; i < queues.length; i++)
        {
            queues[i].setSize(queueLen[i]);
        }
    }
    
    
}
