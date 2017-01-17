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
public class SameCellConnector extends Connector
{
    private boolean reservation, congestion;
    
    public SameCellConnector()
    {
        
    }
    
    public void initConnectivity()
    {
        reservation = true;
        congestion = false;
    }
    
    public boolean isConnected(Cell i, Cell j)
    {
        return reservation && congestion;
    }
    
    public int getY(Cell i, Cell j)
    {
        return 0;
    }
    
    public void addY(Cell i, Cell j)
    {
        // do nothing
    }
    
    public void setReservationConnectivity(Cell i, Cell j, boolean connect)
    {
        reservation = connect;
    }
    
    public void setCongestionConnectivity(Cell i, Cell j, boolean connect)
    {
        congestion = connect;
    }

}
