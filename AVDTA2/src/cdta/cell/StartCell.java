/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cdta.cell;

import cdta.TECLink;

/**
 *
 * @author ml26893
 */
public class StartCell extends Cell
{
    private IntersectionConnector inc;
    
    public StartCell(TECLink link, int id, int t, int capacity, int jamd)
    {
        super(link, id, t, capacity, jamd);
    }
    
    public void setIncConnector(IntersectionConnector e)
    {
        inc = e;
    }
    
    public IntersectionConnector getIncConnector()
    {
        return inc;
    }
}
