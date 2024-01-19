/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.wallet;

import avdta.vehicle.Vehicle;
import avdta.vehicle.wallet.Wallet;

/**
 * This wallet bids an equal amount of money at each intersection.
 * @author Michael
 */
public class FairWallet extends Wallet
{
    /**
     * Constructs the wallet with the given amount of money
     * @param money initial amount of money
     */
    public FairWallet(double money)
    {
        super(money);
    }
    
    /**
     * Places a bid on behalf of the vehicle.
     * The bid is the remaining money divided by the number of remaining intersections.
     * @param vehicle the vehicle to be bidded for
     * @return the amount of money to bid
     */
    public double bid(Vehicle vehicle)
    {
        return getMoney() / vehicle.getNumRemainingLinks();
    }
}
