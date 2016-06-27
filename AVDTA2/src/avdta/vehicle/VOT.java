/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import java.util.Random;

/**
 *
 * @author micha
 */
public class VOT {
    public static double a = 22020.6;
    public static double b = 2.7926;
    public static double c = 0.2977;
    
    

    
    
    public static double dagum_rand(Random rand)
    {
        double y = rand.nextDouble();
        return Math.pow( a / (Math.pow(1/y, 1/c) - 1), 1/b);
    }
}
