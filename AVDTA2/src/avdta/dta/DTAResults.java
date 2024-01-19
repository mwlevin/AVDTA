/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

/**
 * This class encapsulates the results from running DTA. 
 * It includes the travel time, gap, and number of exiting vehicles.
 * It is returned as an output from running DTA.
 * @author Michael
 */
public class DTAResults 
{
    private double mintt, tstt;
    private int num_veh;
    private int exiting;
    
    /**
     * Constructs an empty {@link DTAResults} initialized all variables to 1.
     */
    public DTAResults()
    {
        this(1.0, 1.0, 1, 1);
    }
    
    /**
     * Constructs the {@link DTAResults} with the given parameters.
     * @param mintt the minimum travel time (s)
     * @param tstt the total system travel time (s)
     * @param num_veh the total number of vehicles
     * @param exiting the number of exiting vehicles
     */
    public DTAResults(double mintt, double tstt, int num_veh, int exiting)
    {
        this.mintt = mintt;
        this.tstt = tstt;
        this.num_veh = num_veh;
        this.exiting = exiting;
    }
    
    public DTAResults clone()
    {
        return new DTAResults(mintt, tstt, num_veh, exiting);
    }
    
    /**
     * Returns the average travel time
     * @return the average travel time (min)
     */
    public double getAvgTT()
    {
        return tstt/60 / num_veh;
    }
    
    /**
     * Returns the number of non-exiting vehicles
     * @return the number of non-exiting vehicles
     */
    public int getNonExiting()
    {
        return num_veh - exiting;
    }
    
    /**
     * Returns the number of exiting vehicles
     * @return the number of exiting vehicles
     */
    public int getExiting()
    {
        return exiting;
    }
    
    /**
     * Updates the number of exiting vehicles.
     * @param exiting the new number of exiting vehicles
     */
    public void setExiting(int exiting)
    {
        this.exiting = exiting;
    }
    
    /**
     * Updates the number of non-exiting vehicles.
     * Calls {@link DTAResults#setExiting(int)} with {@link DTAResults#getTrips()} {@code - nonexiting}.
     * @param nonexiting the new number of non-exiting vehicles
     */
    public void setNonExiting(int nonexiting)
    {
        setExiting(num_veh - nonexiting);
    }
    
    /**
     * Updates the number of trips.
     * Note that this does not update the number of non-exiting vehicles, if {@link DTAResults#setNonExiting(int)} was called.
     * @param trips the new number of trips
     */
    public void setTrips(int trips)
    {
        this.num_veh = trips;
    }
    
    /**
     * Returns the minimum travel time
     * @return the minimum travel time (s)
     */
    public double getMinTT()
    {
        return mintt;
    }
    
    /**
     * Updates the minimum travel time
     * @param m the new minimum travel time (s)
     */
    public void setMinTT(double m)
    {
        mintt = m;
    }
    
    /**
     * Updates the total system travel time
     * @param tstt the new total system travel time (s)
     */
    public void setTSTT(double tstt)
    {
        this.tstt = tstt;
    }
    
    /**
     * Returns the total system travel time
     * @return the total system travel time (hr)
     */
    public double getTSTT()
    {
        return tstt/3600.0;
    }
    
    /**
     * Returns the average excess cost
     * @return the average excess cost (s)
     */
    public double getAEC()
    {
        return (tstt - mintt) / num_veh;
    }
    
    /**
     * Returns the gap as a percent of total system travel time
     * @return the gap (%)
     */
    public double getGapPercent()
    {
        return (tstt - mintt) / tstt * 100.0;
    }
    
    /**
     * Returns the total number of trips
     * @return the total number of trips
     */
    public int getTrips()
    {
        return num_veh;
    }
}
