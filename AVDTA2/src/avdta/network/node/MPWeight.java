/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network.node;

/**
 *
 * @author mlevin
 */
public class MPWeight {

    public double calcMPWeight(MPTurn turn)
    {
        return turn.getQueue();
    }
    
}
