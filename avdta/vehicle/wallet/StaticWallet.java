/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.wallet;

import avdta.vehicle.Vehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.wallet.Wallet;

/**
 * This wallet bids the same amount of money at each intersection.
 * @author Michael
 */
public class StaticWallet extends Wallet
{
    /**
     * Constructs the wallet with the amount of money to bid at each intersection
     * @param money the amount of money to bid at each intersection
     */
    public StaticWallet(double money)
    {
        super(money);
    }
    
    /**
     * This wallet never loses money. No money is actually removed.
     * @param money the amount of money to be removed
     */
    public void pay(double money){}
    
    /**
     * Always bids the amount of money in this wallet
     * @param vehicle the vehicle to be bidded for.
     * @return {@link Wallet#getMoney()}
     */
    public double bid(Vehicle vehicle)
    {
        return getMoney();
    }
}
