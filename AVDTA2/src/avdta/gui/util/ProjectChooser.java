/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.util;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import avdta.project.Project;

/**
 * This class extends {@link JFileChooser} to specifically select {@link Project} folders.
 * {@link Project} folders are denoted with a special icon (see {@link ProjectFileView}) even though they essentially behave as normal Windows folders.
 * @author Michael
 */
public class ProjectChooser extends JFileChooser
{
    private ProjectFileView view;
    
    private boolean acceptAll, acceptNone;
    
    /**
     * Constructs this {@link ProjectChooser} with the specified directory.
     * @param dir the directory
     */
    public ProjectChooser(File dir)
    {
        super(dir);
        view = new ProjectFileView(null);
        setFileView(view);
        setAcceptNone(true);
    }
    
    /**
     * Constructs this {@link ProjectChooser} with the specified directory and type of project.
     * This calls {@link ProjectFileView#ProjectFileView(java.lang.String)}.
     * @param dir the directory
     * @param type the type
     */
    public ProjectChooser(File dir, String type)
    {
        super(dir);
        view = new ProjectFileView(type);
        setFileView(view);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }
    
    /**
     * Calls {@link JFileChooser#showDialog(java.awt.Component, java.lang.String)} instructing the user to open a project.
     * @param component the parent {@link Component}
     * @return {@link JFileChooser#showDialog(java.awt.Component, java.lang.String)}
     */
    public int showDialog(Component component)
    {
        return showDialog(component, "Open project");
    }
    
    /**
     * Checks whether to accept the file as an openable {@link Project}.
     * This calls {@link ProjectFileView#isProject(java.io.File)} and checks the output code.
     * If {@link ProjectChooser#setAcceptNone(boolean)} is set to true, this accepts the file if the file is not a {@link Project}.
     * If {@link ProjectChooser#setAcceptAll(boolean)} is set to true, this accepts any type of {@link Project}.
     * Otherwise, this accepts {@link Project}s of the type specified in {@link ProjectChooser#ProjectChooser(java.io.File, java.lang.String)}.
     * @param file the file to be checked
     * @return whether the file can be opened
     */
    public boolean accept(File file)
    {
        if(view == null)
        {
            return false;
        }
        int code = view.isProject(file);
        //return true;
        //int code = view.isProject(file);
        
        if(acceptNone)
        {
            return code == 0;
        }
        else
        {
            return code == 0 || code == 1 || (acceptAll && code == 2);
        }
    }
    
    /**
     * Updates whether to accept all types of {@link Project}s.
     * If true, {@link ProjectChooser#setAcceptNone(boolean)} is set to false.
     * @param a whether to accept all types of {@link Project}s
     */
    public void setAcceptAll(boolean a)
    {
        acceptAll = a;
        
        if(acceptAll)
        {
            acceptNone = false;
        
            setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
    }
    
    /**
     * Updates whether to accept no {@link Project}s at all.
     * If true, {@link ProjectChooser#setAcceptAll(boolean)} is set to false.
     * @param a whether to accept no {@link Project}s
     */
    public void setAcceptNone(boolean a)
    {
        acceptAll = !a;
        acceptNone = a;
        
        if(acceptNone)
        {
            setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        else
        {
            setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
    }
}
