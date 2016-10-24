/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import avdta.project.Project;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * This class defines methods to convert network data from the VISTA data format to the AVDTA data format. 
 * This class uses the following tables from VISTA: nodes, link_details, phases, signals, and links.
 * To use it, copy the required tables into files, and construct a new {@link ImportFromVISTA} with the files. 
 * The constructor will call all conversion methods.
 * @author Michael
 */
public class ImportFromVISTA 
{
    private Set<Integer> vista_centroids;
    
    /**
     * Calls {@link ImportFromVISTA#ImportFromVISTA(avdta.project.Project, java.io.File, java.io.File, java.io.File, java.io.File, java.io.File, java.io.File)} with a {@code nodes.txt} file, a {@code linkdetails.txt} file, a {@code elevation.txt} file, a {@code phases.txt} file, a {@code signals.txt} file, and a {@code links.txt} file from the specified directory.
     * @param project the project
     * @param directory the directory to look for files
     * @throws IOException if a file cannot be accessed
     */
    public ImportFromVISTA(Project project, String directory) throws IOException
    {
        this(project, new File(directory+"/nodes.txt"), new File(directory+"/linkdetails.txt"), new File(directory+"/elevation.txt"),
                new File(directory+"/phases.txt"), new File(directory+"/signals.txt"), new File(directory+"/links.txt"));
    }
    
    /**
     * Converts VISTA network data into the AVDTA data format from the following files.
     * @param project the {@link Project}
     * @param nodes the file containing the nodes table
     * @param linkdetails the file containing the link_details table
     * @param elevation the file containing elevation data. This is optional.
     * @param phases the file containing the phases table
     * @param signals the file containing the signals table
     * @param linkpoints the file containing the links table
     * @throws IOException if a file cannot be accessed
     */
    public ImportFromVISTA(Project project, File nodes, File linkdetails, File elevation, File phases, File signals, File linkpoints) throws IOException
    {
        vista_centroids = new HashSet<Integer>();
        convertNodes(project, nodes, elevation);
        convertLinks(project, linkdetails);
        convertPhases(project, phases);
        convertSignals(project, signals);
        convertLinkPoints(project, linkpoints);
        
    }
    
    /**
     * Converts the links table.
     * @param project the {@link Project}
     * @param input the file containing the database table
     * @throws IOException if a file is not found
     */
    public void convertLinkPoints(Project project, File input) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getLinkPointsFile()), true);
        
        
        
        fileout.println(ReadNetwork.getLinkPointsFileHeader());
        
        if(input != null)
        {
            Scanner filein = new Scanner(input);

            while(filein.hasNextInt())
            {
                int id = filein.nextInt();
                String points = filein.nextLine().trim();
                fileout.println(id+"\t"+points);
            }
            filein.close();
        }
        fileout.close();
    }
    
    /**
     * Converts the signals table.
     * @param project the {@link Project}
     * @param signals the file containing the database table
     * @throws IOException if a file is not found
     */
    public void convertSignals(Project project, File signals) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getSignalsFile()), true);
        
        Scanner filein = new Scanner(signals);
        
        fileout.println(ReadNetwork.getSignalsFileHeader());
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            filein.next();
            double offset = filein.nextDouble();
            
            fileout.println(id+"\t"+offset);
        }
        
        fileout.close();
    }
    
    /**
     * Converts the phases table.
     * @param project the {@link Project}
     * @param phases the file containing the database table
     * @throws IOException if a file is not found
     */
    public void convertPhases(Project project, File phases) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getPhasesFile()), true);
        Scanner filein = new Scanner(phases);
        
        fileout.println(ReadNetwork.getPhasesFileHeader());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int nodeid = filein.nextInt();
            int linkid = filein.nextInt();
            int phaseid = filein.nextInt();
            String rest = filein.nextLine().trim();
            
            fileout.println(nodeid+"\t1\t"+phaseid+"\t"+"\t"+rest);
            
            
        }
        fileout.close();
        filein.close();
    }
    
    /**
     * Converts the linkdetails table.
     * @param project the {@link Project}
     * @param linkdetails the file containing the database table
     * @throws IOException if a file is not found
     */
    public void convertLinks(Project project, File linkdetails) throws IOException
    {
        Scanner filein = new Scanner(linkdetails);
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getLinksFile()), true);
        
        fileout.println(ReadNetwork.getLinksFileHeader());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int source_id = filein.nextInt();
            int dest_id = filein.nextInt();
            double length = filein.nextDouble();
            double ffspd = filein.nextDouble() * 60;
            double capacity = filein.nextDouble();
            double wavespd = ffspd*0.5;
            int numLanes = filein.nextInt();
            
            if(type == 100)
            {
                if(vista_centroids.contains(source_id))
                {
                    type = ReadNetwork.CENTROID+1;
                }
                else if(vista_centroids.contains(dest_id))
                {
                    type = ReadNetwork.CENTROID+2;
                }
                else
                {
                     type = ReadNetwork.CENTROID;
                }
            }
            else
            {
                type = ReadNetwork.CTM;
            }
            
            fileout.println(id+"\t"+type+"\t"+source_id+"\t"+dest_id+"\t"+length+"\t"+ffspd+"\t"+wavespd+"\t"+capacity+"\t"+numLanes);
        }
        
        filein.close();
        fileout.close();
    }
    
    /**
     * Converts the nodes table. 
     * Also adds elevation if the elevation file exists. 
     * If the elevation file is null, it will be ignored.
     * @param project the {@link Project}
     * @param nodes the file containing the database table
     * @param elevation the file containing elevation data. This file is optional.
     * @throws IOException if a file is not found
     */
    public void convertNodes(Project project, File nodes, File elevation) throws IOException
    {
        // temporary map
        Map<Integer, Double> emap = new HashMap<Integer, Double>();
        
        Scanner filein;
        
        if(elevation != null && elevation.exists())
        {
            filein = new Scanner(elevation);

            while(filein.hasNextInt())
            {
                emap.put(filein.nextInt(), filein.nextDouble());
            }
            
            filein.close();
        }
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getNodesFile()), true);
        
        filein = new Scanner(nodes);
        
        fileout.println(ReadNetwork.getNodesFileHeader());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            double lng = filein.nextDouble();
            double lat = filein.nextDouble();
            double elev = 0;
            if(emap.containsKey(id))
            {
                elev = emap.get(id);
            }
            
            if(type == 100)
            {
                fileout.println(id+"\t"+ReadNetwork.CENTROID+"\t"+lng+"\t"+lat+"\t"+elev);
                vista_centroids.add(id);
            }
            else
            {
                fileout.println(id+"\t"+ReadNetwork.SIGNAL+"\t"+lng+"\t"+lat+"\t"+elev);
            }
        }
        filein.close();
        fileout.close();
    }
}
