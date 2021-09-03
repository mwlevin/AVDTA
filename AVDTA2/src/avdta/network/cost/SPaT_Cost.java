/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.cost;

import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;

/**
 * The {@link SPaT_Cost} class uses average travel times from the previous simulation as its estimate for the travel cost, 
 * with a discount in cost for nodes that are SPaT enabled if the car is a CV.
 * @author Michael
 */
public class SPaT_Cost extends TravelCost
{
    /**
     * Returns the average travel time at the specified enter time from the previous simulation.
     * @param l the {@link Link}
     * @param vot the value of time (irrelevant)
     * @param enter the time entering the link (s)
     * @return {@link Link#getAvgTT(int)}
     */
    public double cost(Link l, double vot, int enter, DriverType driver)
    {
        //IF vehicle is CV and the source node
       
        if(l.getDest().getSPaT() && (driver.isCV() || driver.isAV())){
          double alp = 1; //ff time weight
          double bet = 0.9; //fuel efficiency weight
          //System.out.println("Cost for link " + l.toString() + " moved from " + l.getAvgTT(enter) + " to " + (alp*(l.getAvgTT(enter)) - (bet*l.getAvgTT(enter))));
          return (alp*(l.getAvgTT(enter)) - (bet*l.getAvgTT(enter)));
        }
        return l.getAvgTT(enter);
    }
    
    public double cost(Link l, double vot, int enter)
    {
        System.out.println("Not discounting for SPaT because no driver was specified.");
        return l.getAvgTT(enter);
    }
}
