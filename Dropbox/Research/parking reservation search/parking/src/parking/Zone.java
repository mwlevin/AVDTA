/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parking;

/**
 *
 * @author micha
 */
public class Zone extends Node
{
    private double p;
    private double hold_cost, park_cost, walk_time;
    
    public static final Zone NULL = new Zone(0, 1, 0, Integer.MAX_VALUE, 0);
    
    public Zone(int id, double p, double hold_cost, double park_cost, double walk_time)
    {
        super(id);
        this.p = p;
        this.hold_cost = hold_cost;
        this.park_cost = park_cost;
        this.walk_time = walk_time;
    }
    
    public void setHoldCost(double h)
    {
        this.hold_cost = h;
    }
    
    public void setWalkTime(double w)
    {
        walk_time = w;
    }
    
    public double getWalkTime()
    {
        return walk_time;
    }
    
    public void setPrPark(double p)
    {
        this.p = p;
    }
    
    public double getHoldCost()
    {
        return hold_cost;
    }
    
    public double getParkCost()
    {
        return park_cost;
    }
    
    public double getPrPark()
    {
        return p;
    }
}
