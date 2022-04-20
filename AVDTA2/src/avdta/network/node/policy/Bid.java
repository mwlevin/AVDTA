/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.vehicle.Vehicle;
import java.io.Serializable;

/**
 * This represents a bid by a specific {@link Vehicle}. The bid could be for other {@link Vehicle}s to move.
 * @author Michael
 */
public class Bid implements Serializable
{
    private Vehicle vehicle;
    private double amount;
    
    /**
     * Constructs the bid for the specified {@link Vehicle} and with the specified amount
     * @param v the @link{Vehicle} placing the bid
     * @param amount the value of the bid
     */
    public Bid(Vehicle v, double amount)
    {
        this.vehicle = v;
        this.amount = amount;
    }
    
    /**
     * Returns the {@link Vehicle} placing the bid
     * @return the {@link Vehicle} placing the bid
     */
    public Vehicle getVehicle()
    {
        return vehicle;
    }
    
    /**
     * Returns the value of the bid
     * @return the value of the bid
     */
    public double getAmount()
    {
        return amount;
    }
    
    /**
     * This is called when an auction is won. The {@link Vehicle} will pay the required amount.
     */
    public void pay()
    {
        vehicle.getWallet().pay(amount);
    }
}
