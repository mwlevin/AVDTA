/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta;

import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Zone;
import cdta.cell.Cell;
import cdta.cell.SinkCell;
import cdta.cell.SourceCell;

/**
 *
 * @author ml26893
 */
public class TECConnector extends TECLink
{
    private boolean source;
    private int id;
    
    public TECConnector(CentroidConnector rhs)
    {
        super(rhs);
        
        if(rhs.getSource() instanceof Zone)
        {
            source = true;
            id = rhs.getSource().getId();
        }
        else
        {
            source = false;
            id = rhs.getDest().getId();
        }
        
    }
    
    public boolean isOrigin()
    {
        return source;
    }
    
    public boolean isDest()
    {
        return !isOrigin();
    }
    
    public Cell getCell(int t)
    {
        return super.getCell(0, t);
    }
    
    public int getZoneId()
    {
        return id;
    }
    
    
    public int getNumCells(Link link) 
    {
        return 1;
    }
    
    public Cell createCell(int c, int t)
    {
        if(isOrigin())
        {
            return new SourceCell(0, id, t);
        }
        else
        {
            return new SinkCell(0, id, t);
        }
    }
}
