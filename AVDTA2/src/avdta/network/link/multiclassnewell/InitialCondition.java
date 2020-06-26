/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.multiclassnewell;

/**
 *
 * @author micha
 */
public class InitialCondition implements Comparable<InitialCondition>
{
    private double min_x, max_x;
    private double density;
    
    private double initial_c; // this is for the maximum x value
    
    public InitialCondition(double min_x, double max_x, double density)
    {
        this.min_x = min_x;
        this.max_x = max_x;
        this.density = density;
    }
    
    public void setInitialC(double initial_c)
    {
        this.initial_c = initial_c;
    }
    
    public double getMinX()
    {
        return min_x;
    }
    
    public double getMaxX()
    {
        return max_x;
    }
    
    public boolean isDefined(double x)
    {
        return (x >= min_x && x <= max_x);
    }
    
    public double getC(double x)
    {
        if(isDefined(x))
        {
            return (max_x - x)*density +initial_c;
        }
        else
        {
            return Integer.MAX_VALUE;
        }
    }
    
    public double calcX(double c)
    {
        double x = (c - initial_c - density*max_x)/(-density);
        
        if(isDefined(x))
        {
            return x;
        }
        else
        {
            return -1;
        }
    }
    
    public double getDensity()
    {
        return density;
    }
    
    public String toString()
    {
        return "["+min_x+", "+max_x+"]";
    }

    public double getInitialC()
    {
        return initial_c;
    }
    
    public int compareTo(InitialCondition o) 
    {
        if(o.min_x < min_x)
        {
            return 1;
        }
        else if(min_x < o.min_x)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
