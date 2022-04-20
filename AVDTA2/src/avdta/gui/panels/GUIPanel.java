/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import javax.swing.JPanel;

/**
 *
 * @author ml26893
 */
public class GUIPanel extends JPanel implements AbstractGUIPanel
{
    private AbstractGUIPanel parent;
    
    public GUIPanel(AbstractGUIPanel parent)
    {
        this.parent = parent;
    }
    
    public void parentReset()
    {
        parent.parentReset();
    }
    
    public void parentSetEnabled(boolean e)
    {
        parent.parentSetEnabled(e);
    }
    
    public void reset()
    {
    }
    public void setEnabled(boolean e)
    {
        super.setEnabled(e);
    }
}
