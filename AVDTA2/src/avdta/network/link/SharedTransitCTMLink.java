/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

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
    public SharedTransitCTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, double jamd, double length, int numLanes)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes-1);        
    }
    
    public double getTransitCapacityPerLane()
    {
        return getCapacityPerLane();
    }
    
    public double getTransitWaveSpeed()
    {
        return getWaveSpeed();
    }
    
    public double getTransitCellJamdPerLane()
    {
        return getCellJamdPerLane();
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
}
