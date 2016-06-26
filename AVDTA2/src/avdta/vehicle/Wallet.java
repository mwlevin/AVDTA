/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import java.io.Serializable;

/**
 *
 * @author ut
 */
public abstract class Wallet implements Serializable
{
    public static final Wallet EMPTY = new EmptyWallet();
    
    private double money;
    private double budget;
    
    public Wallet(double money)
    {
        this.money = money;
        this.budget = money;
    }
    
    public void reset()
    {
        money = budget;
    }
    
    public void pay(double m)
    {
        money -= m;
    }
    
    public double getMoney()
    {
        return money;
    }
    
    public abstract double bid(Vehicle vehicle);
}
