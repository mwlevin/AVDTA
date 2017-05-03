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
import java.io.IOException;


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
}
