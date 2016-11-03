/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.dta.Assignment;
import avdta.dta.DTAResults;
import avdta.gui.util.ProjectChooser;
import avdta.Version;
import avdta.gui.panels.DTAPane;
import avdta.gui.panels.DemandPane;
import avdta.gui.panels.NetworkPane;
import avdta.dta.DTASimulator;
import avdta.gui.panels.GUIPane;
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
import avdta.project.TransitProject;
import avdta.vehicle.DriverType;

/**
 *
 * @author micha
 */
public class DTAGUI extends GUI implements GUIPane
{
   
    private NetworkPane networkPane;
    private DemandPane demandPane;
    private DTAPane dtaPane;
    private TransitPane transitPane;
    
    private JMenuItem lastAssignment;
    
    
    public DTAGUI()
    {

        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());


        
        JTabbedPane tabs = new JTabbedPane();
        
        networkPane = new NetworkPane(this);
        demandPane = new DemandPane(this);
        transitPane = new TransitPane(this);
        dtaPane = new DTAPane(this);
        
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
    
    public void parentReset()
    {
        dtaPane.reset();
        demandPane.reset();
        networkPane.reset();
        transitPane.reset();
    }
    
    public void parentSetEnabled(boolean e)
    {
        dtaPane.setEnabled(e);
        demandPane.setEnabled(e);
        networkPane.setEnabled(e);
        transitPane.setEnabled(e);
    }
    
    public JMenuBar createMenuBar()
    {
        final JFrame frame = this;
        
        JMenuBar menu = super.createMenuBar();
        
        JMenu me = new JMenu("DTA");
        lastAssignment = new JMenuItem("Last assignment");
        lastAssignment.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Assignment assign = dtaPane.getMostRecentAssignment();
                
                if(assign == null)
                {
                    JOptionPane.showMessageDialog(frame, "No assignments found", "Last assignment", JOptionPane.ERROR_MESSAGE);
                }
                else
                {
                    DTAResults results = assign.getResults();
                    
                    JOptionPane.showMessageDialog(frame, "Gap: "+String.format("%.2f", results.getGapPercent())+
                            "\nTrips: "+results.getTrips()+
                            "\nNon-exiting: "+results.getNonExiting()+
                            "\nTSTT: "+String.format("%.1f", results.getTSTT())+" hr\nAvg. time: "+String.format("%.2f", results.getAvgTT())+" min",
                            "Last assignment", JOptionPane.PLAIN_MESSAGE);
                }
            }
        });
        me.add(lastAssignment);
        
        lastAssignment.setEnabled(false);
        
        menu.add(me);
        
        return menu;
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
        parentSetEnabled(false);
        super.openProject(p);
        
        DTAProject project = (DTAProject)p;
        this.project = project;
            
        //project.loadSimulator();

        
        if(project != null)
        {
            setTitle(project.getName()+" - "+ GUI.getTitleName());
        }
        else
        {
            setTitle(GUI.getTitleName());
        }
        
        
        networkPane.setProject(project);
        demandPane.setProject(project);
        dtaPane.setProject(project);
        transitPane.setProject(project);
        lastAssignment.setEnabled(project != null);
        
        
        parentSetEnabled(true);
        
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
                    
                    if(project instanceof DTAProject)
                    {
                        rhs.cloneFromProject((DTAProject)project);
                    }
                    else if(project instanceof TransitProject)
                    {
                        rhs.cloneFromProject((TransitProject)project);
                    }
                    else
                    {
                        rhs.cloneFromProject(project);
                    }
                    
                    openProject(rhs);
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
            catch(Exception ex)
            {
                GUI.handleException(ex);
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
