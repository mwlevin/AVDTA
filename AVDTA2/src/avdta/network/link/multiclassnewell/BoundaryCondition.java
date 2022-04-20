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
public class BoundaryCondition
{
    private int min_t, max_t;
    private int initial_c, final_c;
    
    public BoundaryCondition(int min_t, int max_t, int initial_C, int final_C)
    {
        this.min_t = min_t;
        this.max_t = max_t;
        this.final_c = final_C;
        this.initial_c = initial_C;
    }
    
    
    public void setFinalC(int c)
    {
        final_c = c;
    }
    
    public void setInitialC(int c)
    {
        initial_c = c;
    }
    
    public double getMinT()
    {
        return min_t;
    }
    
    public double getMaxT()
    {
        return max_t;
    }
    
    public double getFlow()
    {
        return (double)(final_c - initial_c) / (max_t - min_t) / 3600.0;
    }
    
    public boolean isDefined(double t)
    {
        return (t >= min_t && t <= max_t);
    }
    
    public double getC(double t)
    {
        if(isDefined(t))
        {
            return initial_c + (t-min_t) * getFlow();
        }
        else
        {
            return Integer.MAX_VALUE;
        }
    }

    public double calcT(double c)
    {
        double q = getFlow();
        double t = (c + q*min_t - initial_c) / q;
        
        if(isDefined(t))
        {
            return t;
        }
        else
        {
            return -1;
        }
    }
    
    public String toString()
    {
        return ""+getInitialC()+"-"+getFinalC();
    }
    
    public int getInitialC()
    {
        return initial_c;
    }
    
    public int getFinalC()
    {
        return final_c;
    }

    public int compareTo(BoundaryCondition o) 
    {
        if(o.min_t < min_t)
        {
            return 1;
        }
        else if(min_t < o.min_t)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
