/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author micha
 */
public class DTAProject extends Project
{
    public DTAProject()
    {
        
    }
    
    public DTAProject(String name) throws IOException
    {
        super(name);
    }
    
    
    public void createProjectFolders(File dir) throws IOException
    {
        super.createProjectFolders(dir);
        
        String dirStr = dir.getCanonicalPath();
        
        File file = new File(dirStr+"/assignments");
        file.mkdirs();
    }
    

    public File getStaticODFile()
    {
        return new File(getProjectDirectory()+"/network/static_od.dat");
    }
    
    public File getDynamicODFile()
    {
        return new File(getProjectDirectory()+"/network/dynamic_od.dat");
    }
    
    public File getDemandProfileFile()
    {
        return new File(getProjectDirectory()+"/network/demand_profile.dat");
    }
    
    public File getDemandFile()
    {
        return new File(getProjectDirectory()+"/network/demand.dat");
    }
    
    public String getResultsFolder()
    {
        return getProjectDirectory()+"/results/";
    }
    
    public File getOptionsFile()
    {
        return new File(getProjectDirectory()+"/network/options.dat");
    }
    
    public String getType()
    {
        return "DTA";
    }
    
}
