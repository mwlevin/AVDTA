/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

/**
 *
 * @author ut
 */
public class EmptyWallet extends Wallet
{
    public EmptyWallet()
    {
        super(0);
    }
    
    public double bid(Vehicle vehicle)
    {
        return 0;
    }
}
