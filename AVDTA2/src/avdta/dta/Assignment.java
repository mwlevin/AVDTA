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
 * This is a wrapper class for an assignment. An assignment specifies the {@link Path} that each {@link Vehicle} is using.
 * Because the number of vehicles may be large, the {@link Assignment} class stores data in a file.
 * An {@link Assignment} is stored as several files within a folder. See {@link DTAProject#getAssignmentsFolder()} for the root directory.
 * To assign vehicles to the paths listed in this {@link Assignment}, see {@link Assignment#readFromFile(avdta.project.DTAProject, java.util.List, avdta.network.PathList)}.
 * This requires that vehicles to be sorted by departure times then id, which they normally are.
 * @author Michael
 */
public class Assignment implements Comparable<Assignment>
{
    private DTAResults results;
    private int time;
    
    private String name;
    private String directory;
 
    /**
     * Constructs this {@link Assignment} from the specified directory and reads the properties ({@link Assignment#readAssignment(Scanner)}).
     * @param dir the directory
     * @throws IOException if a file cannot be accessed
     */
    public Assignment(File dir) throws IOException
    {
        directory = dir.getCanonicalPath();
        
        
        Scanner filein = new Scanner(getPropertiesFile());
        
        readAssignment(filein);
        
        filein.close();

    }
    
    /**
     * Returns the time this assignment was created
     * @return Returns the time this assignment was created (s)
     */
    public int getTime()
    {
        return time;
    }
    
    /**
     * Returns the paths file
     * @return {@link Assignment#getAssignmentDirectory()}/paths.dat
     */
    public File getPathsFile()
    {
        return new File(getAssignmentDirectory()+"/paths.dat");
    }
    
    /**
     * Returns the assignment directory.
     * @return the assignment directory as a {@link String}
     */
    public String getAssignmentDirectory()
    {
        return directory;
    }
    
    /**
     * Returns the assignment folder.
     * @return a {@link File} object that refers to the assignment folder.
     */
    public File getAssignmentFolder()
    {
        return new File(getAssignmentDirectory());
    }
    
    /**
     * Returns the file containing the demand used in this {@link Assignment}
     * @return the demand file
     */
    public File getDemandFile()
    {
        return new File(getAssignmentDirectory()+"/demand.txt");
    }
    
    /**
     * Returns the file containing the log of the DTA run creating this {@link Assignment}
     * @return the log file
     */
    public File getLogFile()
    {
        return new File(getAssignmentDirectory()+"/log.txt");
    }
    
    /**
     * Returns the file containing the properties of this {@link Assignment}
     * @return the properties file
     */
    public File getPropertiesFile()
    {
        return new File(getAssignmentDirectory()+"/properties.dat");
    }
    
    /**
     * Returns the name. This is by default the date of creation.
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns the name. This is by default the date of creation.
     * @return the name
     */
    public String toString()
    {
        return getName();
    }
    
    /**
     * Compares two assignments according to when they were created
     * @param rhs the assignment to be compared against
     * @return orders by creation time, descending
     */
    public int compareTo(Assignment rhs)
    {
        return rhs.time - time;
    }
    
    /**
     * Reads the assignment name and results
     * @param filein the input source
     */
    public void readAssignment(Scanner filein)
    {
        name = filein.nextLine();
        
        double mintt = filein.nextDouble();
        double tstt = filein.nextDouble();
        int num_veh = filein.nextInt();
        int exiting = filein.nextInt();
        results = new DTAResults(mintt*3600.0, tstt*3600.0, num_veh, exiting);
    }
    
    
    /**
     * Constructs a new {@link Assignment} for the given project and results.
     * The name is set to the current date.
     * The folder name is set to an unique integer.
     * @param project the project
     * @param results the results
     */
    public Assignment(DTAProject project, DTAResults results)
    {
        this(project, results, Calendar.getInstance().getTime().toString());
    }
    
    /**
     * Constructs a new {@link Assignment} for the given project and results, with the specified name.
     * The folder name is set to an unique integer.
     * @param project the project
     * @param results the results
     * @param name the name
     */
    public Assignment(DTAProject project, DTAResults results, String name)
    {
        this(project, results, ""+(int)(System.nanoTime()/1.0e9), name);
    }
    
    /**
     * Constructs a new {@link Assignment} for the given project and results, with the specified name.
     * @param project the project
     * @param results the results
     * @param name the name
     * @param folderName the directory name
     */
    public Assignment(DTAProject project, DTAResults results, String folderName, String name)
    {
        this.results = results;
        this.name = name;
        
        directory = project.getAssignmentsFolder()+"/"+folderName;
        
        File file = new File(directory);
        file.mkdirs();
        
        time = (int)(System.currentTimeMillis()/1000.0);
    }
    
    /**
     * Updates the results of this assignment
     * @param results the new results
     */
    public void setResults(DTAResults results)
    {
        this.results = results;
    }
    
    /**
     * Returns the results associated with simulating this assignment
     * @return the results
     */
    public DTAResults getResults()
    {
        return results;
    }
    
    /**
     * Returns the file containing the vehicles
     * @return the file containing the vehicles
     */
    public File getVehiclesFile()
    {
        return new File(getAssignmentDirectory()+"/vehicles.dat");
    }
    
    /**
     * Returns the file containing the link travel times
     * @return the file containing the link travel times
     */
    public File getTimesFile()
    {
        return new File(getAssignmentDirectory()+"/linktt.dat");
    }
    
    /**
     * Returns the sim.vat file
     * @return the sim.vat file
     */
    public File getSimVatFile()
    {
        return new File(getAssignmentDirectory()+"/sim.vat");
    }
    
    /**
     * Saves the assignment to the file.
     * @param vehicles the list of vehicles to be saved
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
    public void writeToFile(List<Vehicle> vehicles, DTAProject project) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(getPropertiesFile()), true);
        fileout.println(getName());
        fileout.println(getResultsData());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getVehiclesFile()), true);
        
        for(Vehicle v : vehicles)
        {
            if(!v.isTransit())
            {
                fileout.println(v.getId()+"\t"+v.getPath().getId());
            }
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
    
    /**
     * Returns the results data in {@link String} form to be written to file.
     * @return the results data in {@link String} form
     */
    public String getResultsData()
    {
        return (results.getMinTT()/3600.0)+"\t"+results.getTSTT()+"\t"+results.getTrips()+"\t"+results.getExiting();
    }
    
    /**
     * Reads the assignment from the file using the specified {@link PathList}. 
     * This creates mappings of ids to vehicles and ids to paths for faster lookup.
     * Vehicles are assigned to the path specified by the saved id.
     * @param project the project
     * @param vehicles the list of vehicles
     * @param pathlist the list of paths
     * @throws IOException if a file cannot be accessed
     */
    public void readFromFile(DTAProject project, List<Vehicle> vehicles, PathList pathlist) throws IOException
    {
        Map<Integer, Vehicle> vehMap = new HashMap<Integer, Vehicle>();
        
        for(Vehicle v : vehicles)
        {
            vehMap.put(v.getId(), v);
        }
        
        Map<Integer, Path> paths = pathlist.createPathIdsMap();
        
        Scanner filein = new Scanner(getVehiclesFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            
            Vehicle v = vehMap.get(id);
            
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
