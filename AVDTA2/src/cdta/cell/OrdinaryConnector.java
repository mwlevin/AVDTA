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
public class OrdinaryConnector extends Connector
{
    private boolean reservation, congestion;
    
    private int y;
    
    public OrdinaryConnector()
    {
        
    }
    
    public boolean isConnected(Cell i, Cell j)
    {
        return reservation && congestion;
    }
    
    public int getY(Cell i, Cell j)
    {
        return y;
    }
    
    public void addY(Cell i, Cell j)
    {
        y ++;
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
