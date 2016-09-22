/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

/**
 * This represents an assignment interval, which is used when generating demand. 
 * This contains the start time, duration, and weight.
 * @author Michael
 */
public class AST implements Comparable<AST>
{
    private int id;
    private int start;
    private int duration;
    private double weight;
    
    /**
     * Constructs the assignment interval with the specified parameters.
     * @param id the id
     * @param start the start time (s)
     * @param duration the duration (s)
     * @param weight the weight
     */
    public AST(int id, int start, int duration, double weight)
    {
        this.id = id;
        this.start = start;
        this.duration = duration;
        this.weight = weight;
    }
    
    /**
     * Returns the id
     * @return the id
     */
    public int getId()
    {
        return id;
    }
    
    /**
     * Checks whether the specified time is contained during this assignment interval
     * @param time the time to check (s)
     * @return whether the specified time is contained during this assignment interval
     */
    public boolean contains(int time)
    {
        return time >= start && time < start+duration;
    }
    
    /**
     * Returns the start time
     * @return the start time (s)
     */
    public int getStart()
    {
        return start;
    }
    
    /**
     * Returns the duration 
     * @return the duration (s)
     */
    public int getDuration()
    {
        return duration;
    }
    
    /**
     * Returns the weight of this assignment interval (the proportion of demand).
     * Note that weights are normalized when demand is generated.
     * @return the weight of this assignment interval
     */
    public double getWeight()
    {
        return weight;
    }
    
    /**
     * Updates the weight of this assignment interval (the proportion of demand).
     * Note that weights are normalized when demand is generated.
     * @param w the new weight
     */
    public void setWeight(double w)
    {
        this.weight = w;
    }
    
    /**
     * Orders {@link AST}s according to start time
     * @param rhs the {@link AST} to compare with
     * @return order based on start time, ascending
     */
    public int compareTo(AST rhs)
    {
        return start - rhs.start;
    }
}
