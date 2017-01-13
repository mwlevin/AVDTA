/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import avdta.demand.ReadDemandNetwork;
import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.fourstep.FourStepSimulator;
import avdta.fourstep.ReadFourStepNetwork;
import avdta.network.ReadNetwork;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author micha
 */
public class FourStepProject extends DTAProject
{
    private Map<String, String> networkOptions;
    
    public FourStepProject()
    {
        networkOptions = new TreeMap<String, String>();
    }
    
    public FourStepProject(File dir) throws IOException
    {
        super(dir);
        
        networkOptions = new TreeMap<String, String>();
    }
    
    /**
     * Returns the {@link DTASimulator} associated with this project. 
     * If null, call {@link DTAProject#loadSimulator()}
     * @return the {@link DTASimulator} associated with this project
     */
    public FourStepSimulator getSimulator()
    {
        return (FourStepSimulator)super.getSimulator();
    }
    
    /**
     * Returns the assignments folder
     * @return {@link Project#getProjectDirectory()}/assignments/
     */
    public String getAssignmentsFolder()
    {
        return getProjectDirectory()+"/assignments_fourstep";
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
        
     
        
        File file = new File(dirStr+"/fourstep");
        file.mkdirs();
        
        file = new File(dirStr+"/assignments_fourstep");
        file.mkdirs();
        
        createIndicatorFile(super.getTypeIndicator());
    }
    
    /**
     * Loads the {@link DTASimulator} using {@link ReadDTANetwork}
     * @throws IOException if a file is not found
     */
    public void loadSimulator() throws IOException
    {
        try
        {
            ReadFourStepNetwork read = new ReadFourStepNetwork();  
            
            FourStepSimulator output = read.readNetwork(this);
        
            setSimulator(output);
        }
        catch(Exception ex)
        {
            ex.printStackTrace(System.err);
            setSimulator(createEmptySimulator());
        }

    }
    
    /**
     * Creates an empty {@link DTASimulator}
     * @return an empty {@link DTASimulator}
     */
    public FourStepSimulator createEmptySimulator()
    {
        return new FourStepSimulator(this);
    }
    
    /**
     * Writes empty FourStep files. Also calls {@link DTAProject#writeEmptyFiles()}
     * @throws IOException if a file cannot be created
     */
    public void writeEmptyFiles() throws IOException
    {
        super.writeEmptyFiles();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(getZonesFile()), true);
        fileout.println(ReadFourStepNetwork.getZonesFileHeader());
        fileout.close();
        
    }
    
    /**
     * Returns the productions file.
     * @return {@link Project#getProjectDirectory()}/fourstep/zones.txt
     */
    public File getZonesFile()
    {
        return new File(getProjectDirectory()+"/fourstep/zones.txt");
    }
    
    /**
     * Returns the four-step options file.
     * @return {@link Project#getProjectDirectory()}/fourstep/options.txt
     */
    public File getFourStepOptionsFile()
    {
        return new File(getProjectDirectory()+"/fourstep/options.txt");
    }
    
    
    /**
     * Fills the project options with default values.
     * Also calls {@link Project#fillOptions()}.
     */
    public void fillOptions()
    {
        super.fillOptions();
        
        double vot = 3.198; // $/hr
        double TR_asc = -52.6, logit_dispersion = 0.0548, transitFee = 1;
        double AV_asc = 0;
    
    
        // arrival time penalty values
        double alpha = 1*vot, beta = 0.609375*vot, gamma = 2.376525*vot;
        
        setFourStepOption("allow-repositioning", "true");
        setFourStepOption("arrival-time-alpha", ""+alpha);
        setFourStepOption("arrival-time-beta", ""+beta);
        setFourStepOption("arrival-time-gamma", ""+gamma);
        setFourStepOption("transit-asc", ""+TR_asc);
        setFourStepOption("reposition-asc", ""+AV_asc);
        setFourStepOption("logit-dispersion", ""+logit_dispersion);
        setFourStepOption("transit-fee", ""+transitFee);
        setFourStepOption("all-avs", "true");
        setFourStepOption("fuel-cost", "2");
        setFourStepOption("demand-asts", "8");
    }
    
    /**
     * Adds/updates the options to contain the specified key and value
     * @param key the key
     * @param val the value
     */
    public void setFourStepOption(String key, String val)
    {
        networkOptions.put(key.toLowerCase(), val);
    }
    
    /**
     * Returns the value associated with the specified key
     * @param k the key
     * @return the value associated with the specified key
     */
    public String getFourStepOption(String k)
    {
        return networkOptions.get(k.toLowerCase());
    }
    
    /**
     * Returns the project type
     * @return FourStep
     */
    public String getType()
    {
        return "FourStep";
    }
    
    /**
     * Returns the type indicator {@link String} used to create the indicator file to determine the type of this project.
     * @return "fourstep"
     */
    public String getTypeIndicator()
    {
        return "fourstep";
    }
    
    /**
     * Writes the four-step options file.
     * Also calls {@link DTAProject#writeOptions()}.
     * @throws IOException if the file is not found
     */
    public void writeOptions() throws IOException
    {
        super.writeOptions();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(getFourStepOptionsFile()), true);

        fileout.println(ReadNetwork.getOptionsFileHeader());
        for(String k : networkOptions.keySet())
        {
            fileout.println(k+"\t"+networkOptions.get(k));
        }
        
        fileout.close();
    }
    
}
