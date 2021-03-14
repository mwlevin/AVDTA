/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.wallet;

import avdta.vehicle.Vehicle;
import java.io.Serializable;

/**
 * A wallet used by vehicles to bid at intersection auctions
 * @author Michael
 */
public abstract class Wallet implements Serializable
{
    public static final Wallet EMPTY = new EmptyWallet();
    
    private double money;
    private double budget;
    
    /**
     * Constructs the wallet with the given amount of money
     * @param money initial amount of money
     */
    public Wallet(double money)
    {
        this.money = money;
        this.budget = money;
    }
    
    /**
     * Resets the wallet to contain the initial amount of money
     */
    public void reset()
    {
        money = budget;
    }
    
    /**
     * Removes money from the wallet
     * @param m the amount of money to be removed
     */
    public void pay(double m)
    {
        money -= m;
    }
    
    /**
     * Returns the amount of money remaining
     * @return the amount of money remaining
     */
    public double getMoney()
    {
        return money;
    }
    
    /**
     * Places a bid on behalf of the vehicle
     * @param vehicle the vehicle to be bidded for
     * @return the amount of money to bid
     */
    public abstract double bid(Vehicle vehicle);
}
