/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.gui.editor.Editor;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;

/**
 *
 * @author ml26893
 */
public class Start extends JFrame
{
    public Start()
    {
        setTitle(GUI.getTitleName());
        setIconImage(GUI.getIcon());
        
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        
        
        JButton dta = new JButton("DTA");
        
        JButton editor = new JButton("Editor");
        
        
        dta.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                new DTAGUI();
            }
        });
        
        editor.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                new Editor();
            }
        });
        
        constrain(p, dta, 0, 0, 1, 1);
        constrain(p, editor, 1, 0, 1, 1);
        
        add(p);
        
        p.setPreferredSize(new Dimension(300, 300));
        
        
        pack();
        setResizable(false);
        
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        
        setVisible(true);
    }
}
