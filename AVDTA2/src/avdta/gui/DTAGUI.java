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
import avdta.gui.panels.dta.DTAPanel;
import avdta.gui.panels.demand.DemandPanel;
import avdta.gui.panels.network.NetworkPanel;
import avdta.dta.DTASimulator;
import static avdta.gui.GUI.handleException;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.analysis.AnalysisPanel;
import avdta.gui.panels.transit.TransitPanel;
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
public class DTAGUI extends GUI implements AbstractGUIPanel
{
   
    private NetworkPanel networkPane;
    private DemandPanel demandPane;
    private DTAPanel dtaPane;
    private TransitPanel transitPane;
    private AnalysisPanel analysisPane;
    
    private JMenuItem lastAssignment;
    
    
    public DTAGUI()
    {

        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());


        
        JTabbedPane tabs = new JTabbedPane();
        
        networkPane = new NetworkPanel(this);
        demandPane = new DemandPanel(this);
        transitPane = new TransitPanel(this);
        dtaPane = new DTAPanel(this);
        analysisPane = new AnalysisPanel(this);
        
        tabs.add("Network", networkPane);
        tabs.add("Demand", demandPane);
        tabs.add("Transit", transitPane);
        tabs.add("DTA", dtaPane);
        tabs.add("Analysis", analysisPane);
        
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
        analysisPane.reset();
    }
    
    public void parentSetEnabled(boolean e)
    {
        dtaPane.setEnabled(e);
        demandPane.setEnabled(e);
        networkPane.setEnabled(e);
        transitPane.setEnabled(e);
        analysisPane.setEnabled(e);
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
    
    
    
    public void openProject(Project p) throws IOException
    {
        parentSetEnabled(false);
        super.openProject(p);
        
        DTAProject project = (DTAProject)p;
        this.project = project;
           

        
        if(project != null)
        {
            setTitle(project.getName()+" - "+ GUI.getTitleName());
            
            try
            {
                project.loadSimulator();
            }
            catch(Exception ex)
            {
                GUI.handleException(ex);
            }
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
        analysisPane.setProject(project);
        
        
        parentSetEnabled(true);
        
    }
    
    public Project createEmptyProject()
    {
        return new DTAProject();
    }
    
    public String getProjectType()
    {
        return "DTA";
    }
    
    public DTAProject openProject(File file) throws IOException
    {
        return new DTAProject(file);
                
    }
    
    
    
    public void reset()
    {
        parentReset();
    }
    
    
}
