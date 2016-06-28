/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.util.FileTransfer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
    private Random rand;
    
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
    
    public String getOption(String k)
    {
        return networkOptions.get(k.toLowerCase());
    }
    
    public void writeOptions() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(getOptionsFile()), true);

        fileout.println("key\tvalue");
        for(String k : networkOptions.keySet())
        {
            fileout.println(k+"\t"+networkOptions.get(k));
        }
        
        fileout.close();
    }
    
    public void setOption(String key, String val)
    {
        networkOptions.put(key, val);
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
            map.put(filein.next().trim(), filein.next().trim());
        }
       
        
        filein.close();
        
        randSeed = Integer.parseInt(map.get("seed"));
        rand = new Random(randSeed);
        
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
    }
    
    public void fillOptions()
    {
        setOption("simulation-mesoscopic-delta","0.5");
        setOption("simulation-duration","36000");
        setOption("hv-reaction-time","1");
        setOption("av-reaction-time","0.5");
        setOption("hvs-use-reservations","0");
        setOption("dynamic-lane-reversal","0");
        setOption("simulation-mesoscopic-step","6");
        
        
    }
    
    public Simulator createEmptySimulator()
    {
        return new Simulator(this);
    }
    
    public Random getRandom()
    {
        return rand;
    }
    

    public void changeRandSeed()
    {
        randSeed = (int)(System.nanoTime()/1.0e10);
        
        rand = new Random(randSeed);
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
    
    public File getPropertiesFile()
    {
        return new File(getProjectDirectory()+"/project.dat");
    }
    
    public File getLinksFile()
    {
        return new File(getProjectDirectory()+"/network/links.txt");
    }
    
    public File getPhasesFile()
    {
        return new File(getProjectDirectory()+"/network/phases.txt");
    }
    
    public File getOptionsFile()
    {
        return new File(getProjectDirectory()+"/network/options.txt");
    }
    
    public String getProjectDirectory()
    {
        return directory;
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
    }
}
