/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.demand;

import avdta.project.DemandProject;
import java.io.Serializable;
import java.util.Scanner;
/**
 * This class represents an entry in {@link DemandProject#getDemandProfileFile()}.
 * @author Michael
 */
public class DemandProfileRecord implements Serializable
{
    private int id, start, duration;
    private double weight;
    
    /**
     * Instantiates this {@link DemandProfileRecord} with the given parameters.
     * @param id the id
     * @param weight the weight
     * @param start the start time (s)
     * @param duration the duration (s)
     */
    public DemandProfileRecord(int id, double weight, int start, int duration)
    {
        this.id = id;
        this.weight = weight;
        this.start = start;
        this.duration = duration;
    }
    
    /**
     * Instantiates this {@link DemandProfileRecord} from the given line of input data.
     * @param line the input data
     */
    public DemandProfileRecord(String line)
    {
        Scanner chopper = new Scanner(line);
        id = chopper.nextInt();
        weight = chopper.nextDouble();
        start = chopper.nextInt();
        duration = chopper.nextInt();
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
     * Updates the id.
     * @param id the new id
     */
    public void setId(int id)
    {
        this.id = id;
    }
    
    /**
     * Returns the weight.
     * @return the weight
     */
    public double getWeight()
    {
        return weight;
    }
    
    /**
     * Updates the weight.
     * @param weight the new weight.
     */
    public void setWeight(double weight)
    {
        this.weight = weight;
    }
    
    /**
     * Returns the start time.
     * @return the start time (s)
     */
    public int getStartTime()
    {
        return start;
    }
    
    /**
     * Updates the start time.
     * @param start the new start time (s)
     */
    public void setStartTime(int start)
    {
        this.start = start;
    }
    
    /**
     * Returns the duration.
     * @return the duration (s)
     */
    public int getDuration()
    {
        return duration;
    }
    
    /**
     * Updates the duration.
     * @param duration the new duration (s)
     */
    public void setDuration(int duration)
    {
        this.duration = duration;
    }
    
    /**
     * Returns a {@link String} representation that can be written to the data file.
     * @return the {@link String} representation
     */
    public String toString()
    {
        return ""+id+"\t"+weight+"\t"+start+"\t"+duration;
    }
}
