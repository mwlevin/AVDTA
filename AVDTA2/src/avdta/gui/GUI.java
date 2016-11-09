/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.gui.editor.Editor;
import avdta.Version;
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
import avdta.project.Project;
import avdta.project.SQLLogin;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
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
            else
            {
                new DTAGUI();
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
        ex.printStackTrace(System.err);
        JOptionPane.showMessageDialog(frame, ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private JMenuItem cloneMI, closeMI, createDatabase, editor, changeSeed, sanityCheck;
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
                
                createDatabase.setEnabled(SQLLogin.hasSQL());
                reset();
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
    
    public abstract void newProject();
    public abstract void closeProject();
    public abstract void openProject();
    public abstract void cloneProject();
    public abstract void reset();
    
    public abstract void createDatabase();
    
    public void openProject(Project p) throws IOException
    {
        if(openEditor != null)
        {
            openEditor.setVisible(false);
            openEditor = null;
        }
        this.project = p;
        createDatabase.setEnabled(SQLLogin.hasSQL());
        
        cloneMI.setEnabled(project != null);
        closeMI.setEnabled(project != null);
        editor.setEnabled(project != null);
        changeSeed.setEnabled(project != null);
        sanityCheck.setEnabled(project != null);
    }
    
    public void setupSQL()
    {
        
    }
    
    public static String getTitleName()
    {
        return "AVDTA";
    }
}
