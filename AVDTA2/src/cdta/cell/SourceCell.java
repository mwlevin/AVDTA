/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import cdta.TECConnector;

/**
 *
 * @author micha
 */
public class SourceCell extends Cell
{
    private int zoneId;
    
    public SourceCell(TECConnector link, int id, int zoneId, int t)
    {
        super(link, id,  t);
        this.zoneId = zoneId;
    }
    
    public int getZoneId()
    {
        return zoneId;
    }
    
    public int getJamD()
    {
        return Integer.MAX_VALUE;
    }
    
    public int getCapacity()
    {
        return Integer.MAX_VALUE;
    }
    
    public double getReceivingFlow()
    {
        return Integer.MAX_VALUE;
    }
}
