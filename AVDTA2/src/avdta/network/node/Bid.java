/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.vehicle.Vehicle;
import java.io.Serializable;

/**
 *
 * @author Michael
 */
public class Bid implements Serializable
{
    private Vehicle vehicle;
    private double amount;
    
    public Bid(Vehicle v, double amount)
    {
        this.vehicle = v;
        this.amount = amount;
    }
    
    public Vehicle getVehicle()
    {
        return vehicle;
    }
    
    public double getAmount()
    {
        return amount;
    }
    
    public void pay()
    {
        vehicle.getWallet().pay(amount);
    }
}
