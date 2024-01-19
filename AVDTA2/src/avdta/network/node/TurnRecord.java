/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;

/**
 * A record of a {@link Turn} that assists in the representation and manipulation of {@link PhaseRecord} data. 
 * This class contains the ids of the {@link Link}s of the associated {@link Turn}.
 * @author Michael
 */
public class TurnRecord 
{
    private int i, j;
    
    /**
     * Constructs this {@link TurnRecord} based on the specified {@link Turn}.
     * @param t the {@link Turn} to be cloned
     */
    public TurnRecord(Turn t)
    {
        this(t.i, t.j);
    }
    
    /**
     * Constructs this {@link TurnRecord} based on the specified {@link Link} ids.
     * @param i the id of the incoming {@link Link}
     * @param j the id of the outgoing {@link Link}
     */
    public TurnRecord(int i, int j)
    {
        this.i = i;
        this.j = j;
    }
    
    /**
     * Constructs this {@link TurnRecord} bsaed on the specified {@link Link}s.
     * @param i the incoming {@link Link}
     * @param j the outgoing {@link Link}
     */
    public TurnRecord(Link i, Link j)
    {
        this(i.getId(), j.getId());
    }
    
    /**
     * Updates the incoming {@link Link}
     * @param i the id of the new incoming {@link Link}
     */
    public void setI(int i)
    {
        this.i = i;
    }
    
     /**
     * Updates the outgoing {@link Link}
     * @param j the id of the new outgoing {@link Link}
     */
    public void setJ(int j)
    {
        this.j = j;
    }
    
    /**
     * Updates the incoming {@link Link}
     * @param l the new incoming {@link Link}
     */
    public void setI(Link l)
    {
        this.i = l.getId();
    }
    
    /**
     * Updates the outgoing {@link Link}
     * @param l the new outgoing {@link Link}
     */
    public void setJ(Link l)
    {
        this.j = l.getId();
    }
    
    /**
     * Returns the id of the incoming {@link Link}
     * @return the id of the incoming {@link Link}
     */
    public int getI()
    {
        return i;
    }
    
    /**
     * Returns the id of the outgoing {@link Link}
     * @return the id of the outgoing {@link Link}
     */
    public int getJ()
    {
        return j;
    }
}
