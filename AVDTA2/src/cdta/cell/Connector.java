/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import java.util.Iterator;

/**
 *
 * @author micha
 */
public abstract class Connector
{
    
    
    public abstract int getY(Cell i, Cell j);
    public abstract void addY(Cell i, Cell j);
    
    public abstract boolean isConnected(Cell i, Cell j);
    
    public abstract void setReservationConnectivity(Cell i, Cell j, boolean connect);
    public abstract void setCongestionConnectivity(Cell i, Cell j, boolean connect);
    public abstract void initConnectivity();
    
    public abstract int sumY();
    public abstract int sumYIn(Cell i);
    public abstract int sumYOut(Cell j);
    public abstract boolean validate();
    
    public abstract Iterator<Cell> iterator(Cell inc);
}
