/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import javax.swing.JPanel;
import static avdta.gui.GraphicUtils.*;
import avdta.network.ImportFromVISTA;
import avdta.project.DTAProject;
import avdta.project.Project;
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
public class ImportNetworkPane extends JPanel
{
    private Project project;
    private NetworkPane parent;
    
    private JFileField importFromProject;
    private JFileField linkdetails, nodes, elevation, phases;
    
    private JButton import1;
    private JButton import2;
    
    public ImportNetworkPane(NetworkPane parent)
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
        linkdetails = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
                import2.setEnabled(nodes.getFile() != null && linkdetails.getFile() != null && phases.getFile() != null);
            }
        };
        nodes = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
                import2.setEnabled(nodes.getFile() != null && linkdetails.getFile() != null && phases.getFile() != null);
            }
        };
        elevation = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
                import2.setEnabled(nodes.getFile() != null && linkdetails.getFile() != null && phases.getFile() != null);
            }
        };
        phases = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
                import2.setEnabled(nodes.getFile() != null && linkdetails.getFile() != null && phases.getFile() != null);
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
        constrain(p, new JLabel("Nodes: "), 0, 1, 1, 1);
        constrain(p, nodes, 1, 1, 1, 1);
        constrain(p, new JLabel("Link details: "), 0, 2, 1, 1);
        constrain(p, linkdetails, 1, 2, 1, 1);
        constrain(p, new JLabel("Phases: "), 0, 3, 1, 1);
        constrain(p, phases, 1, 3, 1, 1);
        constrain(p, new JLabel("Elevation: "), 0, 4, 1, 1);
        constrain(p, elevation, 1, 4, 1, 1);
        constrain(p, import2, 2, 1, 1, 4);
        
        constrain(this, p, 0, 2, 1, 1);
        
        disable();
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
            if(phases.getFile() == null)
            {
                File file = new File(dir+"/phases.txt");
                if(file.exists())
                {
                    phases.setFile(file);
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
        
    }
    public void setProject(Project project)
    {
        this.project = project;
        
        if(project != null)
        {
            enable();
        }
        else
        {
            disable();
        }
    }
    
    public void importFromVISTA() throws IOException
    {
        parent.disable();
        
        ImportFromVISTA read = new ImportFromVISTA(project, nodes.getFile(), linkdetails.getFile(), 
                elevation.getFile(), phases.getFile());
        
        project.loadSimulator();
        parent.reset();
        
        nodes.setFile(null);
        elevation.setFile(null);
        phases.setFile(null);
        linkdetails.setFile(null);
        
        parent.enable();
        parent.reset();
    }
    
    public void importFromProject() throws IOException
    {
        parent.disable();
        
        Project rhs = new DTAProject(importFromProject.getFile());
        
        project.importNetworkFromProject(rhs);
        project.loadSimulator();
        
        importFromProject.setFile(null);
        parent.reset();
        
        parent.enable();
        parent.reset();
    }
    
    
    public void enable()
    {
        import1.setEnabled(importFromProject.getFile() != null);
        import2.setEnabled(nodes.getFile() != null && linkdetails.getFile() != null && phases.getFile() != null);
        elevation.setEnabled(true);
        linkdetails.setEnabled(true);
        nodes.setEnabled(true);
        phases.setEnabled(true);
        importFromProject.setEnabled(true);
    }
    
    public void disable()
    {
        import1.setEnabled(false);
        import2.setEnabled(false);
        elevation.setEnabled(false);
        linkdetails.setEnabled(false);
        nodes.setEnabled(false);
        phases.setEnabled(false);
        importFromProject.setEnabled(false);
    }
}
