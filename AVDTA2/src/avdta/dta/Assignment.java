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
    
    public Assignment(File input) throws IOException
    {
        name = input.getCanonicalPath();
        name = name.substring(0, name.lastIndexOf("\\".charAt(0)));
        name = name.substring(name.lastIndexOf("\\".charAt(0))+1, name.length());
        
        time = (int)(input.lastModified()/1000);
        
        // read header data
        
        Scanner filein = new Scanner(input);
        
        readAssignment(filein);
        
        filein.close();
        
        
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
        double mintt = filein.nextDouble();
        double tstt = filein.nextDouble();
        int num_veh = filein.nextInt();
        int exiting = filein.nextInt();
        results = new DTAResults(mintt*3600.0, tstt*3600.0, num_veh, exiting);
    }
    
    public Assignment(DTAProject project, DTAResults results)
    {
        this(project, results, ""+(int)(System.nanoTime()/1.0e9));
    }
    
    public Assignment(DTAProject project, DTAResults results, String name)
    {
        this.results = results;
        this.name = name;
        
        File file = new File(project.getAssignmentsFolder()+"/"+name);
        file.mkdirs();
    }
    
    public void setResults(DTAResults results)
    {
        this.results = results;
    }
    public DTAResults getResults()
    {
        return results;
    }
    
    public File getVehiclesFile(DTAProject project)
    {
        return new File(project.getAssignmentsFolder()+"/"+getName()+"/vehicles.dat");
    }
    
    public File getTimesFile(DTAProject project)
    {
        return new File(project.getAssignmentsFolder()+"/"+getName()+"/linktt.dat");
    }
    
    public void writeToFile(List<Vehicle> vehicles, DTAProject project) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(getVehiclesFile(project)), true);
        fileout.println(getHeaderData());
        
        for(Vehicle v : vehicles)
        {
            fileout.println(v.getId()+"\t"+v.getPath().getId());
        }
        
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getTimesFile(project)), true);
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
    
    public String getHeaderData()
    {
        return (results.getMinTT()/3600.0)+"\t"+results.getTSTT()+"\t"+results.getNumVeh()+"\t"+results.getNumExiting();
    }
    
    
    public void readFromFile(DTAProject project, List<Vehicle> vehicles, PathList pathlist) throws IOException
    {
        Map<Integer, Path> paths = pathlist.createPathIdsMap();
        
        Scanner filein = new Scanner(getVehiclesFile(project));
        
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
        
        filein = new Scanner(getTimesFile(project));
        
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
