/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author ml26893
 */
public class ProjectChooser extends JFileChooser
{
    private ProjectFileView view;
    
    private boolean acceptAll, acceptNone;
    
    public ProjectChooser(File dir)
    {
        super(dir);
        view = new ProjectFileView("");
        setFileView(view);
        setAcceptNone(true);
    }
    
    public ProjectChooser(File dir, String type)
    {
        super(dir);
        view = new ProjectFileView(type);
        setFileView(view);
        setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    }
    
    
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
            return code == 1 || (acceptAll && code == 2);
        }
    }
    
    public void setAcceptAll(boolean a)
    {
        acceptAll = a;
        
        if(acceptAll)
        {
            acceptNone = false;
        
            setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }
    }
    
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
