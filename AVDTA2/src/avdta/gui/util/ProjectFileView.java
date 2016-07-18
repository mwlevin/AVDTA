/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.util;

import avdta.gui.GUI;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;

/**
 *
 * @author micha
 */
public class ProjectFileView extends FileView
{
    private static final Icon icon = new ImageIcon(GUI.getIcon().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
    
    private String type;
    
    public ProjectFileView()
    {
        this(null);
    }
    public ProjectFileView(String type)
    {
        this.type = type;
    }
    
    public Icon getIcon(File file)
    {
        if(isProject(file) > 0)
        {
            return icon;
        }
        
        return super.getIcon(file);
    }
    
    // return 0 - false, 1 - true, 2 - other type of project
    public int isProject(File file)
    {
        try
        {
            File properties = new File(file.getCanonicalPath()+"/project.dat");
            
            if(properties.exists())
            {
                if(type == null)
                {
                    return 2;
                }
                else
                {
                    File typefile = new File(file.getCanonicalPath()+"/"+type+".dat");

                    if(typefile.exists())
                    {
                        return 1;
                    }
                    else
                    {
                        return 2;
                    }
                }
            }
        }
        catch(IOException ex){}
        return 0;
    }
    
    public Boolean isTraversable(File file)
    {
        return isProject(file) == 0;
    }
}