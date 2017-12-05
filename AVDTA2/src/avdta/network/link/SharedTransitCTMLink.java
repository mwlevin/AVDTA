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
import avdta.network.type.Type;

/**
 * This is a {@link CTMLink} with a dynamic transit lane, which adds lanes to cells when transit is not present.
 * @author Michael
 */
public class SharedTransitCTMLink extends CTMLink implements AbstractSplitLink
{
    private TransitLane transitLane;
    
    /**
     * Constructs the link with the given parameters 
     * @param id the link id
     * @param source the source node
     * @param dest the destination node
     * @param capacity the capacity per lane (veh/hr)
     * @param ffspd the free flow speed (mi/hr)
     * @param wavespd the congested wave speed (mi/hr)
     * @param jamd the jam density (veh/mi)
     * @param length the length (mi)
     * @param numLanes the number of lanes (not including the transit lane)
     * @param transitLane the transit lane
     */
    public SharedTransitCTMLink(int id, Node source, Node dest, double capacity, double ffspd, double wavespd, 
            double jamd, double length, int numLanes, TransitLane transitLane)
    {
        super(id, source, dest, capacity, ffspd, wavespd, jamd, length, numLanes);  
        this.transitLane = transitLane;
    }
    

    
    /**
     * Returns the transit lane
     * @return the transit lane
     */
    public TransitLane getTransitLane()
    {
        return transitLane;
    }
    
    /**
     * Returns the type code for this link
     * @return {@link ReadNetwork#CTM}+{@link ReadNetwork#SHARED_TRANSIT}
     */
    public Type getType()
    {
        return ReadNetwork.SHARED_TRANSIT;
    }
    
    /**
     * Links cells on the main link with cells on the transit lane. The transit lane shares a lane with the main link when transit is not present.
     */
    public void tieCells()
    {
        
        Cell[] cells = getCells();
        Cell[] transitCell = transitLane.getCells();
        
        for(int i = 0; i < cells.length; i++)
        {
            ((SharedTransitCell)cells[i]).setTransitLane(transitCell[i]);
        }
    }
    
    /**
     * Overrides to create a {@link SharedTransitEndCell}
     * @param prev the previous cell
     * @return the appropriate type of end cell
     */
    public Cell createEndCell(Cell prev)
    {
        return new SharedTransitEndCell((SharedTransitCell)prev, this);
    }
    
    /**
     * Overrides to create a {@link SharedTransitStartCell}
     * @return the appropriate type of start cell
     */
    public Cell createStartCell()
    {
        return new SharedTransitStartCell(this);
    }
    
    /**
     * Overrides to create a {@link SharedTransitLinkCell}
     * * @param prev the previous cell
     * @return the appropriate type of link cell
     */
    public Cell createCell(Cell prev)
    {
        return new SharedTransitLinkCell((SharedTransitCell)prev, this);
    }
    

    /**
     * Prepare to calculate transition flows and move vehicles.
     * This also updates the number of lanes available to each cell.
     */
    public void prepare()
    {
        updateLanes();
        
        super.prepare();
    }
    
    private void updateLanes()
    {
        for(Cell c : cells)
        {
            ((SharedTransitCell)c).updateLanes();
        }
    }
    
    /**
     * Creates a {@link LinkRecord} for manipulating data and writing it to the data file.
     * This is identical to {@link Link#createLinkRecord()} except the number of lanes is incremented by 1.
     * @return a {@link LinkRecord} version of this link's data
     */
    public LinkRecord createLinkRecord()
    {
        return new LinkRecord(getId(), getType().getCode(), getSource().getId(), getDest().getId(), getLength(), getFFSpeed(), getWaveSpeed(), 
                getCapacityPerLane(), getNumLanes()+1);
    }
    
}
