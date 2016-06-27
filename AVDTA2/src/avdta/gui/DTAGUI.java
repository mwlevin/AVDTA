/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.dta.DTASimulator;
import avdta.project.DTAProject;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import static avdta.gui.GraphicUtils.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 *
 * @author micha
 */
public class DTAGUI extends GUI
{
    protected DTAProject project;
    private NetworkPane networkPane;
    
    private JTextField projectTxt, typeTxt;
    
    public DTAGUI()
    {
        
        
        final JFrame frame = this;

        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());

        
        projectTxt = new JTextField(20);
        projectTxt.setEnabled(false);
        projectTxt.setBorder(null);
        projectTxt.setBackground(getBackground());
        
        typeTxt = new JTextField(20);
        typeTxt.setEnabled(false);
        typeTxt.setBorder(null);
        typeTxt.setBackground(getBackground());
        
        
        JTabbedPane tabs = new JTabbedPane();
        
        networkPane = new NetworkPane();
        
        tabs.add("Network", networkPane);
        
        constrain(p, projectTxt, 0, 0, 1, 1);
        constrain(p, typeTxt, 1, 0, 1, 1);
        constrain(p, tabs, 0, 1, 2, 1);
        
        
        
        
        
        
        
        
        
        add(p);
        
        
        
        
        
        JMenuBar menu = new JMenuBar();
        JMenu me;
        JMenuItem mi;
        me = new JMenu("About");
        mi = new JMenuItem("Version");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(frame, "Version "+Version.getVersion()+"\nCopyright "+Version.getAuthor(), 
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        me.add(mi);
        menu.add(me);
        
        this.setJMenuBar(menu);
        
        
        
        
        
        
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
    
    public void openProject(DTAProject project)
    {
        this.project = project;
        
        networkPane.setProject(project);
    }
}
