/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

/**
 *
 * @author Michael
 */
public class FairWallet extends Wallet
{
    public FairWallet(double money)
    {
        super(money);
    }
    
    
    public double bid(Vehicle vehicle)
    {
        return getMoney() / vehicle.numRemainingLinks();
    }
}
