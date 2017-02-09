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
public class Poisson 
{
    private double rate;
    
    
    
    public Poisson(double rate)
    {
        this.rate = rate;
    }
    
    
    public double prob(int x, int state, double duration, Queue queue)
    {
        if(x+state < queue.getMax())
        {
            return poisson(x, duration);
        }
        else
        {
            double output = 1.0;
            
            for(int i = 0; i < queue.getMax() - state; i++)
            {
                output -= poisson(i, duration);
            }
            
            return output;
        }
    }
    
    // lambda^k e^-lambda / k!
    public double poisson(int x, double time)
    {
        double lambda = rate * time;
        return Math.pow(lambda, x) * Math.exp(-lambda) / factorial(x);
    }
    
    private int factorial(int x)
    {
        int output = 1;
        
        for(int i = 2; i <= x; i++)
        {
            output *= i;
        }
        
        return output;
    }
    
    
}
