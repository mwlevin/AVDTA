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
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class ImportFromVISTA 
{
    public ImportFromVISTA(Project project, File nodes, File linkdetails, File elevation, File phases, File signals) throws IOException
    {
        convertNodes(project, nodes, elevation);
        convertLinks(project, linkdetails);
        convertPhases(project, phases);
        convertSignals(project, signals);
    }
    
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
            
            fileout.println(nodeid+"\t0\t"+phaseid+"\t"+"\t"+rest);
            
            
        }
        fileout.close();
        filein.close();
    }
    
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
                type = ReadNetwork.CENTROID;
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
