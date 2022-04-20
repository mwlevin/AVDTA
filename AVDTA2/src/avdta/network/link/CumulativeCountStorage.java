/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.link;

/**
 *
 * @author mlevin
 */
public interface CumulativeCountStorage 
{
    public void addCC(int idx, int val);
    public int getCC(int idx);
    
    public void nextTimeStep();
    public void clear();
}
