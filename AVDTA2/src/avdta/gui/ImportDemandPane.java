/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.dta.DTAImportFromVISTA;
import javax.swing.JPanel;
import avdta.project.DTAProject;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import static avdta.gui.GraphicUtils.*;
import avdta.network.ImportFromVISTA;
import avdta.project.Project;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author micha
 */
public class ImportDemandPane extends JPanel
{
    private DTAProject project;
    private DemandPane parent;
    
    private JFileField importFromProject;
    private JFileField staticOD, dynamicOD, demandProfile, demand;
    private JButton import1, import2;
    
    public ImportDemandPane(DemandPane parent)
    {
        this.parent = parent;
        
        import1 = new JButton("Import");
        import1.setEnabled(false);
        import2 = new JButton("Import");
        import2.setEnabled(false);
        
        FileFilter txtFiles = null;
        
        importFromProject = new JFileField(10, null, "networks/")
        {
            public void valueChanged()
            {
                import1.setEnabled(importFromProject.getFile() != null);
            }
            public File chooseFile()
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
                    return chooser.getSelectedFile();
                }
                else
                {
                    return null;
                }
            }
        };
        demand = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
                import2.setEnabled(dynamicOD.getFile() != null && staticOD.getFile() != null 
                        && demand.getFile() != null && demandProfile != null);
            }
        };
        demandProfile = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
                import2.setEnabled(dynamicOD.getFile() != null && staticOD.getFile() != null 
                        && demand.getFile() != null && demandProfile != null);
            }
        };
        staticOD = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
                import2.setEnabled(dynamicOD.getFile() != null && staticOD.getFile() != null 
                        && demand.getFile() != null && demandProfile != null);
            }
        };
        dynamicOD = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
                import2.setEnabled(dynamicOD.getFile() != null && staticOD.getFile() != null 
                        && demand.getFile() != null && demandProfile != null);
            }
        };
        
        
        import1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    importFromProject();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        
        import2.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    importFromVISTA();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        
        setLayout(new GridBagLayout());
        
        
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, new JLabel("Import network from project"), 0, 0, 3, 1);
        constrain(p, new JLabel("Project: "), 0, 1, 1, 1);
        constrain(p, importFromProject, 1, 1, 1, 1);
        constrain(p, import1, 2, 1, 1, 1);
        
        constrain(this, p, 0, 1, 1, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JLabel("Import from VISTA"), 0, 0, 4, 1);
        constrain(p, new JLabel("Static OD: "), 0, 1, 1, 1);
        constrain(p, staticOD, 1, 1, 1, 1);
        constrain(p, new JLabel("Dynamic OD: "), 0, 2, 1, 1);
        constrain(p, dynamicOD, 1, 2, 1, 1);
        constrain(p, new JLabel("Demand profile: "), 0, 3, 1, 1);
        constrain(p, demandProfile, 1, 3, 1, 1);
        constrain(p, new JLabel("Demand: "), 0, 4, 1, 1);
        constrain(p, demand, 1, 4, 1, 1);
        constrain(p, import2, 2, 1, 1, 4);
        
        constrain(this, p, 0, 2, 1, 1);
        
        disable();
    }
    
    public void importFromProject() throws IOException
    {
        parent.disable();
        
        DTAProject rhs = new DTAProject(importFromProject.getFile());
        
        project.importDemandFromProject(rhs);
        project.loadSimulator();
        
        importFromProject.setFile(null);
        parent.reset();
        
        parent.enable();
    }
    
    public void importFromVISTA() throws IOException
    {
        parent.disable();
        
        DTAImportFromVISTA read = new DTAImportFromVISTA(project, staticOD.getFile(), dynamicOD.getFile(), 
                demandProfile.getFile(), demand.getFile());
        
        project.loadSimulator();
        parent.reset();
        
        staticOD.setFile(null);
        dynamicOD.setFile(null);
        demand.setFile(null);
        demandProfile.setFile(null);
        
        parent.enable();
    }
    
    public void checkForOtherFiles(File f)
    {
        try
        {
            String dir = f.getCanonicalPath();

            dir = dir.substring(0, dir.lastIndexOf("\\".charAt(0))+1);
            

            if(staticOD.getFile() == null)
            {
                File file = new File(dir+"/static_od.txt");
                if(file.exists())
                {
                    staticOD.setFile(file);
                }
            }
            if(dynamicOD.getFile() == null)
            {
                File file = new File(dir+"/dynamic_od.txt");
                if(file.exists())
                {
                    dynamicOD.setFile(file);
                }
            }
            if(demandProfile.getFile() == null)
            {
                File file = new File(dir+"/demand_profile.txt");
                if(file.exists())
                {
                    demandProfile.setFile(file);
                }
            }
            if(demand.getFile() == null)
            {
                File file = new File(dir+"/demand.txt");
                if(file.exists())
                {
                    demand.setFile(file);
                }
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        
    }
    
    public void disable()
    {
        importFromProject.setEnabled(false);
        staticOD.setEnabled(false);
        dynamicOD.setEnabled(false);
        demandProfile.setEnabled(false);
        demand.setEnabled(false);
        import1.setEnabled(false);
        import2.setEnabled(false);
    }
    
    public void enable()
    {
        importFromProject.setEnabled(true);
        staticOD.setEnabled(true);
        dynamicOD.setEnabled(true);
        demandProfile.setEnabled(true);
        demand.setEnabled(true);
        import1.setEnabled(importFromProject.getFile() != null);
        import2.setEnabled(dynamicOD.getFile() != null && staticOD.getFile() != null 
                        && demand.getFile() != null && demandProfile != null);
    }
    
    public void setProject(DTAProject project)
    {
        this.project = project;
        
        if(project == null)
        {
            disable();
        }
        else
        {
            enable();
        }
    }
}
