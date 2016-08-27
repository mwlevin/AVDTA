/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.link.Link;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author micha
 */
public class EditLink extends JPanel
{
    private Editor editor;
    private Link prev;
    
    private JTextField id, source, dest, capacity, ffspd, wavespd, length;
    private JComboBox numLanes, flowModel;
    
    public EditLink(Editor editor, Link prev)
    {
        
    }
    
    
    
    public void saveLink(Link prev, Link newLink)
    {
        editor.saveLink(prev, newLink);
    }
}
