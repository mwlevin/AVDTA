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
 * @author Michael
 */
public class SAVProject extends DemandProject
{
    /**
     * Constructs an empty project
     */
    public SAVProject()
    {
        
    }
    
    /**
     * Constructs the project from the specified directory
     * @param directory the project directory
     * @throws IOException if a file is not found
     */
    public SAVProject(File directory) throws IOException
    {
        super(directory);
    }
    
    /**
     * Returns the project type
     * @return SAV
     */
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
        
        PrintStream fileout = new PrintStream(getProjectDirectory()+"/sav.dat");
        fileout.close();
    }
    
    
    
    public File getFleetFile()
    {
        return new File(getProjectDirectory()+"/demand/fleet.txt");
    }
    
    
}
