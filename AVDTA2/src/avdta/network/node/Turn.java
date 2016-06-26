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
public class Turn implements java.io.Serializable
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
}
