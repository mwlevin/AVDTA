/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link.cell;

import avdta.network.link.DLRCTMLink;

/**
 *
 * @author micha
 */
public abstract class DLRCell extends Cell
{
    private Cell opposite; 
    
    public DLRCell(DLRCTMLink link)
    {
        super(link);
    }
    
    public void setOppositeCell(Cell o)
    {
        opposite = o;
    }
    
    public Cell getOpposite()
    {
        return opposite;
    }
    
    public double getMaxLanes()
    {
        int total_road_lanes = getLink().getNumLanes();
        
        if(opposite != null)
        {
            total_road_lanes += opposite.getLink().getNumLanes();
        }
        
        if(curr.size() > 0)
        {
            return (int)Math.min(getNumLanes() + 1, total_road_lanes);
        }
        else
        {
            return total_road_lanes;
        }
    }
}
