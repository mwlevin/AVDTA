/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import avdta.network.Simulator;
import cdta.TECLink;

/**
 *
 * @author micha
 */
public class Cell implements Comparable<Cell>
{
    private int id;
    
    private SameCellConnector sameCell;
    private Connector nextCell, prevCell;
    
    private int t;
    
    private TECLink link;
    
    private int n;
    
    
    
    
    public Cell prev;
    public boolean label;
    public boolean added;
    
    public Cell(TECLink link, int id, int t)
    {
        this.t = t;
        this.link = link;
        this.id = id;
    }
    
    public String toString()
    {
        return "("+link.getId()+","+t+")";
    }
    public int compareTo(Cell rhs)
    {

        if(t != rhs.t)
        {
            return t - rhs.t;
        }
        else if(link.getId() != rhs.link.getId())
        {
            return link.getId() - rhs.link.getId();
        }
        else
        {
            return id - rhs.id;
        }
    }
    
    public int getZoneId()
    {
        return 0;
    }
    
    public TECLink getLink()
    {
        return link;
    }
    
    public int getTime()
    {
        return t;
    }
    
    public int getSendingFlow()
    {
        return (int)Math.min(getCapacity(), n);
    }
    
    public void setN(int n)
    {
        this.n = n;
    }
    
    public int getN()
    {
        return n;
    }
    
    public void addN()
    {
        this.n ++;
    }
    
    public void setId(int id)
    {
        this.id = id;
    }
    
    public int getId()
    {
        return id;
    }
    
    public int hashCode()
    {
        return id;
    }

    public SameCellConnector getSameCellConnector()
    {
        return sameCell;
    }
    
    public void setSameCellConnector(SameCellConnector c)
    {
        sameCell = c;
    }
    
    public Connector getNextCellConnector()
    {
        return nextCell;
    }
    
    public void setNextCellConnector(Connector c)
    {
        nextCell = c;
    }
    
    public Connector getPrevCellConnector()
    {
        return prevCell;
    }
    
    public void setPrevCellConnector(Connector c)
    {
        prevCell = c;
    }

    public double getLength()
    {
        return link.getCellLength();
    }
    

    public int getJamD()
    {
        return link.getCellJamD();
    }
    

    public int getCapacity()
    {
        return link.getCellCapacity();
    }
    
    public double getReceivingFlow()
    {
        return Math.min(getCapacity(), getMesoDelta() * (getJamD() - n));
    }
    
    public double getMesoDelta()
    {
        return link.getMesoDelta();
    }
}
