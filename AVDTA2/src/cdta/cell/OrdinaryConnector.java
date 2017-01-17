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
public class OrdinaryConnector extends SameCellConnector
{
    private int y;
    
    public OrdinaryConnector()
    {
        
    }
    
    
    public int getY(Cell i, Cell j)
    {
        return y;
    }
    
    public void addY(Cell i, Cell j)
    {
        y ++;
    }
}
