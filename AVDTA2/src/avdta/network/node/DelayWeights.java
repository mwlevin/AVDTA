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
public interface DelayWeights 
{
    public abstract double getWeight(Link i, Link j);
}
