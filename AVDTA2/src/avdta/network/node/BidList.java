/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import java.util.ArrayList;
import java.util.List;

/**
 *
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
    
    public double getTotal()
    {
        return total;
    }
    
    public void add(Bid b)
    {
        if(b.getAmount() != 0)
        {
            list.add(b);
            total += b.getAmount();
        }
    }
    
    public void clear()
    {
        total = 0;
        list.clear();
    }
    
    public void pay()
    {
        for(Bid b : list)
        {
            b.pay();
        }
        
        clear();
    }
}
