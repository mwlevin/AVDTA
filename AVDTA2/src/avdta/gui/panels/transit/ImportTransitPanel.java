/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.transit;

import avdta.gui.panels.transit.TransitPanel;
import avdta.gui.GUI;
import avdta.gui.panels.GUIPanel;
import static avdta.gui.util.GraphicUtils.constrain;
import avdta.gui.util.JFileField;
import avdta.gui.util.ProjectChooser;
import avdta.network.ImportFromVISTA;
import avdta.network.TransitImportFromVISTA;
import avdta.project.DTAProject;
import avdta.project.TransitProject;
import avdta.project.Project;
import avdta.project.SQLLogin;
import avdta.project.TransitProject;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Michael
 */
public class ImportTransitPanel extends GUIPanel
{
    private TransitProject project;
    
    private JFileField importFromProject;
    private JFileField bus, busfrequency, busperiod, busroutelink;
    
    private JButton import1;
    private JButton import2;
    private JButton sqlImport, sqlExport;
    
    public ImportTransitPanel(TransitPanel parent)
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
                setEnabled(false);
                Thread t = new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            project.importTransitFromSQL();
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
                            project.exportTransitToSQL();
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
        bus = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        busperiod = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        busfrequency = new JFileField(10, txtFiles, "data/")
        {
            public void valueChanged(File f)
            {
                checkForOtherFiles(f);
            }
        };
        busroutelink = new JFileField(10, txtFiles, "data/")
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
        
        
        
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setBorder(BorderFactory.createTitledBorder("Import transit from project"));
        constrain(p1, new JLabel("Project: "), 0, 1, 1, 1);
        constrain(p1, importFromProject, 1, 1, 1, 1);
        constrain(p1, import1, 2, 1, 1, 1);
        
        constrain(this, p1, 0, 1, 1, 1);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        
        p2.setBorder(BorderFactory.createTitledBorder("Import transit from VISTA"));
        constrain(p2, new JLabel("bus: "), 0, 1, 1, 1);
        constrain(p2, bus, 1, 1, 1, 1);
        constrain(p2, new JLabel("bus_route_link: "), 0, 2, 1, 1);
        constrain(p2, busroutelink, 1, 2, 1, 1);
        constrain(p2, new JLabel("bus_frequency: "), 0, 3, 1, 1);
        constrain(p2, busfrequency, 1, 3, 1, 1);
        constrain(p2, new JLabel("bus_period: "), 0, 4, 1, 1);
        constrain(p2, busperiod, 1, 4, 1, 1);
        constrain(p2, import2, 2, 1, 1, 4);
        
        constrain(this, p2, 0, 2, 1, 1);
        
        p1.setPreferredSize(new Dimension((int)p2.getPreferredSize().getWidth(), (int)p1.getPreferredSize().getHeight()));
        
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
                    TransitProject rhs = new DTAProject(importFromProject.getFile());

                    project.importTransitFromProject(rhs);
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
    
    public void setProject(TransitProject project)
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
    
    public void setEnabled(boolean e)
    {
        import1.setEnabled(e && importFromProject.getFile() != null);
        import2.setEnabled(e && bus.getFile() != null && busfrequency.getFile() != null && busperiod.getFile() != null && busroutelink.getFile() != null);
        bus.setEnabled(e);
        busfrequency.setEnabled(e);
        busperiod.setEnabled(e);
        busroutelink.setEnabled(e);
        importFromProject.setEnabled(e);
        
        boolean sqlCheck = SQLLogin.hasSQL();
        sqlImport.setEnabled(e && sqlCheck);
        sqlExport.setEnabled(e && sqlCheck);
        
        super.setEnabled(e);

    }
    
    public void importFromVISTA() throws IOException
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    TransitImportFromVISTA read = new TransitImportFromVISTA(project, bus.getFile(), busperiod.getFile(), 
                    busroutelink.getFile(), busfrequency.getFile());

                    project.loadSimulator();
                    parentReset();

                    bus.setFile(null);
                    busperiod.setFile(null);
                    busroutelink.setFile(null);
                    busfrequency.setFile(null);

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
    
    public void reset()
    {
        
    }
    
    public void checkForOtherFiles(File f)
    {
        try
        {
            String dir = f.getCanonicalPath();

            dir = dir.substring(0, dir.lastIndexOf("\\".charAt(0))+1);
            

            if(bus.getFile() == null)
            {
                File file = new File(dir+"/bus.txt");
                if(file.exists())
                {
                    bus.setFile(file);
                }
            }
            if(busfrequency.getFile() == null)
            {
                File file = new File(dir+"/bus_frequency.txt");
                if(file.exists())
                {
                    busfrequency.setFile(file);
                }
            }
            if(busperiod.getFile() == null)
            {
                File file = new File(dir+"/bus_period.txt");
                if(file.exists())
                {
                    busperiod.setFile(file);
                }
            }
            if(busroutelink.getFile() == null)
            {
                File file = new File(dir+"/bus_route_link.txt");
                if(file.exists())
                {
                    busroutelink.setFile(file);
                }
            }

        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
        
        import2.setEnabled(bus.getFile() != null && busperiod.getFile() != null && busroutelink.getFile() != null && busfrequency.getFile() != null);
        
    }
}