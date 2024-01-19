/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.policy;

import avdta.network.node.policy.Bid;
import java.util.ArrayList;
import java.util.List;
import avdta.vehicle.Vehicle;

/**
 * This is a list of bids, used when vehicles can bid for other vehicles at auctions.
 * @author Michael
 */
public class BidList
{
    private List<Bid> list;
    private double total;

    public BidList()
    {
        total = 0;
        list = new ArrayList<Bid>();
    }
    
    /**
     * Returns the total bid amount
     * @return the total bid amount
     */
    public double getTotal()
    {
        return total;
    }
    
    /**
     * Adds a bid to the list
     * @param b the bid to be added
     */
    public void add(Bid b)
    {
        if(b.getAmount() != 0)
        {
            list.add(b);
            total += b.getAmount();
        }
    }
    
    /**
     * Clears the list of bids
     */
    public void clear()
    {
        total = 0;
        list.clear();
    }
    
    /**
     * This is called when a {@link Vehicle} wins the auction. All bids must be paid.
     */
    public void pay()
    {
        for(Bid b : list)
        {
            b.pay();
        }
        
        clear();
    }
}
