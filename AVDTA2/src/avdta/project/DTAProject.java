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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
    
    public void exportDemandToSQL() throws SQLException, IOException
    {
        SQLLogin login = new SQLLogin(getDatabaseName());
        Connection connect = DriverManager.getConnection(login.toString());
        login = null;
        
        // nodes, links, options, phases
        String dir = getProjectDirectory()+"/temp";
        File folder = new File(dir);
        folder.mkdirs();
        
        createTempFile(getDemandFile(), new File(dir+"/demand.txt"));
        createTempFile(getStaticODFile(), new File(dir+"/static_od.txt"));
        createTempFile(getDynamicODFile(), new File(dir+"/dynamic_od.txt"));
        createTempFile(getDemandProfileFile(), new File(dir+"/demand_profile.txt"));
        
        Statement st = connect.createStatement();
        st.executeQuery("create table if not exists demand (id int, type int, origin int, dest int, dtime int, vot float);");
        st.executeQuery("create table if not exists demand_profile (id int, weight float, start int, duration int);");
        st.executeQuery("create table if not exists dynamic_od (id int, type int, origin int, destination int, ast int, demand float);");
        st.executeQuery("create table if not exists static_od (id int, type int, origin int, destination int, demand float);");
        
        st.executeQuery("\\copy demand from temp/demand.txt");
        st.executeQuery("\\copy static_od from temp/static_od.txt");
        st.executeQuery("\\copy dynamic_od from temp/dynamic_od.txt");
        st.executeQuery("\\copy demand_profile from temp/demand_profile.txt");
        
        
        for(File file : folder.listFiles())
        {
            file.delete();
        }
        folder.delete();
        
    }
    
    public void importDemandFromSQL() throws SQLException, IOException
    {
        SQLLogin login = new SQLLogin(getDatabaseName());
        Connection connect = DriverManager.getConnection(login.toString());
        login = null;
        
        String dir = getProjectDirectory()+"/temp";
        File folder = new File(dir);
        folder.mkdirs();
        
        Statement st = connect.createStatement();
        st.executeQuery("\\copy demand to temp/demand.txt");
        st.executeQuery("\\copy dynamic_od to temp/dynamic_od.txt");
        st.executeQuery("\\copy static_od to temp/static_od.txt");
        st.executeQuery("\\copy demand_profile to temp/demand_profile.txt");
        
        createRealFile(new File(dir+"/demand.txt"), ReadDTANetwork.getDemandFileHeader(), getDemandFile());
        createRealFile(new File(dir+"/dynamic_od.txt"), ReadDTANetwork.getDynamicODFileHeader(), getDynamicODFile());
        createRealFile(new File(dir+"/static_od.txt"), ReadDTANetwork.getStaticODFileHeader(), getStaticODFile());
        createRealFile(new File(dir+"/demand_profile.txt"), ReadDTANetwork.getDemandProfileFileHeader(), getDemandProfileFile());
        
        for(File file : folder.listFiles())
        {
            file.delete();
        }
        folder.delete();
    }
}
