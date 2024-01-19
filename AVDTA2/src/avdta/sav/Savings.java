/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

/**
 * This class stores and compares the savings associated with traveling from i to j.
 * @author Michael
 */
public class Savings implements Comparable<Savings>
{
    public int tt;
    public SAVDest i, j;

    public Savings(SAVDest i, SAVDest j, int tt)
    {
        this.i = i;
        this.j = j;
        this.tt = tt;
    }

    public int compareTo(Savings rhs)
    {
        return rhs.tt - tt;
    }
}
    

