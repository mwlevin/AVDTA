/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

import avdta.network.ReadNetwork;
import avdta.network.link.cell.Cell;
import avdta.network.link.cell.SharedTransitCell;
import avdta.network.link.cell.SharedTransitEndCell;
import avdta.network.link.cell.SharedTransitLinkCell;
import avdta.network.link.cell.SharedTransitStartCell;
import avdta.network.node.Node;

/**
 *
 * @author micha
 */
public class SharedTransitCTMLink extends CTMLink
{
    private TransitLane transitLane;
    
    public SharedTransitCTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, 
            double jamd, double length, int numLanes, TransitLane transitLane)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);  
        this.transitLane = transitLane;
    }
    
    public TransitLane getTransitLane()
    {
        return transitLane;
    }
    
    public int getType()
    {
        return ReadNetwork.CTM + ReadNetwork.SHARED_TRANSIT;
    }
    
    public void tieCells()
    {
        Cell[] cells = getCells();
        Cell[] transitCell = transitLane.getCells();
        
        for(int i = 0; i < cells.length; i++)
        {
            ((SharedTransitCell)cells[i]).setTransitLane(transitCell[i]);
        }
    }
    
    public Cell createEndCell(Cell prev)
    {
        return new SharedTransitEndCell((SharedTransitCell)prev, this);
    }
    
    public Cell createStartCell()
    {
        return new SharedTransitStartCell(this);
    }
    
    public Cell createCell(Cell prev)
    {
        return new SharedTransitLinkCell((SharedTransitCell)prev, this);
    }
    
    public void prepare()
    {
        updateLanes();
        
        super.prepare();
    }
    
    public void updateLanes()
    {
        for(Cell c : cells)
        {
            ((SharedTransitCell)c).updateLanes();
        }
    }
    
}
