/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.transit;

/**
 * This represents the travel time for a bus between two stops. {@link BusLink}s store a list of {@link TTRecord}s.
 * @author Michael
 */
public class TTRecord implements Comparable<TTRecord>
{
    private int dtime, tt;

    /**
     * Constructs the record with the given departure time and travel time
     * @param dtime the departure time
     * @param tt the travel time
     */
    public TTRecord(int dtime, int tt)
    {
        this.dtime = dtime;
        this.tt = tt;
    }

    /**
     * Returns the departure time
     * @return the departure time (s)
     */
    public int getDepTime()
    {
        return dtime;
    }

    /**
     * Returns the travel time
     * @return the travel time (s)
     */
    public int getTT()
    {
        return tt;
    }

    /**
     * Checks whether the departure times are the same
     * @param o the object to be compared
     * @return if the departure times are the same
     */
    public boolean equals(Object o)
    {
        TTRecord rhs = (TTRecord)o;
        return rhs.dtime == dtime;
    }

    /**
     * Orders the {@link TTRecord}s by departure time
     * @param rhs the {@link TTRecord} to be compared
     * @return order based on departure time
     */
    public int compareTo(TTRecord rhs)
    {
        return dtime - rhs.dtime;
    }

    public int hashCode()
    {
        return dtime;
    }

}
