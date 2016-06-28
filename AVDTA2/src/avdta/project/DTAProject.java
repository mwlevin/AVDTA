/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.util.FileTransfer;
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
    
    public DTAProject(File directory) throws IOException
    {
        super(directory);
    }
    
    
    public DTASimulator getSimulator()
    {
        return (DTASimulator)super.getSimulator();
    }
    
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
            //ex.printStackTrace(System.err);
            setSimulator(createEmptySimulator());
        }

    }
    
    public void createProjectFolders(File dir) throws IOException
    {
        super.createProjectFolders(dir);
        
        String dirStr = dir.getCanonicalPath();
        
        File file = new File(dirStr+"/assignments");
        file.mkdirs();
        
        PrintStream fileout = new PrintStream(getProjectDirectory()+"/dta.dat");
        fileout.close();
    }
    
    public DTASimulator createEmptySimulator()
    {
        return new DTASimulator(this);
    }
    

    public File getStaticODFile()
    {
        return new File(getProjectDirectory()+"/network/static_od.txt");
    }
    
    public File getDynamicODFile()
    {
        return new File(getProjectDirectory()+"/network/dynamic_od.txt");
    }
    
    public File getDemandProfileFile()
    {
        return new File(getProjectDirectory()+"/network/demand_profile.txt");
    }
    
    public File getDemandFile()
    {
        return new File(getProjectDirectory()+"/network/demand.txt");
    }
    
    
    
    public File getOptionsFile()
    {
        return new File(getProjectDirectory()+"/network/options.txt");
    }
    
    public String getType()
    {
        return "DTA";
    }
    
    
    public void importNetworkFromProject(DTAProject rhs) throws IOException
    {
        // copy demand, static_od, dynamic_od, demand_profile
        
        super.importNetworkFromProject(rhs);
        
        FileTransfer.copy(rhs.getDemandFile(), getDemandFile());
        FileTransfer.copy(rhs.getStaticODFile(), getStaticODFile());
        FileTransfer.copy(rhs.getDynamicODFile(), getDynamicODFile());
        FileTransfer.copy(rhs.getDemandProfileFile(), getDemandProfileFile());
    }
}
