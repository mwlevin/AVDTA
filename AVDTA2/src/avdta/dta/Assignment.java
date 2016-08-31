/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.network.Path;
import avdta.network.PathList;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.project.DTAProject;
import avdta.util.RunningAvg;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Relies on vehicles to be sorted by dtime then id
 * @author micha
 */
public class Assignment implements Comparable<Assignment>
{
    private DTAResults results;
    private int time;
    
    private String name;
    private String directory;
 
    
    public Assignment(File dir) throws IOException
    {
        directory = dir.getCanonicalPath();
        
        
        Scanner filein = new Scanner(getPropertiesFile());
        
        readAssignment(filein);
        
        filein.close();

    }
    
    public long getTime()
    {
        return time;
    }
    
    public String getAssignmentDirectory()
    {
        return directory;
    }
    
    public File getPropertiesFile()
    {
        return new File(getAssignmentDirectory()+"/properties.dat");
    }
    
    public String getName()
    {
        return name;
    }
    
    public String toString()
    {
        return getName();
    }
    
    public int compareTo(Assignment rhs)
    {
        return rhs.time - time;
    }
    
    public void readAssignment(Scanner filein)
    {
        name = filein.nextLine();
        
        double mintt = filein.nextDouble();
        double tstt = filein.nextDouble();
        int num_veh = filein.nextInt();
        int exiting = filein.nextInt();
        results = new DTAResults(mintt*3600.0, tstt*3600.0, num_veh, exiting);
    }
    
    public Assignment(DTAProject project, DTAResults results)
    {
        this(project, results, Calendar.getInstance().getTime().toString());
    }
    
    public Assignment(DTAProject project, DTAResults results, String name)
    {
        this(project, results, ""+(int)(System.nanoTime()/1.0e9), name);
    }
    
    public Assignment(DTAProject project, DTAResults results, String folderName, String name)
    {
        this.results = results;
        this.name = name;
        
        directory = project.getAssignmentsFolder()+"/"+folderName;
        
        File file = new File(directory);
        file.mkdirs();
        
        time = (int)(System.currentTimeMillis()/1000.0);
    }
    
    public void setResults(DTAResults results)
    {
        this.results = results;
    }
    public DTAResults getResults()
    {
        return results;
    }
    
    public File getVehiclesFile()
    {
        return new File(getAssignmentDirectory()+"/vehicles.dat");
    }
    
    public File getTimesFile()
    {
        return new File(getAssignmentDirectory()+"/linktt.dat");
    }
    
    public void writeToFile(List<Vehicle> vehicles, DTAProject project) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(getPropertiesFile()), true);
        fileout.println(getName());
        fileout.println(getResultsData());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getVehiclesFile()), true);
        
        for(Vehicle v : vehicles)
        {
            fileout.println(v.getId()+"\t"+v.getPath().getId());
        }
        
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getTimesFile()), true);
        for(Link l : project.getSimulator().getLinks())
        {
            fileout.print(l.getId());
            
            RunningAvg[] tts = l.getAvgTTs();
            
            for(int t = 0; t < tts.length; t++)
            {
                fileout.print("\t"+(t*Simulator.ast_duration)+"\t"+tts[t].getAverage());
            }
            
            fileout.println();
        }
        fileout.close();
        
    }
    
    public String getResultsData()
    {
        return (results.getMinTT()/3600.0)+"\t"+results.getTSTT()+"\t"+results.getNumVeh()+"\t"+results.getNumExiting();
    }
    
    
    public void readFromFile(DTAProject project, List<Vehicle> vehicles, PathList pathlist) throws IOException
    {
        Map<Integer, Path> paths = pathlist.createPathIdsMap();
        
        Scanner filein = new Scanner(getVehiclesFile());
        
        filein.nextLine();
        
        for(Vehicle v : vehicles)
        {
            int id = filein.nextInt();
            
            if(v.getId() != id)
            {
                throw new RuntimeException("Id mismatch when reading assignment");
            }
            
            int path_id = filein.nextInt();
            Path p = paths.get(path_id);
            
            if(p == null)
            {
                throw new RuntimeException("Missing path when reading assignment - "+path_id);
            }
            
            v.setPath(p);
        }
        filein.close();
        
        Map<Integer, Link> linksMap = new HashMap<Integer, Link>();
        
        for(Link l : project.getSimulator().getLinks())
        {
            linksMap.put(l.getId(), l);
        }
        
        filein = new Scanner(getTimesFile());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            Scanner chopper = new Scanner(filein.nextLine());
            
            Link link = linksMap.get(id);
            
            if(link == null)
            {
                continue;
            }
            
            int prev_t = 0;
            double prev_tt = link.getFFTime();
            while(chopper.hasNextInt())
            {
                int t = chopper.nextInt();
                double tt = chopper.nextDouble();
                
                link.setAvgTT(t, tt);
                
                prev_t = t;
                prev_tt = tt;
            }
        }
        filein.close();
    }
    
    
}
