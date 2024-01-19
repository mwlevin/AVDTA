/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.util;

/**
 *
 * @author ml26893
 */
public class Pair<S, T> 
{
    private S first;
    private T second;
    
    public Pair(S first, T second)
    {
        this.first = first;
        this.second = second;
    }
    
    public S first()
    {
        return first;
    }
    
    public T second()
    {
        return second;
    }
    
    public void setFirst(S first)
    {
        this.first = first;
    }
    
    public void setSecond(T second)
    {
        this.second = second;
    }
    
    public String toString()
    {
        return "("+first+","+second+")";
    }
}
