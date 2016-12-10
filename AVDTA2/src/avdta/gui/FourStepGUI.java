/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.dta.Assignment;
import avdta.dta.DTAResults;
import static avdta.gui.GUI.handleException;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.analysis.AnalysisPanel;
import avdta.gui.panels.demand.DemandPanel;
import avdta.gui.panels.dta.DTAPanel;
import avdta.gui.panels.fourstep.FourStepPanel;
import avdta.gui.panels.network.NetworkPanel;
import avdta.gui.panels.transit.TransitPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.project.DTAProject;
import avdta.project.FourStepProject;
import avdta.project.Project;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author micha
 */
public class FourStepGUI extends GUI implements AbstractGUIPanel 
{
    private NetworkPanel networkPane;
    private DemandPanel demandPane;
    private TransitPanel transitPane;
    private FourStepPanel fourstepPane;
    private AnalysisPanel analysisPane;
    
    private JMenuItem lastAssignment;
    
    public FourStepGUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());


        
        JTabbedPane tabs = new JTabbedPane();
        
        networkPane = new NetworkPanel(this);
        demandPane = new DemandPanel(this);
        transitPane = new TransitPanel(this);
        fourstepPane = new FourStepPanel(this);
        analysisPane = new AnalysisPanel(this);
        
        tabs.add("Network", networkPane);
        tabs.add("Demand", demandPane);
        tabs.add("Transit", transitPane);
        tabs.add("Four-step", fourstepPane);
        tabs.add("Analysis", analysisPane);
        
        constrain(p, tabs, 0, 0, 1, 1);
        
        
        add(p);
        pack();
        setResizable(false);
        

        setLocationRelativeTo(null);

        
        
        setVisible(true);
    }
    
    public JMenuBar createMenuBar()
    {
        final JFrame frame = this;
        
        JMenuBar menu = super.createMenuBar();
        
        JMenu me = new JMenu("DTA");
        lastAssignment = new JMenuItem("Last assignment");
        
        /*
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
        */
        
        me.add(lastAssignment);
        
        lastAssignment.setEnabled(false);
        
        menu.add(me);
        
        return menu;
    }
    
    public void parentReset()
    {
        demandPane.reset();
        networkPane.reset();
        transitPane.reset();
        analysisPane.reset();
    }
    
    public void parentSetEnabled(boolean e)
    {
        demandPane.setEnabled(e);
        networkPane.setEnabled(e);
        transitPane.setEnabled(e);
        analysisPane.setEnabled(e);
    }
    
    public void reset()
    {
        parentReset();
    }
    
    public void createDatabase()
    {
        if(project == null)
        {
            return;
        }
        try
        {
            project.createDatabase();
        }
        catch(Exception ex)
        {
            GUI.handleException(ex);
        }
    }
    
    
    public FourStepProject openProject(File file) throws IOException
    {
        return new FourStepProject(file);
                
    }
    
    public void openProject(Project p) throws IOException
    {
        parentSetEnabled(false);
        super.openProject(p);
        
        FourStepProject project = (FourStepProject)p;
        this.project = project;
           
        try
        {
            project.loadSimulator();
        }
        catch(Exception ex)
        {
            GUI.handleException(ex);
        }
        
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
        transitPane.setProject(project);
        lastAssignment.setEnabled(project != null);
        analysisPane.setProject(project);
        
        
        parentSetEnabled(true);
        
    }
    
    public String getProjectType()
    {
        return "FourStep";
    }
    
    public Project createEmptyProject()
    {
        return new FourStepProject();
    }
}
