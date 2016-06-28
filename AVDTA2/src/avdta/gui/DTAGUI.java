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
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileView;
import avdta.network.ReadNetwork;

/**
 *
 * @author micha
 */
public class DTAGUI extends GUI
{
    protected DTAProject project;
    private NetworkPane networkPane;
    private DemandPane demandPane;
    
    
    private JMenuItem cloneMI;
    
    public DTAGUI()
    {
        
        
        final JFrame frame = this;

        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());


        
        JTabbedPane tabs = new JTabbedPane();
        
        networkPane = new NetworkPane();
        
        tabs.add("Network", networkPane);
        tabs.add("Demand", demandPane);
        
        
        constrain(p, tabs, 0, 0, 1, 1);
        
        
        
        
        
        
        
        
        
        add(p);
        
        
        
        
        
        JMenuBar menu = new JMenuBar();
        JMenu me;
        JMenuItem mi;
        
        me = new JMenu("File");
        mi = new JMenuItem("New");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                newProject();
            }
        });
        me.add(mi);
        
        mi = new JMenuItem("Open");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                openProject();
            }
        });
        me.add(mi);
        
        mi = new JMenuItem("Clone");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cloneProject();
            }
        });
        me.add(mi);
        
        cloneMI = mi;
        cloneMI.setEnabled(false);
        
        menu.add(me);
        
        me = new JMenu("About");
        mi = new JMenuItem("Version");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(frame, "Version "+Version.getVersion()+"\nCopyright © 2014 by "+Version.getAuthor(), 
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
    
    public void openProject(DTAProject project) throws IOException
    {
        this.project = project;
        
        
        cloneMI.setEnabled(project != null);
        project.loadSimulator();
        
        networkPane.setProject(project);
        demandPane.setProject(project);
        
        
        if(project != null)
        {
            setTitle(project.getName()+" - AVDTA");
        }
        
        
    }
    
    public void cloneProject()
    {
        
    }
    
    public void openProject()
    {
        ProjectFileView view = new ProjectFileView("DTA");
        
        JFileChooser chooser = new JFileChooser(new File("networks/"))
        {
            public boolean accept(File file)
            { 
               return view.isProject(file) < 2;
            }
        };
        
        
        
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileView(view);
        
        int returnVal = chooser.showDialog(this, "Open network");
        
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
    
    public void newProject()
    {
        ProjectFileView view = new ProjectFileView("DTA");
        
        JFileChooser chooser = new JFileChooser(new File("networks/"))
        {
            public boolean accept(File file)
            {
                return view.isProject(file) == 0;
            }
        };
        
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileView(view);
        
        
        int returnVal = chooser.showDialog(this, "Select folder");
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            File dir = chooser.getSelectedFile();
            
            String name = JOptionPane.showInputDialog(this, "What do you want to name this project? ", "Project name", 
                    JOptionPane.QUESTION_MESSAGE);
            
            try
            {
                DTAProject project = new DTAProject();
                project.createProject(name, new File(dir.getCanonicalPath()+"/"+name));
                openProject(project);
            }
            catch(IOException ex)
            {
                handleException(ex);
            }
        }
    }
}
