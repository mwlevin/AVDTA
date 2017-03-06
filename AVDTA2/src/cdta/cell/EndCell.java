/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import cdta.TECLink;

/**
 *
 * @author micha
 */
public class EndCell extends Cell
{
    private int L;
    
    public EndCell(TECLink link, int id, int t, int capacity, int jamd)
    {
        super(link, id, t, capacity, jamd);
        L = link.getNumLanes();
    }
    
    
    public void laneBlocked()
    {
        L = (int)Math.max(0, L-1);
    }
    
    public double getLaneRatio()
    {
        return (double) L / getLink().getNumLanes();
    }
}
