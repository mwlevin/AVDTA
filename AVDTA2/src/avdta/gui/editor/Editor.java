/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.gui.GUI;
import static avdta.gui.GUI.getIcon;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.gui.util.ProjectChooser;
import avdta.network.Simulator;
import avdta.project.DTAProject;
import avdta.project.Project;
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author ml26893
 */
public class Editor extends JFrame
{
    private MapViewer map;
    private DisplayManager display;
    
    public Editor()
    {
        this(null);
    }
    public Editor(Project project)
    {
        setTitle(GUI.getTitleName());
        setIconImage(getIcon());
        
        display = new DefaultDisplayManager();
        map = new MapViewer(display, 800, 800);
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        JScrollPane scroll = new JScrollPane(map);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        //scroll.getViewport().setViewPosition(new Point(500, 500));
        
        constrain(p, scroll, 0, 0, 1, 1);
        scroll.setWheelScrollingEnabled(false);
        map.setScrollPane(scroll);
        
        
        add(p);
        
        JMenuBar menu = new JMenuBar();
        JMenu me;
        JMenuItem mi;
        if(project == null)
        {
            

            me = new JMenu("File");
            mi = new JMenuItem("New project");


            mi = new JMenuItem("Open project");

            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    openProject();
                }
            });
            me.add(mi);

            me.add(mi);

            menu.add(me);
            
        }
        
        this.setJMenuBar(menu);

        
        pack();
        setResizable(false);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        
        setLocationRelativeTo(null);

        setVisible(true);
        
        if(project != null)
        {
            openProject(project);
        }
    }
    

    public void openProject()
    {
        JFileChooser chooser = new ProjectChooser(new File(GUI.getDefaultDirectory()), "DTA");
        
        int returnVal = chooser.showDialog(this, "Open project");
        
        if(returnVal == chooser.APPROVE_OPTION)
        {
            try
            {
                DTAProject project = new DTAProject(chooser.getSelectedFile());
                
                openProject(project);
            }
            catch(IOException ex)
            {
                JOptionPane.showMessageDialog(this, "The selected folder is not a DTA network", "Invalid network", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void openProject(Project project)
    {
        Simulator sim = project.getSimulator();
        

        map.setNetwork(sim);
        map.repaint();
    }
}
