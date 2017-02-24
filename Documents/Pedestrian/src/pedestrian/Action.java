/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pedestrian;

/**
 *
 * @author micha
 */
public class Action 
{
    private double duration;
    private int[] queueLen;
    private boolean[] crosswalk;
    
    public Action(boolean[] crosswalk, int[] queueLen, double duration)
    {
        this.crosswalk = crosswalk;
        this.queueLen = queueLen;
        this.duration = duration;
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
    
    public boolean equals(Object o)
    {
        Action rhs = (Action)o;
        
        
        for(int i = 0; i < queueLen.length; i++)
        {
            if(queueLen[i] != rhs.queueLen[i])
            {
                return false;
            }
        }
        
        for(int i = 0; i < crosswalk.length; i++)
        {
            if(crosswalk[i] != rhs.crosswalk[i])
            {
                return false;
            }
        }
        
        return true;
    }
    
    
    public String toString()
    {
        String output = "(";
        
        for(boolean b : crosswalk)
        {
            output += b+",";
        }
        
        for(int i = 0; i < queueLen.length; i++)
        {
            output += queueLen[i];
            
            if(i < queueLen.length -1)
            {
                output += ",";
            }
        }
        
        output += "): "+duration;
        
        return output;
    }
    
    public double getDuration()
    {
        return duration;
    }
    
    public boolean[] getCrosswalkActivation()
    {
        return crosswalk;
    }
    
    public int[] getQueueChanges()
    {
        return queueLen;
    }
}
