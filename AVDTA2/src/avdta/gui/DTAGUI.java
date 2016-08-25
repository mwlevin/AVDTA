/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.gui.util.ProjectChooser;
import avdta.gui.util.Version;
import avdta.gui.panels.DTAPane;
import avdta.gui.panels.DemandPane;
import avdta.gui.panels.NetworkPane;
import avdta.dta.DTASimulator;
import avdta.gui.panels.TransitPane;
import avdta.project.DTAProject;
import java.awt.GridBagLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import static avdta.gui.util.GraphicUtils.*;
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
import avdta.project.Project;

/**
 *
 * @author micha
 */
public class DTAGUI extends GUI
{
    protected DTAProject project;
    private NetworkPane networkPane;
    private DemandPane demandPane;
    private DTAPane dtaPane;
    private TransitPane transitPane;
    
    
    
    
    public DTAGUI()
    {
        
        
        final JFrame frame = this;

        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());


        
        JTabbedPane tabs = new JTabbedPane();
        
        networkPane = new NetworkPane();
        demandPane = new DemandPane();
        transitPane = new TransitPane();
        dtaPane = new DTAPane();
        
        tabs.add("Network", networkPane);
        tabs.add("Demand", demandPane);
        tabs.add("Transit", transitPane);
        tabs.add("DTA", dtaPane);
        
        constrain(p, tabs, 0, 0, 1, 1);
        
        
        
        
        
        
        
        
        
        add(p);
        pack();
        setResizable(false);
        

        setLocationRelativeTo(null);

        
        
        setVisible(true);
    }
    
    public void closeProject()
    {
        try
        {
            openProject(null);
        }
        catch(IOException ex)
        {
            GUI.handleException(ex);
        }
    }
    
    public void openProject(Project p) throws IOException
    {
        super.openProject(p);
        
        
        DTAProject project = (DTAProject)p;
        this.project = project;
        
        
        cloneMI.setEnabled(project != null);
        closeMI.setEnabled(project != null);
        
        if(project != null)
        {
            setTitle(project.getName()+" - AVDTA");
        }
        
        
        networkPane.setProject(project);
        demandPane.setProject(project);
        dtaPane.setProject(project);
        transitPane.setProject(project);
        
        
        
        
        
    }
    
    public void cloneProject()
    {
        JFileChooser chooser = new ProjectChooser(new File(GUI.getDefaultDirectory()));
        
        
        
        int returnVal = chooser.showDialog(this, "Select folder");
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            File dir = chooser.getSelectedFile();
            
            String name = JOptionPane.showInputDialog(this, "What do you want to name this project? ", "Project name", 
                    JOptionPane.QUESTION_MESSAGE);
            
            if(name != null)
            {
                try
                {
                    DTAProject rhs = new DTAProject();
                    rhs.createProject(name, new File(dir.getCanonicalPath()+"/"+name));
                    rhs.cloneFromProject(project);
                }
                catch(IOException ex)
                {
                    handleException(ex);
                }
            }
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
    
    public void newProject()
    {
        JFileChooser chooser = new ProjectChooser(new File(GUI.getDefaultDirectory()));
               
        int returnVal = chooser.showDialog(this, "Select folder");
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            File dir = chooser.getSelectedFile();
            
            String name = JOptionPane.showInputDialog(this, "What do you want to name this project? ", "Project name", 
                    JOptionPane.QUESTION_MESSAGE);
            
            if(name != null)
            {
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
    
    public void reset()
    {
        networkPane.reset();
        demandPane.reset();
        dtaPane.reset();
        transitPane.reset();
    }
    
    public void createDatabase()
    {
        try
        {
            project.createDatabase();
        }
        catch(Exception ex)
        {
            GUI.handleException(ex);
        }
    }
}
