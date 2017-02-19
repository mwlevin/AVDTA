/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import avdta.network.Simulator;

/**
 *
 * @author ml26893
 */
public class TECConflictRegion 
{
    private int id;
    
    private double y;
    
    public TECConflictRegion(int id)
    {
        y = 0;
        
       this.id = id;
    }
    
    public boolean validate()
    {
        return y <= 1.0;
    }
    
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public void addY(Cell i, Cell j)
    {
        y += 1.0/ (i.getLink().getCellCapacity() / Simulator.dt);
    }
    
    public boolean canAddY(Cell i, Cell j)
    {
        return y + 1.0/(i.getLink().getCellCapacity() / Simulator.dt) <= 1.0;
    }
}
