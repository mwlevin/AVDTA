/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.gui.GUI;
import avdta.gui.util.JFileField;
import avdta.gui.util.ProjectChooser;
import avdta.gui.panels.NetworkPane;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.network.ImportFromVISTA;
import avdta.project.DTAProject;
import avdta.project.Project;
import avdta.project.SQLLogin;
import java.awt.GridBagLayout;
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
public class ImportNetworkPane extends GUIPanel
{
    private Project project;
    
    private JFileField importFromProject;
    private JFileField linkdetails, nodes, elevation, phases, signals, linkpoints;
    
    private JButton import1;
    private JButton import2;
    private JButton sqlImport, sqlExport;
    
    
    public ImportNetworkPane(NetworkPane parent)
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
                setEnabled(false);
                Thread t = new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            project.importNetworkFromSQL();
                            project.loadSimulator();
                            parentReset();
                        }
                        catch(Exception ex)
                        {
                            GUI.handleException(ex);
                        }
                    }
                };
                t.start();
            }
        });
        
        sqlExport.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setEnabled(false);
                Thread t =  new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            project.exportNetworkToSQL();
                            setEnabled(true);
                        }
                        catch(Exception ex)
                        {
                            GUI.handleException(ex);
                        }
                    }
                };
                t.start();
                
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
                ProjectChooser chooser = new ProjectChooser(new File(GUI.getDefaultDirectory()), "DTA");
                chooser.setAcceptAll(true);


                int returnVal = chooser.showDialog(this);

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
        linkdetails = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        nodes = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        linkpoints = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        signals = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        elevation = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        phases = new JFileField(10, txtFiles, "data/")
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
        constrain(p, new JLabel("Import network from project"), 0, 0, 3, 1);
        constrain(p, new JLabel("Project: "), 0, 1, 1, 1);
        constrain(p, importFromProject, 1, 1, 1, 1);
        constrain(p, import1, 2, 1, 1, 1);
        
        constrain(this, p, 0, 1, 1, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JLabel("Import from VISTA"), 0, 0, 4, 1);
        constrain(p, new JLabel("nodes: "), 0, 1, 1, 1);
        constrain(p, nodes, 1, 1, 1, 1);
        constrain(p, new JLabel("links: "), 0, 2, 1, 1);
        constrain(p, linkpoints, 1, 2, 1, 1);
        constrain(p, new JLabel("linkdetails: "), 0, 3, 1, 1);
        constrain(p, linkdetails, 1, 3, 1, 1);
        constrain(p, new JLabel("phases: "), 0, 4, 1, 1);
        constrain(p, phases, 1, 4, 1, 1);
        constrain(p, new JLabel("signals: "), 0, 5, 1, 1);
        constrain(p, signals, 1, 5, 1, 1);
        constrain(p, new JLabel("elevation: "), 0, 6, 1, 1);
        constrain(p, elevation, 1, 6, 1, 1);
        constrain(p, import2, 2, 1, 1, 7);
        
        constrain(this, p, 0, 2, 1, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, sqlExport, 0, 0, 1, 1);
        constrain(p, sqlImport, 0, 1, 1, 1);
        
        constrain(this, p, 0, 3, 1, 1);
        
        setEnabled(false);
    }

    
    public void checkForOtherFiles(File f)
    {
        try
        {
            String dir = f.getCanonicalPath();

            dir = dir.substring(0, dir.lastIndexOf("\\".charAt(0))+1);
            

            if(nodes.getFile() == null)
            {
                File file = new File(dir+"/nodes.txt");
                if(file.exists())
                {
                    nodes.setFile(file);
                }
            }
            if(linkdetails.getFile() == null)
            {
                File file = new File(dir+"/linkdetails.txt");
                if(file.exists())
                {
                    linkdetails.setFile(file);
                }
            }
            if(signals.getFile() == null)
            {
                File file = new File(dir+"/signals.txt");
                if(file.exists())
                {
                    signals.setFile(file);
                }
            }
            if(phases.getFile() == null)
            {
                File file = new File(dir+"/phases.txt");
                if(file.exists())
                {
                    phases.setFile(file);
                }
            }
            if(linkpoints.getFile() == null)
            {
                File file = new File(dir+"/links.txt");
                if(file.exists())
                {
                    linkpoints.setFile(file);
                }
            }
            if(elevation.getFile() == null)
            {
                File file = new File(dir+"/elevation.txt");
                if(file.exists())
                {
                    elevation.setFile(file);
                }
            }
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        
        import2.setEnabled(nodes.getFile() != null && linkdetails.getFile() != null && phases.getFile() != null && signals.getFile() != null);
        
    }
    
    public void setProject(Project project)
    {
        this.project = project;
        
        if(project != null)
        {
            setEnabled(true);
        }
        else
        {
            setEnabled(false);
        }
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
                    ImportFromVISTA read = new ImportFromVISTA(project, nodes.getFile(), linkdetails.getFile(), 
                    elevation.getFile(), phases.getFile(), signals.getFile(), linkpoints.getFile());

                    project.loadSimulator();
                    parentReset();

                    nodes.setFile(null);
                    elevation.setFile(null);
                    phases.setFile(null);
                    linkdetails.setFile(null);
                    signals.setFile(null);

                    parentSetEnabled(true);
                    parentReset();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        };
        t.start();
        
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
                    Project rhs = new DTAProject(importFromProject.getFile());

                    project.importNetworkFromProject(rhs);
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
    
    
    public void setEnabled(boolean e)
    {
        import1.setEnabled(e && importFromProject.getFile() != null);
        import2.setEnabled(e && nodes.getFile() != null && linkdetails.getFile() != null && phases.getFile() != null && signals.getFile() != null);
        elevation.setEnabled(e);
        linkdetails.setEnabled(e);
        nodes.setEnabled(e);
        phases.setEnabled(e);
        signals.setEnabled(e);
        linkpoints.setEnabled(e);
        importFromProject.setEnabled(e);
        
        boolean sqlCheck = SQLLogin.hasSQL();
        sqlImport.setEnabled(e && sqlCheck);
        sqlExport.setEnabled(e && sqlCheck);
        
        super.setEnabled(e);
    }
    
    public void reset()
    {
        
    }

}
