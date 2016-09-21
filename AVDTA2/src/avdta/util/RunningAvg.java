/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.util;

import java.io.Serializable;

/**
 * This class implements a running average. It stores the total count and the weight only.
 * @author Michael
 */
public class RunningAvg implements Serializable
{
	private double value;
	private double count;
	
        /**
         * Constructs the {@link RunningAvg} with 0.
         */
	public RunningAvg()
	{
		value = 0;
                count = 0;
	}
        
        /**
         * Sets the value as specified and sets the total weight to 1.
         * @param v the new value
         */
        public void setValue(double v)
        {
            value = v;
            count = 1;
        }
	
        /**
         * Adds the given value with a weight of 1
         * @param val the value to be added
         */
	public void add(double val)
	{
		value += val;
		count++;
	}
        
        /**
         * Adds the given value at the given weight
         * @param val the value to be added
         * @param weight the weight of the value
         */
        public void add(double val, double weight)
        {
            value += val * weight;
            count += weight;
        }
	
        /**
         * Returns the total weight
         * @return the total weight
         */
	public double getCount()
	{
		return count;
	}
	
        /**
         * Returns the running average
         * @return the running average
         */
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
	
        /**
         * Resets the total value and weight to 0
         */
	public void reset()
	{
		value = 0;
		count = 0;
	}
}
