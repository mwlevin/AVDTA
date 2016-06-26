/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import java.io.Serializable;

/**
 *
 * @author Michael
 */
public class RunningAvg implements Serializable
{
	private double value;
	private double count;
	
	public RunningAvg()
	{
		value = 0;
                count = 0;
	}
	
	public void add(double val)
	{
		value += val;
		count++;
	}
        
        public void add(double val, double weight)
        {
            value += val * weight;
            count += weight;
        }
	
	public double getCount()
	{
		return count;
	}
	
	public double getAverage()
	{
            if(count > 0)
            {
		return value / count;
            }
            else
            {
                return 0;
            }
	}
	
	public void reset()
	{
		value = 0;
		count = 0;
	}
}
