/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.util;

/**
 *
 * @author Michael
 */
public interface StatusUpdate
{
    public abstract void update(double estimate);
    public abstract void update(double estimate, String text);
}