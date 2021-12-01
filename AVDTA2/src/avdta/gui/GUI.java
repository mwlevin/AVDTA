/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.gui.editor.Editor;
import avdta.Version;
import avdta.gui.util.ProjectChooser;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import avdta.network.node.PhasedTBR;
import avdta.project.DTAProject;
import avdta.project.FourStepProject;
import avdta.project.Project;
import avdta.project.SQLLogin;
import avdta.project.TransitProject;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

/**
 *
 * @author micha
 */
public abstract class GUI extends JFrame
{
    private static String directory = null;
    
    
    private static JFrame frame;
    private static Image icon;
    
    
    
    
    public static void main(String[] args) throws IOException
    {
        System.setErr(new PrintStream(new FileOutputStream(new File("error_log.txt")), true));
        
        try
        {
            if(args.length > 0 && args[0].equalsIgnoreCase("editor"))
            {
                new Editor();
            }
            else if(args.length > 0 && args[0].equalsIgnoreCase("dta"))
            {
                new DTAGUI();
            }
            else if(args.length > 0 && args[0].equalsIgnoreCase("fourstep"))
            {
                new FourStepGUI();
            }
            else if(args.length > 0 && args[0].equalsIgnoreCase("sav"))
            {
                new SAVGUI();
            }
            else if(args.length > 0 && args[0].equalsIgnoreCase("all"))
            {
                new Start();
            }
            else
            {
                new Start();
            }
        }
        catch(Exception ex)
        {
            GUI.handleException(ex);
        }
        
        
    }
    

    public static Image getIcon()
    {
        if(icon == null)
        {
            try
            {
                icon = ImageIO.read(new File("icon.png"));
            }
            catch(IOException ex){}
        }
        
        return icon;
    }
    
    public static String getDefaultDirectory()
    {
        if(directory == null)
        {
            loadDirectory();
        }
        return directory;
    }
    
    private static void loadDirectory()
    {
        try
        {
            Scanner filein = new Scanner(new File("directory.txt"));
            directory = filein.nextLine().trim();
            filein.close();
        }
        catch(IOException ex)
        {
            directory = "";
        }
    }
    
    public static void handleException(Exception ex)
    {
        System.out.println("Error occurred: "+ex);
        ex.printStackTrace(System.out);
        ex.printStackTrace(System.err);
        JOptionPane.showMessageDialog(frame, ex.toString()+" "+ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private JMenuItem cloneMI, closeMI, createDatabase, editor, changeSeed, sanityCheck, connectivityTest;
    protected Project project;
    
    private Editor openEditor;
    
    
    public GUI()
    {
        setTitle(getTitleName());
        setIconImage(getIcon());
        
        frame = this;

        JMenuBar menu = createMenuBar();
        menu.add(createHelpMenu(this));
        
        this.setJMenuBar(menu);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
    }
    
    protected JMenuBar createMenuBar()
    {
        JMenuBar menu = new JMenuBar();
        JMenu me;
        JMenuItem mi;
        
        me = new JMenu("File");
        mi = new JMenuItem("New project");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                newProject();
            }
        });
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        me.add(mi);
        
        mi = new JMenuItem("Open project");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                openProject();
            }
        });
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        me.add(mi);
        
        mi = new JMenuItem("Clone opened project");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cloneProject();
            }
        });
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        me.add(mi);
        

        cloneMI = mi;
        cloneMI.setEnabled(false);
        
        mi = new JMenuItem("Close project");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                closeProject();
            }
        });
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        me.add(mi);
        
        closeMI = mi;
        closeMI.setEnabled(false);
        
        me.addSeparator();
        
        mi = new JMenuItem("Exit");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
        me.add(mi);
        
        menu.add(me);
        
        me = new JMenu("SQL");
        mi = new JMenuItem("Setup SQL");
        createDatabase = new JMenuItem("Create database");
        createDatabase.setEnabled(false);
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setupSQL();
                
                createDatabase.setEnabled(project != null && SQLLogin.hasSQL());
            }
        });
        
        me.add(mi);
        
        
        
        createDatabase.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createDatabase();
            }
        });
        
        me.add(createDatabase);
        
        menu.add(me);
        
        me = new JMenu("Network");
        
        editor = new JMenuItem("Editor");
        
        editor.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                openEditor = new Editor(project, false);
            }
        });
        editor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
        me.add(editor);
        editor.setEnabled(false);
        
        me.addSeparator();
        
        connectivityTest = new JMenuItem("Connectivity test");
        connectivityTest.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    boolean output = project.getSimulator().connectivityTest();

                    if(output)
                    {
                        JOptionPane.showMessageDialog(frame, "All vehicles have a connected path.", "Connectivity test", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else
                    {
                        JOptionPane.showMessageDialog(frame, "Connectivity errors detected.\n"+project.getResultsFolder()+"/unconnected.txt", 
                                "Connectivity test", JOptionPane.WARNING_MESSAGE);
                    }
                }
                catch(Exception ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        connectivityTest.setEnabled(false);
        me.add(connectivityTest);
        
        sanityCheck = new JMenuItem("Sanity check");
        sanityCheck.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int errors = project.sanityCheck();
                
                if(errors == 0)
                {
                    JOptionPane.showMessageDialog(frame, "No errors detected.", "Sanity check complete", JOptionPane.INFORMATION_MESSAGE);
                }
                else
                {
                    JOptionPane.showMessageDialog(frame, "Errors detected.\nView log in "+project.getSanityCheckFile().toString(), "Sanity check complete", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        sanityCheck.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        me.add(sanityCheck);
        sanityCheck.setEnabled(false);
        
        changeSeed = new JMenuItem("Reset random seed");
        changeSeed.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    project.changeRandSeed();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        me.add(changeSeed);
        changeSeed.setEnabled(false);
        menu.add(me);
        
        
        
        
        
        
        return menu;
    }
    
    public static JMenu createHelpMenu(final JFrame frame)
    {
        JMenu me = new JMenu("Help");

        JMenuItem mi = new JMenuItem("Help");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(frame, "Documentation is located in \"/documentation/DTA.pdf\"\n\nDeveloper email: michaellevin@utexas.edu", 
                        "Help", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        me.add(mi);
        
        me.addSeparator();
        
        mi = new JMenuItem("About");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(frame, "AVDTA v"+Version.getVersion()+"\nCopyright © 2014 by "+Version.getAuthor(), 
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        me.add(mi);
        
        return me;
    }
    
    public abstract Project openProject(File file) throws IOException;
    public abstract String getProjectType();
    public abstract Project createEmptyProject();
    
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
                    Project rhs = createEmptyProject();
                    rhs.createProject(name, new File(dir.getCanonicalPath()+"/"+name));
                    
                    if(project instanceof FourStepProject)
                    {
                        rhs.cloneFromProject((FourStepProject)project);
                    }
                    else if(project instanceof DTAProject)
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
    
    public void newProject(File dir, String name)
    {
        try
        {
            Project project = createEmptyProject();
            project.createProject(name, new File(dir.getCanonicalPath()+"/"+name));
            openProject(project);
        }
        catch(IOException ex)
        {
            handleException(ex);
        }
    }
    
    public void openProject()
    {
        JFileChooser chooser = new ProjectChooser(new File(GUI.getDefaultDirectory()), getProjectType());
        
        int returnVal = chooser.showDialog(this, "Open project");
        
        if(returnVal == chooser.APPROVE_OPTION)
        {
            try
            {
                Project project = openProject(chooser.getSelectedFile());
                
                openProject(project);
            }
            catch(IOException ex)
            {
                JOptionPane.showMessageDialog(this, "The selected folder is not a "+getProjectType()+" network", "Invalid network", 
                        JOptionPane.ERROR_MESSAGE);
            }
            catch(Exception ex)
            {
                GUI.handleException(ex);
            }
        }
    }
    
    public void openProject(Project p) throws IOException
    {
        if(openEditor != null)
        {
            openEditor.setVisible(false);
            openEditor = null;
        }
        this.project = p;
        createDatabase.setEnabled(project != null && SQLLogin.hasSQL());
        
        cloneMI.setEnabled(project != null);
        closeMI.setEnabled(project != null);
        editor.setEnabled(project != null);
        changeSeed.setEnabled(project != null);
        sanityCheck.setEnabled(project != null);
        connectivityTest.setEnabled(project != null);
    }
    
    public void setupSQL()
    {
        
    }
    
    public static String getTitleName()
    {
        return "AVDTA";
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
                newProject(dir, name);
                
            }
        }
    }
    
    public void closeProject()
    {
        try
        {
            Project project = null;
            openProject(project);
        }
        catch(IOException ex)
        {
            GUI.handleException(ex);
        }
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

    public static class main {

        public main() {
        }
    }
}
