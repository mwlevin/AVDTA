/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

/**
 *
 * @author micha
 */
public class SourceCell extends Cell
{
    private int zoneId;
    
    public SourceCell(int id, int zoneId, int t)
    {
        super(null, id,  t);
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
