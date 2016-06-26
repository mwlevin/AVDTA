/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.vehicle;

import avdta.vehicle.Vehicle;
import avdta.vehicle.Wallet;

/**
 *
 * @author Michael
 */
public class StaticWallet extends Wallet
{
    public StaticWallet(double money)
    {
        super(money);
    }
    
    public void pay(double money){}
    
    public double bid(Vehicle vehicle)
    {
        return getMoney();
    }
}
