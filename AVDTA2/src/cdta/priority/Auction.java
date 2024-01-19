/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.priority;

import avdta.vehicle.Vehicle;

/**
 *
 * @author micha
 */
public class Auction implements Priority
{

    public int compare(Vehicle v1, Vehicle v2)
    {
        return (int)Math.ceil(10000*(v2.getVOT() - v1.getVOT()));
    }
        
}
