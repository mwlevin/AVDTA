/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

/**
 *
 * @author ml26893
 */
public interface AbstractGUIPanel 
{
    public void parentReset();
    
    public void parentSetEnabled(boolean e);
    
    public void reset();
    public void setEnabled(boolean e);
}
