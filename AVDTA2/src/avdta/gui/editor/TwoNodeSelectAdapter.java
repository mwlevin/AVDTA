/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.node.Node;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author micha
 */
public class TwoNodeSelectAdapter extends SelectAdapter
{
    private Node n1, n2;
    private Component parent;
    
    public TwoNodeSelectAdapter(Component parent)
    {
        n1 = null;
        n2 = null;
        this.parent = parent;
    }
    
    public void nodeSelected(Node n)
    {
        if(n1 == null)
        {
            n1 = n;
            firstNodeSelected(n1);
        }
        else
        {
            if(n == n1)
            {
                JOptionPane.showMessageDialog(parent, "Select a different node.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                n2 = n;
                nodesSelected(n1, n2);
            }
        }
    }
    
    public void nodesSelected(Node n1, Node n2){}
    public void firstNodeSelected(Node n){}
}
