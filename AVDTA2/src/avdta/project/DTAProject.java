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
    
    public void cloneFromProject(DTAProject rhs) throws IOException
    {
        super.cloneFromProject(rhs);
        importDemandFromProject(rhs);
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
            ex.printStackTrace(System.err);
            setSimulator(createEmptySimulator());
        }

    }
    
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
    
    public void createProjectFolders(File dir) throws IOException
    {
        super.createProjectFolders(dir);
        
        String dirStr = dir.getCanonicalPath();
        
        File file = new File(dirStr+"/assignments");
        file.mkdirs();
        
        file = new File(dirStr+"/demand");
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
    
    public File getDemandFile()
    {
        return new File(getProjectDirectory()+"/demand/demand.txt");
    }
    
    public String getAssignmentsFolder()
    {
        return getProjectDirectory()+"/assignments";
    }
    
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
    
    
    
    public String getType()
    {
        return "DTA";
    }
    
    public String toString()
    {
        return getName();
    }
    
    public void importDemandFromProject(DTAProject rhs) throws IOException
    {
        // copy demand, static_od, dynamic_od, demand_profile
        
        FileTransfer.copy(rhs.getDemandFile(), getDemandFile());
        FileTransfer.copy(rhs.getStaticODFile(), getStaticODFile());
        FileTransfer.copy(rhs.getDynamicODFile(), getDynamicODFile());
        FileTransfer.copy(rhs.getDemandProfileFile(), getDemandProfileFile());
    }
}
