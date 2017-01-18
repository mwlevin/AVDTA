/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

import avdta.network.Simulator;
import avdta.network.link.Link;
import cdta.cell.StartCell;
import cdta.cell.Cell;
import cdta.cell.Connector;
import cdta.cell.OrdinaryConnector;
import cdta.cell.SameCellConnector;
import java.util.Map;

/**
 *
 * @author micha
 */
public class TECLink 
{
    private int capacity, jamd, id; 
    private double meso_delta, length;
    
    private int numCells;
    
    private Cell[][] cells;

    
    public TECLink(Link link)
    {
        length = link.getFFSpeed() * Simulator.dt / 3600.0;
        numCells = getNumCells(link);
        capacity =(int)Math.round(link.getCapacity() * Simulator.dt/3600.0);
        jamd = (int)Math.round(link.getJamDensity() * length);
        
        meso_delta = link.getWaveSpeed() / link.getFFSpeed();
        
        this.id = link.getId();
        
        
    }

    
    public Cell getFirstCell(int t)
    {
        return getCell(0, t);
    }
    
    public Cell getLastCell(int t)
    {
        return getCell(numCells-1, t);
    }
    
    public Cell getCell(int num, int t)
    {
        return cells[num][t];
    }
    
    public int getNumCells()
    {
        return numCells;
    }
    
    public int getNumCells(Link link)
    {
        return (int)Math.max(2, Math.round(link.getLength() / length));
    }
    
    public void createCells(int T)
    {
        cells = new Cell[numCells][T];
        
        for(int c = 0; c < numCells; c++)
        {
            for(int t = 0; t < T; t++)
            {
                cells[c][t] = createCell(c, t);
            }
        }
        
        for(int c = 0; c < numCells-1; c++)
        {
            for(int t = 0; t < T-1; t++)
            {
                cells[c][t].setSameCellConnector(new SameCellConnector(cells[c][t], cells[c][t+1]));
                cells[c][t].setNextCellConnector(new OrdinaryConnector(cells[c][t], cells[c+1][t+1]));
            }
        }
        
        for(int t = 0; t < T-1; t++)
        {
            cells[numCells-1][t].setSameCellConnector(new SameCellConnector(cells[numCells-1][t], cells[numCells-1][t+1]));
        }
    }
    
    public Cell createCell(int c, int t)
    {
        if(c == 0)
        {
            return new StartCell(this, c, t);
        }
        else
        {
            return new Cell(this, c, t);
        }
    }
    
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }
    
    public int getCellCapacity()
    {
        return capacity;
    }
    
    public int getCellJamD()
    {
        return jamd;
    }
    
    public double getCellLength()
    {
        return length;
    }
    
    public double getMesoDelta()
    {
        return meso_delta;
    }
}
