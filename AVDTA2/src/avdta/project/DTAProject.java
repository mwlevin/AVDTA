/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import avdta.dta.Assignment;
import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.util.FileTransfer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This project represents a DTA network, which extends {@link TransitProject} and adds demand data.  
 * @author Michael
 */
public class DTAProject extends DemandProject
{
    /**
     * Constructs an empty project
     */
    public DTAProject()
    {
        
    }
    
    /**
     * Constructs the project from the specified directory
     * @param directory the project directory
     * @throws IOException if a file is not found
     */
    public DTAProject(File directory) throws IOException
    {
        super(directory);
    }

    
    
    
    /**
     * Returns the {@link DTASimulator} associated with this project. 
     * If null, call {@link DTAProject#loadSimulator()}
     * @return the {@link DTASimulator} associated with this project
     */
    public DTASimulator getSimulator()
    {
        return (DTASimulator)super.getSimulator();
    }
    
    /**
     * Loads the {@link DTASimulator} using {@link ReadDTANetwork}
     * @throws IOException if a file is not found
     */
    public void loadSimulator() throws IOException
    {
        try
        {
            ReadDTANetwork read = new ReadDTANetwork();  
            
            DTASimulator output = read.readNetwork(this);
        
            setSimulator(output);
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
            setSimulator(createEmptySimulator());
        }

    }
    
    /**
     * Writes empty demand files. Also calls {@link TransitProject#writeEmptyFiles()}
     * @throws IOException if a file cannot be created
     */
    public void writeEmptyFiles() throws IOException
    {
        super.writeEmptyFiles();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(getDemandFile()), true);
        fileout.println(ReadDTANetwork.getDemandFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getDynamicODFile()), true);
        fileout.println(ReadDTANetwork.getDynamicODFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getStaticODFile()), true);
        fileout.println(ReadDTANetwork.getStaticODFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getDemandProfileFile()), true);
        fileout.println(ReadDTANetwork.getDemandProfileFileHeader());
        fileout.close();
    }
    
    /**
     * Creates project subfolders inside the specified directory
     * @param dir the project directory
     * @throws IOException if a file cannot be created
     */
    public void createProjectFolders(File dir) throws IOException
    {
        super.createProjectFolders(dir);
        
        String dirStr = dir.getCanonicalPath();
        
        File file = new File(dirStr+"/assignments");
        file.mkdirs();
        
        PrintStream fileout = new PrintStream(getProjectDirectory()+"/dta.dat");
        fileout.close();
    }
    
    /**
     * Creates an empty {@link DTASimulator}
     * @return an empty {@link DTASimulator}
     */
    public DTASimulator createEmptySimulator()
    {
        return new DTASimulator(this);
    }
    
    
    
    /**
     * Returns the assignments folder
     * @return {@link Project#getProjectDirectory()}/assignments/
     */
    public String getAssignmentsFolder()
    {
        return getProjectDirectory()+"/assignments";
    }
    
    /**
     * Clears all saved assignments
     */
    public void deleteAssignments()
    {
        File root = new File(getAssignmentsFolder());
        
        
        for(File dir : root.listFiles())
        {
            for(File f : dir.listFiles())
            {
                f.delete();
            }
            dir.delete();
        }
    }
    
    
    /**
     * Returns the project type
     * @return DTA
     */
    public String getType()
    {
        return "DTA";
    }
    
    
    
    
}
