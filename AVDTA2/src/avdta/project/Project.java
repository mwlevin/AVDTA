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

/**
 *
 * @author micha
 */
public abstract class Project 
{
    private String name;
    
    private int randSeed;
    
    private Simulator simulator;
    
    private Map<String, String> networkOptions;
    
    private String directory;
    
    public Project()
    {
        networkOptions = new TreeMap<String, String>();
    }
    
    public void setSimulator(Simulator sim)
    {
        this.simulator = sim;
    }
    
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
    
    public void createTempFile(File input, File output) throws IOException
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
    
    
    
    
    public void createDatabase() throws SQLException, IOException
    {
        SQLLogin login = new SQLLogin();
        
        Connection connection = DriverManager.getConnection(login.toString()); 
        Statement s=connection.createStatement();
        int Result=s.executeUpdate("CREATE DATABASE "+getDatabaseName());
    }
    
    public String getDatabaseName()
    {
        return getName().replaceAll(" ", "_");
    }
    
    public String getOption(String k)
    {
        return networkOptions.get(k.toLowerCase());
    }
    
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
    
    public void setOption(String key, String val)
    {
        networkOptions.put(key.toLowerCase(), val);
    }
    
    public abstract String getType();
    
    public Simulator getSimulator()
    {
        return simulator;
    }
    
    public Project(File dir) throws IOException
    {
        networkOptions = new TreeMap<String, String>();
        setDirectory(dir);
        loadProject();
    }
    
    public void setDirectory(File dir) throws IOException
    {
        this.directory = dir.getCanonicalPath();
        
        if(directory.indexOf("project.dat") >= 0)
        {
            directory = directory.substring(0, directory.lastIndexOf("/")+1);
        }
    }
    
    public void loadProject() throws IOException
    {
        readProperties();
        loadSimulator();
    }
    
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
    
    public void loadProperties(Map<String, String> properties)
    {
        
    }
    
    public String getName()
    {
        return name;
    }
    
    public void createFolder(File dir) throws IOException
    {
        
        String dirStr = dir.getCanonicalPath();
        dirStr += "/"+name;
        
        File file = new File(dirStr);
        file.mkdirs();
        
        
        createProjectFolders(new File(dirStr));
    }
    
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
    
    public Simulator createEmptySimulator()
    {
        simulator = new Simulator(this);
        return simulator;
    }
    
    public Random getRandom()
    {
        return new Random(randSeed);
    }
    
    public void cloneFromProject(Project rhs) throws IOException
    {
        importNetworkFromProject(rhs);
    }

    public void changeRandSeed()
    {
        randSeed = (int)(System.nanoTime()/1.0e10);
    }
    
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
    
    public Map<String, String> createPropertiesMap()
    {
        return new TreeMap<String, String>();
    }
    
    public void createProjectFolders(File dir) throws IOException
    {
        String dirStr = dir.getCanonicalPath();
        
        File file = new File(dirStr+"/network");
        file.mkdirs();
        
        file = new File(dirStr+"/results");
        file.mkdirs();
        
    }

    
    
    public File getNodesFile()
    {
        return new File(getProjectDirectory()+"/network/nodes.txt");
    }
    
    public File getLinkPointsFile()
    {
        return new File(getProjectDirectory()+"/network/link_coordinates.txt");
    }
    
    public File getPropertiesFile()
    {
        return new File(getProjectDirectory()+"/project.txt");
    }
    
    public File getLinksFile()
    {
        return new File(getProjectDirectory()+"/network/links.txt");
    }
    
    public File getPhasesFile()
    {
        return new File(getProjectDirectory()+"/network/phases.txt");
    }
    
    
    public File getSignalsFile()
    {
        return new File(getProjectDirectory()+"/network/signals.txt");
    }
    
    public File getOptionsFile()
    {
        return new File(getProjectDirectory()+"/options.txt");
    }
    
    public String getProjectDirectory()
    {
        return directory;
    }
    
    public File getPathsFile()
    {
        return new File(getProjectDirectory()+"/paths.dat");
    }
    public String getResultsFolder()
    {
        return getProjectDirectory()+"/results/";
    }
    
    public void importNetworkFromProject(Project rhs) throws IOException
    {
        // clone nodes, links, options, phases
        FileTransfer.copy(rhs.getNodesFile(), getNodesFile());
        FileTransfer.copy(rhs.getLinksFile(), getLinksFile());
        FileTransfer.copy(rhs.getOptionsFile(), getOptionsFile());
        FileTransfer.copy(rhs.getPhasesFile(), getPhasesFile());
        FileTransfer.copy(rhs.getSignalsFile(), getSignalsFile());
    }
}
