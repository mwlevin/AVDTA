/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.Network;
import avdta.network.Simulator;

@Deprecated
/**
 *
 * @author Michael
 */
public class FixedSizeLL
{
    private int size, max_size;
    
    private ListNode head, tail;
    
    public FixedSizeLL(int size)
    {
        this.max_size = size;
        
    }
    
    // t should be equal to size
    public int get(int t)
    {
        if(head == null)
        {
            return 0;
        }
        
        if(head.time == t)
        {
            return head.value;
        }
        else if(tail.time == t)
        {
            return tail.value;
        }
        else
        {
            for(int i = t; i <= Simulator.time / Network.dt; i++)
                createNode(i, 0);
            
            return 0;
        }
    }
    
    public int size()
    {
        return size;
    }
    
    public void add(int t, int value)
    {
        if(tail != null && tail.time == t)
        {
            tail.value += value;
        }
        else
        {
            createNode(t, value);
        }
    }
    
    private void createNode(int t, int value)
    {
        ListNode temp = new ListNode(t, value);

        if(head == null)
        {
            head = temp;
            tail = temp;
        }
        else
        {
            tail.setNext(temp);
            tail = temp;
        }

        if(size == max_size)
        {
            head = head.next;
        }
        else
        {
            size++;
        }
    }
    
    public void clear()
    {
        size = 0;
        head = tail = null;
    }
    
    static class ListNode
    {
        private ListNode next;
        private int value;
        private int time;
        
        public ListNode(int t, int value)
        {
            this.value = value;
            this.time = t;
        }
        
        public int getTime()
        {
            return time;
        }
        
        public int getValue()
        {
            return value;
        }
        
        public void setNext(ListNode next)
        {
            this.next = next;
        }
    }
}
