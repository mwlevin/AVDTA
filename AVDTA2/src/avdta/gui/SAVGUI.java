/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.analysis.AnalysisPanel;
import avdta.gui.panels.demand.DemandPanel;
import avdta.gui.panels.demand.SAVDemandPanel;
import avdta.gui.panels.fourstep.FourStepPanel;
import avdta.gui.panels.network.NetworkPanel;
import avdta.gui.panels.sav.SAVPanel;
import avdta.gui.panels.transit.TransitPanel;
import static avdta.gui.util.GraphicUtils.constrain;
import avdta.project.FourStepProject;
import avdta.project.Project;
import avdta.project.SAVProject;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author micha
 */
public class SAVGUI extends GUI implements AbstractGUIPanel 
{
    private NetworkPanel networkPane;
    private SAVDemandPanel demandPane;
    private TransitPanel transitPane;
    private SAVPanel savPane;
    private AnalysisPanel analysisPane;
    
    
    public SAVGUI()
    {
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());


        
        JTabbedPane tabs = new JTabbedPane();
        
        networkPane = new NetworkPanel(this);
        demandPane = new SAVDemandPanel(this);
        transitPane = new TransitPanel(this);
        savPane = new SAVPanel(this);
        analysisPane = new AnalysisPanel(this);
        
        tabs.add("Network", networkPane);
        tabs.add("Demand", demandPane);
        tabs.add("Transit", transitPane);
        tabs.add("SAV", savPane);
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
        demandPane.reset();
        networkPane.reset();
        transitPane.reset();
        savPane.reset();
        analysisPane.reset();
    }
    
    public void parentSetEnabled(boolean e)
    {
        demandPane.setEnabled(e);
        networkPane.setEnabled(e);
        transitPane.setEnabled(e);
        savPane.setEnabled(e);
        analysisPane.setEnabled(e);
    }
    
    public void reset()
    {
        parentReset();
    }
    
    public SAVProject openProject(File file) throws IOException
    {
        return new SAVProject(file);
                
    }
    
    public void openProject(Project p) throws IOException
    {
        parentSetEnabled(false);
        super.openProject(p);
        
        SAVProject project = (SAVProject)p;
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
        savPane.setProject(project);
        analysisPane.setProject(project);
        
        
        parentSetEnabled(true);
        
    }
    
    public String getProjectType()
    {
        return "SAV";
    }
    
    public Project createEmptyProject()
    {
        return new SAVProject();
    }
}
