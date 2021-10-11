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
    private static double alpha = .1464;
    private static double bet = 1-alpha; // discount rate for SPat
    //SPat discount rate = 1-bet
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
            double dis = (l.getLength()*(1/24.9)*bet*3.01*(1/13.6)*3600);
          //System.out.println("Cost for link " + l.toString() + " moved from " + l.getAvgTT(enter) + " to " + (l.getAvgTT(enter)-dis) + " with discount " + dis);
          return l.getAvgTT(enter)-dis;
        }
        return l.getAvgTT(enter);
    }
    
    public double cost(Link l, double vot, int enter)
    {
        //System.out.println("Not discounting for SPaT because no driver was specified.");
        return l.getAvgTT(enter);
    }
    
    /**
     * Returns the link free flow travel time discounted for SPaT
     * @param l the link
     * @return free flow travel time (seconds)
     */
    public double ffCost(Link l){
        //System.out.println("Discounting FFTime for link " + l.toString() + "from " + (l.getLength() / l.getFFSpeed() * 3600.0) + " to " + ((l.getLength() / l.getFFSpeed() * 3600.0) - bet*(l.getLength() / l.getFFSpeed() * 3600.0)));
        return bet*(l.getLength() / l.getFFSpeed() * 3600.0);
    }
    
    /**
     * Get the SPaT discount rate
     * @return the SPaT discount rate
     */
    public double getDiscount(){
        return bet;
    }
    
    public String toString(){
        return "SPaT Cost function";
    }
}
