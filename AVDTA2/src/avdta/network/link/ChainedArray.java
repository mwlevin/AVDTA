/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

/**
 * This is used to store and retrieve the cumulative counts for the last T time steps. 
 * This is used for the link transmission model ({@link LTMLink})
 * The size is initialized when constructed. 
 * It is expected that the maximum size needed will remain constant. Any time step within the last T may be referenced.
 * A single fixed size array is used, and indexing arithmetic is used to determine which value to access.
 * @author Michael
 */
public class ChainedArray 
{
    private int[] array;
    private int start;
    private int start_idx;
    
    /**
     * Constructs this {@link ChainedArray} with the specified size
     * @param ref_length the size of the {@link ChainedArray}
     */
    public ChainedArray(int ref_length)
    {
        array = new int[ref_length];
        start = 0;
        start_idx = 0;
    }
    
    /**
     * Adds the value at the specified index
     * @param idx the time step to be added
     * @param val the value (cumulative count)
     */
    public void add(int idx, int val)
    {
        index(idx);
        
        for(int i = idx; i <= start_idx; i++)
        {
            array[index(i)] += val;
        }
    }
    
    /**
     * Gets the cumulative count at the specified index
     * @param idx the time step
     * @return the cumulative count
     */
    public int get(int idx)
    {
        return array[index(idx)];
    }
    
    /**
     * Return the number of time steps of data
     * @return the number of time steps of data
     */
    public int length()
    {
        return array.length;
    }
    
    private int index(int idx)
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
    
    /**
     * Empties the array and resets the indexing system
     */
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
