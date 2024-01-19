/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import java.util.Random;

/**
 * This class calculates a value of time distribution based on the Dagum income distribution
 * @author Michael
 */
public class VOT {
    public static final double a = 22020.6;
    public static final double b = 2.7926;
    public static final double c = 0.2977;
    
    

    
    /**
     * Calculates a random value of time based on the Dagum income distribution.
     * @param rand the random number generator
     * @return a random value of time 
     */
    public static double dagum_rand(Random rand)
    {
        double y = rand.nextDouble();
        return Math.pow( a / (Math.pow(1/y, 1/c) - 1), 1/b);
    }
}
