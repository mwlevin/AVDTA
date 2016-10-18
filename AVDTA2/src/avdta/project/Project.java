/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.util.FileTransfer;
import java.sql.Connection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;
import avdta.network.Network;

/**
 * This abstract class represents any project that contains a network (which most do). 
 * It defines an API for accessing network data files. 
 * A project is intended to represent a network scenario. 
 * Changes to the network are best handled by creating a clone of the current project. 
 * Projects may be cloned by copying the project folder or by invoking the API.
 * {@link Project} subclasses add APIs for accessing additional files needed by other project types.
 * 
 * Working with {@link Project}s usually involves the following: create a {@link Project} subclass with the desired directory.
 * Then, access the {@link Simulator} via {@link Project#getSimulator()}.
 * @author Michael
 */
public abstract class Project 
{
    private String name;
    
    private int randSeed;
    
    private Simulator simulator;
    
    private Map<String, String> networkOptions;
    
    private String directory;
    
    private Random rand;
    
    /**
     * Constructs an empty {@link Project}
     */
    public Project()
    {
        networkOptions = new TreeMap<String, String>();
    }
    
    /**
     * Instructs the {@link Network} to save its data.
     * @throws IOException if the file is not found
     */
    public void save() throws IOException
    {
        getSimulator().save(this);
    }
    
    /**
     * Returns the type indicator {@link String} used to create the indicator file to determine the type of this project.
     * @return the type indicator {@link String}
     */
    public abstract String getTypeIndicator();
    
    /**
     * Updates the {@link Simulator} associated with this {@link Project}
     * @param sim the new {@link Simulator}
     */
    public void setSimulator(Simulator sim)
    {
        this.simulator = sim;
    }
    
    /**
     * Loads the {@link Simulator} associated with this {@link Project} using {@link ReadNetwork}.
     * @throws IOException if the file is not found
     */
    public void loadSimulator() throws IOException
    {
        ReadNetwork read = new ReadNetwork();       
        
        try
        {
            Simulator output = read.readNetwork(this);
        
            simulator =  output;
        }
        catch(Exception ex)
        {
            simulator = createEmptySimulator();
        }

    }
    
    /**
     * Exports the network data to SQL so that the data can be manipulated in SQL.
     * @throws SQLException if the database cannot be accessed
     * @throws IOException if the file is not found
     * @see SQLLogin
     */
    public void exportNetworkToSQL() throws SQLException, IOException
    {
        SQLLogin login = new SQLLogin(getDatabaseName());
        Connection connect = DriverManager.getConnection(login.toString());
        login = null;
        
        // nodes, links, options, phases
        String dir = getProjectDirectory()+"/temp";
        File folder = new File(dir);
        folder.mkdirs();
        
        createTempFile(getNodesFile(), new File(dir+"/nodes.txt"));
        createTempFile(getLinksFile(), new File(dir+"/links.txt"));
        createTempFile(getPhasesFile(), new File(dir+"/phases.txt"));
        createTempFile(getOptionsFile(), new File(dir+"/options.txt"));
        createTempFile(getSignalsFile(), new File(dir+"/signals.txt"));
        
        Statement st = connect.createStatement();
        st.executeQuery("create table if not exists options (name varchar(255), value varchar(255));");
        st.executeQuery("create table if not exists nodes (id int, type int, longitude float, latitude float, elevation float);");
        st.executeQuery("create table if not exists links (id int, type int, source int, dest int, length float, ffspd float, w float, capacity float, num_lanes int);");
        st.executeQuery("create table if not exists phases (node int, type int, sequence int, time_red float, time_yellow float, "
                + "time_green float, num_moves int, link_from varchar(255), link_to varchar(255)));");
        st.executeQuery("create table if not exists signals(nodeid int, offset float);");
        
        
        st.executeQuery("\\copy nodes from temp/nodes.txt");
        st.executeQuery("\\copy links from temp/links.txt");
        st.executeQuery("\\copy options from temp/options.txt");
        st.executeQuery("\\copy phases from temp/phases.txt");
        st.executeQuery("\\copy signals from temp/signals.txt");
        
        
        for(File file : folder.listFiles())
        {
            file.delete();
        }
        folder.delete();
        
    }
    
    /**
     * Imports the network data from an external SQL database.
     * @throws SQLException if the database cannot be accessed
     * @throws IOException if the file is not found
     * @see SQLLogin
     */
    public void importNetworkFromSQL() throws SQLException, IOException
    {
        SQLLogin login = new SQLLogin(getDatabaseName());
        Connection connect = DriverManager.getConnection(login.toString());
        login = null;
        
        String dir = getProjectDirectory()+"/temp";
        File folder = new File(dir);
        folder.mkdirs();
        
        Statement st = connect.createStatement();
        st.executeQuery("\\copy nodes to temp/nodes.txt");
        st.executeQuery("\\copy links to temp/links.txt");
        st.executeQuery("\\copy options to temp/options.txt");
        st.executeQuery("\\copy phases to temp/phases.txt");
        st.executeQuery("\\copy signals to temp/signals.txt");
        
        createRealFile(new File(dir+"/nodes.txt"), ReadNetwork.getNodesFileHeader(), getNodesFile());
        createRealFile(new File(dir+"/links.txt"), ReadNetwork.getLinksFileHeader(), getLinksFile());
        createRealFile(new File(dir+"/phases.txt"), ReadNetwork.getPhasesFileHeader(), getPhasesFile());
        createRealFile(new File(dir+"/options.txt"), ReadNetwork.getOptionsFileHeader(), getOptionsFile());
        createRealFile(new File(dir+"/signals.txt"), ReadNetwork.getSignalsFileHeader(), getSignalsFile());
        
        for(File file : folder.listFiles())
        {
            file.delete();
        }
        folder.delete();
    }
    
    /**
     * Copies a data file but removes the header data. Used for exporting to SQL.
     * @param input the real data file
     * @param output the file without header data
     * @throws IOException if the file is not found
     */
    protected void createTempFile(File input, File output) throws IOException
    {
        Scanner filein = new Scanner(input);
        PrintStream fileout = new PrintStream(new FileOutputStream(output), true);
        
        filein.nextLine();
        
        while(filein.hasNextLine())
        {
            fileout.print(filein.nextLine());
        }
        filein.close();
        fileout.close();
    }
    
    /**
     * Copies a data file but removes the header data. Used for exporting to SQL.
     * @param input the data file without the header
     * @param header the header to be added
     * @param output the file with header data
     * @throws IOException if the file is not found
     */
    public void createRealFile(File input, String header, File output) throws IOException
    {
        Scanner filein = new Scanner(input);
        PrintStream fileout = new PrintStream(new FileOutputStream(output), true);
        
        fileout.println(header);
        
        while(filein.hasNextLine())
        {
            fileout.print(filein.nextLine());
        }
        filein.close();
        fileout.close();
    }
    
    
    
    /**
     * Creates a SQL database for this project.
     * @throws SQLException if the database cannot be accessed
     * @throws IOException if the file is not found
     * @see SQLLogin
     */
    public void createDatabase() throws SQLException, IOException
    {
        SQLLogin login = new SQLLogin();
        
        Connection connection = DriverManager.getConnection(login.toString()); 
        Statement s=connection.createStatement();
        int Result=s.executeUpdate("CREATE DATABASE "+getDatabaseName());
    }
    
    /**
     * Returns the database name used for SQL
     * @return {@link Project#getName()} replacing spaces with "_"
     */
    public String getDatabaseName()
    {
        return getName().replaceAll(" ", "_");
    }
    
    /**
     * Returns the value associated with the specified key
     * @param k the key
     * @return the value associated with the specified key
     */
    public String getOption(String k)
    {
        return networkOptions.get(k.toLowerCase());
    }
    
    /**
     * Writes the options file
     * @throws IOException if the file is not found
     */
    public void writeOptions() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(getOptionsFile()), true);

        fileout.println(ReadNetwork.getOptionsFileHeader());
        for(String k : networkOptions.keySet())
        {
            fileout.println(k+"\t"+networkOptions.get(k));
        }
        
        fileout.close();
    }
    
    /**
     * Adds/updates the options to contain the specified key and value
     * @param key the key
     * @param val the value
     */
    public void setOption(String key, String val)
    {
        networkOptions.put(key.toLowerCase(), val);
    }
    
    /**
     * A {@link String} describing the type of project
     * @return a {@link String} describing the type of project
     */
    public abstract String getType();
    
    /**
     * Returns the stored {@link Simulator} associated with this {@link Project}. If null, call {@link Project#getSimulator()}
     * @return the stored {@link Simulator} associated with this {@link Project}. If null, call {@link Project#getSimulator()}
     */
    public Simulator getSimulator()
    {
        return simulator;
    }
    
    /**
     * Loads a project from the specified directory.
     * This reads the project and constructs the associated {@link Simulator}.
     * @param dir The should be the project directory, not one of the project data files
     * @throws IOException if the file is not found
     */
    public Project(File dir) throws IOException
    {
        networkOptions = new TreeMap<String, String>();
        setDirectory(dir);
        loadProject();
    }
    
    /**
     * Updates the project directory 
     * @param dir the new directory
     * @throws IOException if the file is not found
     */
    public void setDirectory(File dir) throws IOException
    {
        this.directory = dir.getCanonicalPath();
        
        if(directory.indexOf("project.dat") >= 0)
        {
            directory = directory.substring(0, directory.lastIndexOf("/")+1);
        }
    }
    
    
    /**
     * Loads the project properties and associated {@link Simulator}.
     * Calls {@link Project#readProperties()} and {@link Project#loadSimulator()}
     * @throws IOException if the file is not found
     */
    public void loadProject() throws IOException
    {
        readProperties();
        loadSimulator();
    }
    
    
    /**
     * Reads the project properties file, looking for the project name and random seed
     * @throws IOException if the file is not found
     */
    public void readProperties() throws IOException
    {
        Scanner filein = new Scanner(getPropertiesFile());
        
        // ignore title
        filein.nextLine();
        
        Map<String, String> map = new HashMap<String, String>();
        
        while(filein.hasNext())
        {
            map.put(filein.next().trim().toLowerCase(), filein.next().trim());
        }
       
        
        filein.close();
        
        randSeed = Integer.parseInt(map.get("seed"));
        
        name = map.get("name");
        
        loadProperties(map);
    }
    
    /**
     * This method may be overridden by subclasses to read additional properties
     * @param properties a map of properties
     */
    public void loadProperties(Map<String, String> properties)
    {
        
    }
    
    /**
     * Returns the project name
     * @return the project name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Creates the project folders
     * @param dir the new project folder
     * @throws IOException if the file is not found
     */
    public void createFolder(File dir) throws IOException
    {
        
        String dirStr = dir.getCanonicalPath();
        dirStr += "/"+name;
        
        File file = new File(dirStr);
        file.mkdirs();
        
        
        createProjectFolders(new File(dirStr));
    }
    
    /**
     * Creates an empty project, including empty placeholder data files
     * @param name the new project name
     * @param dir the project directory
     * @throws IOException if the file is not found
     */
    public void createProject(String name, File dir) throws IOException
    {
        this.name = name;
        setDirectory(dir);
        createProjectFolders(new File(getProjectDirectory()));
        fillOptions();
        writeOptions();
        
        changeRandSeed();
        writeProperties();
        
        simulator = createEmptySimulator();
        
        writeEmptyFiles();
    }
    
    /**
     * Writes empty files. Subclasses should override this method to create any files they need.
     * @throws IOException if the file is not found
     */
    public void writeEmptyFiles() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(getLinksFile()), true);
        fileout.println(ReadNetwork.getLinksFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getPhasesFile()), true);
        fileout.println(ReadNetwork.getPhasesFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getNodesFile()), true);
        fileout.println(ReadNetwork.getNodesFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getSignalsFile()), true);
        fileout.println(ReadNetwork.getNodesFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getLinkPointsFile()), true);
        fileout.println(ReadNetwork.getLinkPointsFileHeader());
        fileout.close();
    }
    
    /**
     * Fills the project options with default values
     */
    public void fillOptions()
    {
        setOption("simulation-duration","36000");
        setOption("ast-duration","900");
        setOption("hv-reaction-time","1");
        setOption("av-reaction-time","0.5");
        setOption("hvs-use-reservations","false");
        setOption("dynamic-lane-reversal","false");
        setOption("simulation-mesoscopic-step","6");
        
        
    }
    
    /**
     * Creates an empty {@link Simulator}
     * @return the empty {@link Simulator}
     */
    public Simulator createEmptySimulator()
    {
        simulator = new Simulator(this);
        return simulator;
    }
    
    /**
     * Returns the random number generator. 
     * A project-wide random number generator is used to obtain repeatable results.
     * @return the random number generator.
     */
    public Random getRandom()
    {
        if(rand == null)
        {
            rand = new Random(randSeed);
        }
        
        return rand;
    }
    
    /**
     * Clone another project. 
     * This copies network data files from the other project into this one.
     * Note that this will overwrite existing data.
     * @param rhs the project to be cloned
     * @throws IOException if the file is not found
     */
    public void cloneFromProject(Project rhs) throws IOException
    {
        importNetworkFromProject(rhs);
    }

    /**
     * Resets the random seed and saves it. 
     * @throws IOException if the file is not found
     */
    public void changeRandSeed() throws IOException
    {
        randSeed = (int)(System.nanoTime()/1.0e10);
        rand = new Random(randSeed);
        writeProperties();
    }
    
    /**
     * Saves the project properties
     * @throws IOException if the file is not found
     */
    public void writeProperties() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(getPropertiesFile()), true);

        fileout.println("key\tvalue");
        
        Map<String, String> map = createPropertiesMap();
        
        map.put("type", getType());
        map.put("seed", ""+randSeed);
        map.put("name", getName());
        
        
        for(String k : map.keySet())
        {
            fileout.println(k+"\t"+map.get(k));
        }


        fileout.close();
    }
    
    /**
     * Creates an empty {@link TreeMap} for project properties. Subclasses may override this to add properties.
     * @return an empty map of properties
     */
    public Map<String, String> createPropertiesMap()
    {
        return new TreeMap<String, String>();
    }
    
    /**
     * Creates project subfolders in the specified directory
     * @param dir the project directory
     * @throws IOException if the file is not found
     */
    public void createProjectFolders(File dir) throws IOException
    {
        String dirStr = dir.getCanonicalPath();
        
        File file = new File(dirStr+"/network");
        file.mkdirs();
        
        file = new File(dirStr+"/results");
        file.mkdirs();
        
        PrintStream fileout = new PrintStream(getProjectDirectory()+"/"+getTypeIndicator()+".dat");
        fileout.close();
        
    }

    
    /**
     * Returns the nodes file
     * @return {@link Project#getProjectDirectory()}/network/nodes.txt
     */
    public File getNodesFile()
    {
        return new File(getProjectDirectory()+"/network/nodes.txt");
    }
    
    /**
     * Returns the link coordinates file
     * @return {@link Project#getProjectDirectory()}/network/link_coordinates.txt
     */
    public File getLinkPointsFile()
    {
        return new File(getProjectDirectory()+"/network/link_coordinates.txt");
    }
    
    /**
     * Returns the project properties file
     * @return {@link Project#getProjectDirectory()}/project.txt
     */
    public File getPropertiesFile()
    {
        return new File(getProjectDirectory()+"/project.txt");
    }
    
    /**
     * Returns the links file
     * @return {@link Project#getProjectDirectory()}/network/links.txt
     */
    public File getLinksFile()
    {
        return new File(getProjectDirectory()+"/network/links.txt");
    }
    
    /**
     * Returns the phases file
     * @return {@link Project#getProjectDirectory()}/network/phases.txt
     */
    public File getPhasesFile()
    {
        return new File(getProjectDirectory()+"/network/phases.txt");
    }
    
    /**
     * Returns the signals file
     * @return {@link Project#getProjectDirectory()}/network/signals.txt
     */
    public File getSignalsFile()
    {
        return new File(getProjectDirectory()+"/network/signals.txt");
    }
    
    /**
     * Returns the options file
     * @return {@link Project#getProjectDirectory()}/options.txt
     */
    public File getOptionsFile()
    {
        return new File(getProjectDirectory()+"/options.txt");
    }
    
    /**
     * Returns the project directory in String form. Used for finding data files and subfolders.
     * @return the project directory in String form
     */
    public String getProjectDirectory()
    {
        return directory;
    }
    
    /**
     * Returns the paths file
     * @return {@link Project#getProjectDirectory()}/paths.dat
     */
    public File getPathsFile()
    {
        return new File(getProjectDirectory()+"/paths.dat");
    }
    
    /**
     * Returns the results folder
     * @return {@link Project#getProjectDirectory()}/results/
     */
    public String getResultsFolder()
    {
        return getProjectDirectory()+"/results/";
    }
    
    /**
     * Copies the nodes, links, options, phases, and signals file from another project.
     * @param rhs the project to copy files from
     * @throws IOException if the file is not found
     */
    public void importNetworkFromProject(Project rhs) throws IOException
    {
        // clone nodes, links, options, phases
        FileTransfer.copy(rhs.getNodesFile(), getNodesFile());
        FileTransfer.copy(rhs.getLinksFile(), getLinksFile());
        FileTransfer.copy(rhs.getOptionsFile(), getOptionsFile());
        FileTransfer.copy(rhs.getPhasesFile(), getPhasesFile());
        FileTransfer.copy(rhs.getSignalsFile(), getSignalsFile());
    }
    
    /**
     * Returns the project name
     * @return the project name
     */
    public String toString()
    {
        return getName();
    }
}
