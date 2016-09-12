/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;

/**
 *
 * @author ml26893
 */
public class TurnRecord 
{
    private int i, j;
    
    public TurnRecord(Turn t)
    {
        this(t.i, t.j);
    }
    
    public TurnRecord(int i, int j)
    {
        this.i = i;
        this.j = j;
    }
    
    public TurnRecord(Link i, Link j)
    {
        this(i.getId(), j.getId());
    }
    
    public void setI(int i)
    {
        this.i = i;
    }
    
    public void setJ(int j)
    {
        this.j = j;
    }
    
    public void setI(Link l)
    {
        this.i = l.getId();
    }
    
    public void setJ(Link l)
    {
        this.j = l.getId();
    }
    
    public int getI()
    {
        return i;
    }
    
    public int getJ()
    {
        return j;
    }
}
