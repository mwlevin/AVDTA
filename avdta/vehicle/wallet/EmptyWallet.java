/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle.wallet;

import avdta.vehicle.Vehicle;
import avdta.vehicle.wallet.Wallet;

/**
 * This wallet doesn't bid anything.
 * @author Michael
 */
public class EmptyWallet extends Wallet
{
    /**
     * Constructs this Wallet with 0 money
     */
    public EmptyWallet()
    {
        super(0);
    }
    
    /**
     * Returns a bid of 0
     * @param vehicle the vehicle to be bidded for
     * @return 0
     */
    public double bid(Vehicle vehicle)
    {
        return 0;
    }
}
