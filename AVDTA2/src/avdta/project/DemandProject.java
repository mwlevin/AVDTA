/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import avdta.demand.ReadDemandNetwork;
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
 * This project includes traveler demand data.
 * @author Michael
 */
public abstract class DemandProject extends TransitProject
{
    /**
     * Constructs an empty project
     */
    public DemandProject()
    {
        
    }
    
    /**
     * Constructs the project from the specified directory
     * @param directory the project directory
     * @throws IOException if a file is not found
     */
    public DemandProject(File directory) throws IOException
    {
        super(directory);
    }
    
    /**
     * Clones the project files from another project. 
     * Note that this will overwrite the files in this project
     * @param rhs the project to be cloned
     * @throws IOException if a file is not found
     */
    public void cloneFromProject(DemandProject rhs) throws IOException
    {
        super.cloneFromProject(rhs);
        importDemandFromProject(rhs);
    }
    
     /**
     * Writes empty demand files. Also calls {@link TransitProject#writeEmptyFiles()}
     * @throws IOException if a file cannot be created
     */
    public void writeEmptyFiles() throws IOException
    {
        super.writeEmptyFiles();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(getDemandFile()), true);
        fileout.println(ReadDemandNetwork.getDemandFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getDynamicODFile()), true);
        fileout.println(ReadDemandNetwork.getDynamicODFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getStaticODFile()), true);
        fileout.println(ReadDemandNetwork.getStaticODFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getDemandProfileFile()), true);
        fileout.println(ReadDemandNetwork.getDemandProfileFileHeader());
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
        
     
        
        File file = new File(dirStr+"/demand");
        file.mkdirs();
        
        
    }
    
    
    
    /**
     * Returns the static OD table file
     * @return {@link Project#getProjectDirectory()}/demand/static_od.txt
     */
    public File getStaticODFile()
    {
        return new File(getProjectDirectory()+"/demand/static_od.txt");
    }
    
    /**
     * Returns the dynamic OD table file
     * @return {@link Project#getProjectDirectory()}/demand/dynamic_od.txt
     */
    public File getDynamicODFile()
    {
        return new File(getProjectDirectory()+"/demand/dynamic_od.txt");
    }
    
    /**
     * Returns the demand profile file
     * @return {@link Project#getProjectDirectory()}/demand/demand_profile.txt
     */
    public File getDemandProfileFile()
    {
        return new File(getProjectDirectory()+"/demand/demand_profile.txt");
    }
    
    /**
     * Returns the demand file
     * @return {@link Project#getProjectDirectory()}/demand/demand.txt
     */
    public File getDemandFile()
    {
        return new File(getProjectDirectory()+"/demand/demand.txt");
    }
    
    /**
     * Copies demand files from the specified project
     * @param rhs the project to copy files from
     * @throws IOException if a file is not found
     */
    public void importDemandFromProject(DemandProject rhs) throws IOException
    {
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
        
        createRealFile(new File(dir+"/demand.txt"), ReadDemandNetwork.getDemandFileHeader(), getDemandFile());
        createRealFile(new File(dir+"/dynamic_od.txt"), ReadDemandNetwork.getDynamicODFileHeader(), getDynamicODFile());
        createRealFile(new File(dir+"/static_od.txt"), ReadDemandNetwork.getStaticODFileHeader(), getStaticODFile());
        createRealFile(new File(dir+"/demand_profile.txt"), ReadDemandNetwork.getDemandProfileFileHeader(), getDemandProfileFile());
        
        for(File file : folder.listFiles())
        {
            file.delete();
        }
        folder.delete();
    }
}
