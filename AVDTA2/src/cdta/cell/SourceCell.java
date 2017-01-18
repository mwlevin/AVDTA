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
    public SourceCell(int id, int t)
    {
        super(null, t);
        setId(id);
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
