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
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 *
 * @author ml26893
 */
public class Start extends JFrame
{
    private JList list;
    
    private static final String[] options = new String[]{"DTA", "Four-step", "SAV", "Editor"};
    
    private static final int DTA = 0;
    private static final int FOURSTEP = 1;
    private static final int SAV = 2;
    private static final int EDITOR = 3;
    
    public Start()
    {
        setTitle(GUI.getTitleName());
        setIconImage(GUI.getIcon());
        
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        list = new JList(options);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(DTA);
        
        JButton start = new JButton("Start");
        
        
        start.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                int idx = list.getSelectedIndex();
                
                switch(idx)
                {
                    case DTA:
                        new DTAGUI();
                        break;
                    case FOURSTEP:
                        new FourStepGUI();
                        break;
                    case SAV:
                        new SAVGUI();
                        break;
                    case EDITOR:
                        new Editor();
                        break;
                }
                
            }
        });
        

        
        constrain(p, new JScrollPane(list), 0, 0, 1, 1);
        constrain(p, start, 1, 0, 1, 1, GridBagConstraints.CENTER);
        
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
