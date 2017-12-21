/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node.obj;

/**
 *
 * @author mlevin
 */
public class PressureTerm 
{
    public int x; // queue length
    public double p; // turning proportion
    
    
    public PressureTerm(int x, double p)
    {
        this.x = x;
        this.p = p;
    }
    
    public String toString()
    {
        return "("+x+", "+p+")";
    }
}
