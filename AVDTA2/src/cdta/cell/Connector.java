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
public abstract class Connector 
{
    
    
    public abstract int getY(Cell i, Cell j);
    
    public abstract boolean isConnected(Cell i, Cell j);
    
    public abstract void setReservationConnectivity(Cell i, Cell j, boolean connect);
    public abstract void setCongestionConnectivity(Cell i, Cell j, boolean connect);
}
