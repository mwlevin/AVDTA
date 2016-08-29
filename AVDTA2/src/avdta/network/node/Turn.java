/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

import avdta.network.link.Link;

/**
 * This class just defines a data type to store a turn i.e. a pair of {@link Link}
 * data-types ({@code i} and {@code j}).
 * @author Michael
 */
public class Turn implements java.io.Serializable, Comparable<Turn>
{
    public Link i, j;
    /**
     * Instantiates the turn with incoming link {@code i} and outgoing link 
     * {@code j}.
     * @param i Incoming link for that turn.
     * @param j Outgoing link for that turn.
     */
    public Turn(Link i, Link j)
    {
        this.i = i;
        this.j = j;
    }
    
    public int compareTo(Turn rhs)
    {
        if(rhs.i != i)
        {
            return i.getId() - rhs.i.getId();
        }
        else
        {
            return j.getId() - rhs.j.getId();
        }
    }
    
    public String toString()
    {
        return ""+i.getId()+"-"+j.getId();
    }
    
    public int hashCode()
    {
        return i.getId()+j.getId();
    }
    
    public boolean equals(Turn rhs)
    {
        return i == rhs.i && j == rhs.j;
    }
}
