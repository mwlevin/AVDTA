/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.dta.DTAImportFromVISTA;
import avdta.gui.GUI;
import avdta.gui.util.JFileField;
import avdta.gui.util.ProjectChooser;
import javax.swing.JPanel;
import avdta.project.DTAProject;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import static avdta.gui.util.GraphicUtils.*;
import avdta.network.ImportFromVISTA;
import avdta.project.Project;
import avdta.project.SQLLogin;
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
public class ImportDemandPane extends GUIPanel
{
    
    private JFileField importFromProject;
    private JFileField staticOD, dynamicOD, demandProfile, demand;
    private JButton import1, import2;
    private JButton sqlImport, sqlExport;
    private DTAProject project;
    
    public ImportDemandPane(DemandPane parent)
    {
        super(parent);
        
        import1 = new JButton("Import");
        import1.setEnabled(false);
        import2 = new JButton("Import");
        import2.setEnabled(false);
        
        sqlImport = new JButton("Import from SQL");
        sqlExport = new JButton("Export to SQL");
        
        sqlImport.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    project.importDemandFromSQL();
                }
                catch(Exception ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        
        sqlExport.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    project.exportDemandToSQL();
                }
                catch(Exception ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        
        FileFilter txtFiles = null;
        
        importFromProject = new JFileField(10, null, "projects/")
        {
            public void valueChanged()
            {
                import1.setEnabled(importFromProject.getFile() != null);
            }
            public File chooseFile()
            {
                JFileChooser chooser = new ProjectChooser(new File(GUI.getDefaultDirectory()), "DTA");

                int returnVal = chooser.showDialog(this, "Open projcet");

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
            }
        };
        demandProfile = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);           
            }
        };
        staticOD = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        dynamicOD = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        
        
        import1.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                importFromProject();
            }
        });
        
        import2.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                importFromVISTA();
            }
        });
        
        setLayout(new GridBagLayout());
        
        
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, new JLabel("Import demand from project"), 0, 0, 3, 1);
        constrain(p, new JLabel("Project: "), 0, 1, 1, 1);
        constrain(p, importFromProject, 1, 1, 1, 1);
        constrain(p, import1, 2, 1, 1, 1);
        
        constrain(this, p, 0, 1, 1, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JLabel("Import from VISTA"), 0, 0, 4, 1);
        constrain(p, new JLabel("static OD: "), 0, 1, 1, 1);
        constrain(p, staticOD, 1, 1, 1, 1);
        constrain(p, new JLabel("dynamic OD: "), 0, 2, 1, 1);
        constrain(p, dynamicOD, 1, 2, 1, 1);
        constrain(p, new JLabel("demand profile: "), 0, 3, 1, 1);
        constrain(p, demandProfile, 1, 3, 1, 1);
        constrain(p, new JLabel("demand: "), 0, 4, 1, 1);
        constrain(p, demand, 1, 4, 1, 1);
        constrain(p, import2, 2, 1, 1, 4);
        
        
        constrain(this, p, 0, 2, 1, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, sqlExport, 0, 0, 1, 1);
        constrain(p, sqlImport, 0, 1, 1, 1);
        
        constrain(this, p, 0, 3, 1, 1);
        
        setEnabled(false);
    }
    
    public void importFromProject()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    DTAProject rhs = new DTAProject(importFromProject.getFile());
        
                    project.importDemandFromProject(rhs);
                    project.loadSimulator();

                    importFromProject.setFile(null);
                    parentReset();

                    parentSetEnabled(true);
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        };
        t.start();    
    }
    
    public void importFromVISTA()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    DTAImportFromVISTA read = new DTAImportFromVISTA(project, staticOD.getFile(), dynamicOD.getFile(), 
                    demandProfile.getFile(), demand.getFile());
        
                    project.loadSimulator();
                    parentReset();

                    staticOD.setFile(null);
                    dynamicOD.setFile(null);
                    demand.setFile(null);
                    demandProfile.setFile(null);

                    parentSetEnabled(true);
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        };
        t.start();
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
        
        import2.setEnabled(dynamicOD.getFile() != null && staticOD.getFile() != null 
                        && demand.getFile() != null && demandProfile != null && demand.getFile() != null);
    }
    

    
    public void setEnabled(boolean e)
    {
        importFromProject.setEnabled(e);
        staticOD.setEnabled(e);
        dynamicOD.setEnabled(e);
        demandProfile.setEnabled(e);
        demand.setEnabled(e);
        import1.setEnabled(e && importFromProject.getFile() != null);
        import2.setEnabled(e && dynamicOD.getFile() != null && staticOD.getFile() != null 
                        && demand.getFile() != null && demandProfile != null);
        boolean sqlCheck = SQLLogin.hasSQL();
        sqlImport.setEnabled(e && sqlCheck);
        sqlExport.setEnabled(e && sqlCheck);
        super.setEnabled(e);
    }
    
    public void setProject(DTAProject project)
    {
        this.project = project;
        
        if(project == null)
        {
            setEnabled(false);
        }
        else
        {
            setEnabled(true);
        }
    }
}
