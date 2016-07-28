/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.gui.util.Version;
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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

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
        //System.setErr(new PrintStream(new FileOutputStream(new File("error_log.txt")), true));
        
        new DTAGUI();
        
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
        JOptionPane.showMessageDialog(frame, ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    protected JMenuItem cloneMI, closeMI, createDatabase;
    
    
    public GUI()
    {
        setTitle("AVDTA");
        setIconImage(GUI.getIcon());
        
        frame = this;
        
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
        me.add(mi);
        
        mi = new JMenuItem("Open project");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                openProject();
            }
        });
        me.add(mi);
        
        mi = new JMenuItem("Clone opened project");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cloneProject();
            }
        });
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
        me.add(mi);
        
        closeMI = mi;
        closeMI.setEnabled(false);
        
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
        
        me = new JMenu("About");
        mi = new JMenuItem("Version");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(frame, "AVDTA v"+Version.getVersion()+"\nCopyright © 2014 by "+Version.getAuthor(), 
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        me.add(mi);
        
        mi = new JMenuItem("Help");
        
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(frame, "Documentation is located in \"/documentation/DTA.pdf\"\n\nDeveloper email: michaellevin@utexas.edu", 
                        "Help", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        me.add(mi);
        
        menu.add(me);
        
        this.setJMenuBar(menu);
    }
    
    public abstract void newProject();
    public abstract void closeProject();
    public abstract void openProject();
    public abstract void cloneProject();
    public abstract void reset();
    
    public abstract void createDatabase();
    
    public void openProject(Project p) throws IOException
    {
        createDatabase.setEnabled(SQLLogin.hasSQL());
    }
    
    public void setupSQL()
    {
        
    }
}
