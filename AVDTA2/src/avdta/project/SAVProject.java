/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;


import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.sav.ReadSAVNetwork;
import avdta.sav.SAVSimulator;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This represents an SAV project. 
 * This adds a fleet file and identifiers associating the project with SAVs.
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
     * Returns the type indicator {@link String} used to create the indicator file to determine the type of this project.
     * @return "dta"
     */
    public String getTypeIndicator()
    {
        return "sav";
    }
    
    /**
     * Returns the project type
     * @return SAV
     */
    public String getType()
    {
        return "SAV";
    }
    
    /**
     * This adds options related to SAVs: {@code relocate}, {@code ride-sharing}, and {@code cost-factor}.
     * Also calls {@link DemandProject#fillOptions()}.
     */
    public void fillOptions()
    {
        super.fillOptions();
        setOption("relocate", "false");
        setOption("ride-sharing", "false");
        setOption("cost-factor", "1.4");
    }
    
    /**
     * Loads the {@link Simulator} associated with this {@link Project} using {@link ReadNetwork}.
     * @throws IOException if the file is not found
     */
    public void loadSimulator() throws IOException
    {
        ReadSAVNetwork read = new ReadSAVNetwork();       
        
        try
        {
            SAVSimulator output = read.readNetwork(this);
        
            setSimulator(output);
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
            setSimulator(createEmptySimulator());
        }

    }
    
    /**
     * Returns the {@link SAVSimulator} associated with this project. 
     * If null, call {@link SAVProject#loadSimulator()}
     * @return the {@link SAVSimulator} associated with this project
     */
    public SAVSimulator getSimulator()
    {
        return (SAVSimulator)super.getSimulator();
    }
    
    /**
     * Creates an empty {@link Simulator}
     * @return the empty {@link Simulator}
     */
    public Simulator createEmptySimulator()
    {
        return new SAVSimulator(this);
    }
    
    /**
     * Create the project folders in the specified directory.
     * This adds the {@code sav.dat} indicator file.
     * @param dir the directory
     * @throws IOException if a file cannot be accessed 
     */
    public void createProjectFolders(File dir) throws IOException
    {
        super.createProjectFolders(dir);
        
        
        String dirStr = dir.getCanonicalPath();
        
        File file = new File(dirStr+"/fleet");
        file.mkdirs();
    }
    
    
    /**
     * Returns the SAV fleet file
     * @return the SAV fleet file
     */
    public File getFleetFile()
    {
        return new File(getProjectDirectory()+"/fleet/fleet.txt");
    }
    
    
}
