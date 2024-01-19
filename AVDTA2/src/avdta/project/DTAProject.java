/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import avdta.RunSimulation;
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
     * Constructs the project from the specified directory.
     * This reads the project and constructs the associated {@link DTASimulator}.
     * @param directory the project directory
     * @throws IOException if a file is not found
     */
    public DTAProject(File directory) throws IOException
    {
        super(directory);
    }

    /**
     * Returns the type indicator {@link String} used to create the indicator file to determine the type of this project.
     * @return "dta"
     */
    public String getTypeIndicator()
    {
        return "dta";
    }
    
    // return the last created
    public Assignment getLastAssignment() throws IOException
    {
        File directory = new File(getAssignmentsFolder());
        
        File newest = null;
        long time = 0;
        
        for(File f : directory.listFiles())
        {
            if(f.lastModified() > time)
            {
                time = f.lastModified();
                newest =f ;
            }
        }
        return new Assignment(newest);
    }
    
    /**
     * Loads the specified {@link Assignment} into the associated {@link DTASimulator}
     * @param assign the {@link Assignment} to be loaded
     * @throws IOException if a file cannot be accessed
     */
    public void loadAssignment(Assignment assign) throws IOException
    {
        FileTransfer.copy(assign.getDemandFile(), getDemandFile());
        
        loadSimulator();
        
        getSimulator().loadAssignment(assign);
    }
    
    /**
     * Returns the {@link DTASimulator} associated with this project. 
     * If null, call {@link DTAProject#loadSimulator()}
     * @return the {@link DTASimulator} associated with this project
     */
    public DTASimulator getSimulator()
    {
        System.out.println("DTAProject.getSimulator called");
        return (DTASimulator)super.getSimulator();
    }
    
    /**
     * Loads the {@link DTASimulator} using {@link ReadDTANetwork}
     * @throws IOException if a file is not found
     */
    public void loadSimulator() throws IOException
    {
        System.out.println("DTAProject.loadSimulator called");
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
        
        File file = null;
        if (RunSimulation.testing) {
            file = new File(dirStr+"/assignments");
        } else {
            file = new File(dirStr+"/DataCollection");
        }
        
        file.mkdirs();
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
        if (RunSimulation.testing) {
            return getProjectDirectory()+"/assignments/";
        } else {
            return getProjectDirectory()+"/DataCollection/";
        }
        //return getProjectDirectory()+"/assignments/";
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
    
    
    /**
     * Opens an {@link Assignment} from the given {@link Assignment} folder and assigns vehicles as specified.
     * This calls {@link DTASimulator#loadAssignment(avdta.dta.Assignment)}.
     * To obtain data about the {@link Assignment}, call {@link DTASimulator#simulate()}.
     * @param file the assignment directory
     * @throws IOException if a file cannot be accessed
     */
    public void openAssignment(File file) throws IOException
    {
        loadAssignment(new Assignment(file));
    }
    
    
    
}
