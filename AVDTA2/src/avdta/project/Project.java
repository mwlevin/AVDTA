/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

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
    
    public Project()
    {
        
    }
    
    public Project(String name) throws IOException
    {
        this.name = name;
        loadProject();
    }
    
    public void loadProject() throws IOException
    {
        
        readProperties();
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
    
    public void createProject(String name) throws IOException
    {
        this.name = name;
        createProjectFolders(new File(getProjectDirectory()));
        
        changeRandSeed();
        writeProperties();
    }
    
    public Random getRandom()
    {
        return rand;
    }
    

    public void changeRandSeed()
    {
        randSeed = (int)(System.nanoTime()/1.0e6);
        
        rand = new Random(randSeed);
    }
    
    public void writeProperties() throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(getPropertiesFile()), true);

        fileout.println("key\tvalue");
        
        Map<String, String> map = createPropertiesMap();
        
        map.put("type", getType());
        map.put("seed", ""+randSeed);
        
        
        for(String k : map.keySet())
        {
            fileout.println(k+"\t"+map.get(k));
        }

        writeProperties(fileout);

        fileout.close();
    }
    
    public Map<String, String> createPropertiesMap()
    {
        return new TreeMap<String, String>();
    }
    
    public void writeProperties(PrintStream fileout)
    {
        
    }
    public abstract String getType();
    
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
        return new File(getProjectDirectory()+"/network/nodes.dat");
    }
    
    public File getPropertiesFile()
    {
        return new File(getProjectDirectory()+"/network/project.dat");
    }
    
    public File getLinksFile()
    {
        return new File(getProjectDirectory()+"/network/links.dat");
    }
    
    public File getPhasesFile()
    {
        return new File(getProjectDirectory()+"/network/phases.dat");
    }
    
    public File getOptionsFile()
    {
        return new File(getProjectDirectory()+"/network/options.dat");
    }
    
    public String getProjectDirectory()
    {
        return "/projects/"+getName();
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
