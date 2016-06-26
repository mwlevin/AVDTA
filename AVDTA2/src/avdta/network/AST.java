/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

/**
 *
 * @author Michael
 */
public class AST implements Comparable<AST>
{
    private int id;
    private int start;
    private int duration;
    private double weight;
    
    public AST(int id, int start, int duration, double weight)
    {
        this.id = id;
        this.start = start;
        this.duration = duration;
        this.weight = weight;
    }
    
    public int getId()
    {
        return id;
    }
    
    public boolean contains(int time)
    {
        return time >= start && time < start+duration;
    }
    
    public int getStart()
    {
        return start;
    }
    
    public int getDuration()
    {
        return duration;
    }
    
    public double getWeight()
    {
        return weight;
    }
    
    public void setWeight(double w)
    {
        this.weight = w;
    }
    
    public int compareTo(AST rhs)
    {
        return start - rhs.start;
    }
}
