/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;


import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.duer.DUERSimulator;
import avdta.duer.ReadDUERNetwork;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;


/**
 *
 * @author ml26893
 */
public class DUERProject extends DTAProject
{
    public DUERProject()
    {
        super();
    }
    
    public DUERProject(File directory) throws IOException
    {
        super(directory);
    }
    
    /**
     * Creates an empty {@link DUERSimulator}
     * @return an empty {@link DUERSimulator}
     */
    public DUERSimulator createEmptySimulator()
    {
        return new DUERSimulator(this);
    }
    
    public void loadSimulator() throws IOException
    {
        try
        {
            ReadDUERNetwork read = new ReadDUERNetwork();  
            
            DUERSimulator output = read.readNetwork(this);
        
            setSimulator(output);
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
            setSimulator(createEmptySimulator());
        }

    }
    
    /**
     * Returns the {@link DUERSimulator} associated with this project. 
     * If null, call {@link DUERProject#loadSimulator()}
     * @return the {@link DUERSimulator} associated with this project
     */
    public DUERSimulator getSimulator()
    {
        return (DUERSimulator)super.getSimulator();
    }
    
    
    public File getIncidentsFile()
    {
        return new File(getProjectDirectory()+"/network/incidents.txt");
    }
    
    /**
     * Writes empty demand files. Also calls {@link TransitProject#writeEmptyFiles()}
     * @throws IOException if a file cannot be created
     */
    public void writeEmptyFiles() throws IOException
    {
        super.writeEmptyFiles();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(getIncidentsFile()), true);
        fileout.println(ReadDUERNetwork.getIncidentsFileHeader());
        fileout.close();
    }
}
