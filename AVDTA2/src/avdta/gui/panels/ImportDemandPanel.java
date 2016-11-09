/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.demand.DemandImportFromVISTA;
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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author micha
 */
public class ImportDemandPanel extends GUIPanel
{
    
    private JFileField importFromProject;
    private JFileField staticOD, dynamicOD, demandProfile, demand;
    private JButton import1, import2;
    private JButton sqlImport, sqlExport;
    private DTAProject project;
    
    public ImportDemandPanel(DemandPanel parent)
    {
        super(parent);
        
        import1 = new JButton("Import");
        import1.setEnabled(false);
        import2 = new JButton("Import");
        import2.setEnabled(false);
        
        sqlImport = new JButton("Import");
        sqlExport = new JButton("Export");
        
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
        
        
        
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setBorder(BorderFactory.createTitledBorder("Import demand from project"));
        
        constrain(p1, new JLabel("Project: "), 0, 1, 1, 1);
        constrain(p1, importFromProject, 1, 1, 1, 1);
        constrain(p1, import1, 2, 1, 1, 1);
        
        constrain(this, p1, 0, 1, 1, 1);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Import demand from VISTA"));
        
        constrain(p2, new JLabel("static OD: "), 0, 1, 1, 1);
        constrain(p2, staticOD, 1, 1, 1, 1);
        constrain(p2, new JLabel("dynamic OD: "), 0, 2, 1, 1);
        constrain(p2, dynamicOD, 1, 2, 1, 1);
        constrain(p2, new JLabel("demand profile: "), 0, 3, 1, 1);
        constrain(p2, demandProfile, 1, 3, 1, 1);
        constrain(p2, new JLabel("demand: "), 0, 4, 1, 1);
        constrain(p2, demand, 1, 4, 1, 1);
        constrain(p2, import2, 2, 1, 1, 4);
        
        p1.setPreferredSize(new Dimension((int)p2.getPreferredSize().getWidth(), (int)p1.getPreferredSize().getHeight()));
        
        constrain(this, p2, 0, 2, 1, 1);
        
        JPanel p3 = new JPanel();
        p3.setLayout(new GridBagLayout());
        p3.setBorder(BorderFactory.createTitledBorder("SQL"));
        constrain(p3, sqlExport, 0, 0, 1, 1);
        constrain(p3, sqlImport, 1, 0, 1, 1);
        
        p3.setPreferredSize(new Dimension((int)p2.getPreferredSize().getWidth(), (int)p3.getPreferredSize().getHeight()));
        
        constrain(this, p3, 0, 3, 1, 1);
        
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
                    DemandImportFromVISTA read = new DemandImportFromVISTA(project, staticOD.getFile(), dynamicOD.getFile(), 
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
