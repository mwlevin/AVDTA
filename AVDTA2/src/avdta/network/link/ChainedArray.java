/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

/**
 *
 * @author Michael
 */
public class ChainedArray 
{
    private int[] array;
    private int start;
    private int start_idx;
    
    public ChainedArray(int ref_length)
    {
        array = new int[ref_length];
        start = 0;
        start_idx = 0;
    }
    
    public void add(int idx, int val)
    {
        index(idx);
        
        for(int i = idx; i <= start_idx; i++)
        {
            array[index(i)] += val;
        }
    }
    
    public int get(int idx)
    {
        return array[index(idx)];
    }
    
    public int length()
    {
        return array.length;
    }
    
    public int index(int idx)
    {

        int offset = idx - start_idx;
        
        if(offset > 0)
        {
            int prev;
            
            for(int i = 0; i < offset; i++)
            {
                prev = start;
                start++;
                
                if(start >= array.length)
                {
                    start -= array.length;
                }
                
                array[start] = array[prev];
            }
            
            start_idx = idx;
            
            return start;
        }
        else
        {
            
            int output = start + offset;

            if(output < 0)
            {
                output += array.length;
            }
            
            return output;
        }
    }
    
    public void clear()
    {
        for(int i = 0; i < array.length; i++)
        {
            array[i] = 0;
        }
        
        start = 0;
        start_idx = 0;
    }
}
