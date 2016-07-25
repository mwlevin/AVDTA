/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author micha
 */
public class SAVProject extends Project
{
    public SAVProject()
    {
        
    }
    
    public SAVProject(File directory) throws IOException
    {
        super(directory);
    }
    
    public String getType()
    {
        return "SAV";
    }
    
    public void fillOptions()
    {
        super.fillOptions();
        setOption("relocate", "false");
        setOption("ride-sharing", "false");
        setOption("cost-factor", "1.4");
    }
    
    public void createProjectFolders(File dir) throws IOException
    {
        super.createProjectFolders(dir);
        
        String dirStr = dir.getCanonicalPath();
        
        File file = new File(dirStr+"/demand");
        file.mkdirs();

        PrintStream fileout = new PrintStream(getProjectDirectory()+"/sav.dat");
        fileout.close();
    }
    
    public File getTripsFile()
    {
        return new File(getProjectDirectory()+"/demand/demand.txt");
    }
    
    public File getFleetFile()
    {
        return new File(getProjectDirectory()+"/demand/fleet.txt");
    }
    
    public File getStaticODFile()
    {
        return new File(getProjectDirectory()+"/demand/static_od.txt");
    }
    
    public File getDynamicODFile()
    {
        return new File(getProjectDirectory()+"/demand/dynamic_od.txt");
    }
    
    public File getDemandProfileFile()
    {
        return new File(getProjectDirectory()+"/demand/demand_profile.txt");
    }
}
