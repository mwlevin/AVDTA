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
import avdta.project.Project;

/**
 * This class provides a modified {@link FileView} that displays {@link Project} folders with a special icon.
 * {@link Project} folders are identified via the {@code project.txt} file.
 * 
 * In addition, this class can select projects of specific types.
 * It looks for a {@code type.dat} indicator file (e.g. {@code dta.dat}) to determine the type of {@link Project}.
 * @author Michael
 */
public class ProjectFileView extends FileView
{
    public static final Icon icon = new ImageIcon(GUI.getIcon().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
    
    private String type;
    
    /**
     * Constructs this {@link ProjectFileView} with no type indicator.
     */
    public ProjectFileView()
    {
        this(null);
    }
    
    /**
     * Constructs this {@link ProjectFileView} for the specified type of projects.
     * @param type the type indicator (see {@link Project#getTypeIndicator()}).
     */
    public ProjectFileView(String type)
    {
        if(type != null)
        {
            this.type = type.toLowerCase();
        }
        else
        {
            this.type = null;
        }
    }
    
    /**
     * Returns {@link ProjectFileView#icon} if the file is a {@link Project} folder, or {@link FileView#getIcon(java.io.File)} otherwise.
     * @param file the file to be checked
     * @return the associated icon
     */
    public Icon getIcon(File file)
    {
        if(isProject(file) > 0)
        {
            return icon;
        }
        
        return super.getIcon(file);
    }
    
    /**
     * Returns a code indicating whether the specified file is a {@link Project}, and if it matches the specific type of {@link Project}.
     * @param file the file to be checked
     * @return return 0 - false, 1 - true, 2 - other type of project
     */
    public int isProject(File file)
    {
        try
        {
            File properties = new File(file.getCanonicalPath()+"/project.txt");
            
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
    
    /**
     * Returns whether the file can be traversed.
     * @param file the file to be checked
     * @return if the file is not a {@link Project} folder.
     */
    public Boolean isTraversable(File file)
    {
        return isProject(file) == 0;
    }
}
