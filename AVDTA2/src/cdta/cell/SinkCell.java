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
public class SinkCell extends Cell
{
    public SinkCell(int t)
    {
        super(null, t);
    }
    
    public int getJamDensity()
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
