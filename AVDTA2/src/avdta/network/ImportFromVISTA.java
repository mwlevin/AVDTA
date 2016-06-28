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
    public ImportFromVISTA(Project project, File nodes, File linkdetails, File elevation, File phases) throws IOException
    {
        convertNodes(project, nodes, elevation);
        convertLinks(project, linkdetails);
        convertPhases(project, phases);
    }
    
    public void convertPhases(Project project, File phases) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getPhasesFile()), true);
        Scanner filein = new Scanner(phases);
        
        fileout.println("nodeid\ttype\toffset\tphase_id\ttime_red\ttime_yellow\ttime_green\tnum_moves\tlink_from\tlink_to");
        
        while(filein.hasNextInt())
        {
            filein.next();
            int type = filein.nextInt();
            int nodeid = filein.nextInt();
            filein.next();
            int phaseid = filein.nextInt();
            String rest = filein.nextLine().trim();
            
            fileout.println(nodeid+"\t"+type+"\t0\t"+phaseid+"\t"+rest);
            
            
        }
        fileout.close();
        filein.close();
    }
    
    public void convertLinks(Project project, File linkdetails) throws IOException
    {
        Scanner filein = new Scanner(linkdetails);
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getLinksFile()), true);
        
        fileout.println("id\ttype\tsource\tdest\tlength (ft)\tffspd (mph)\tcapacity\tnumLanes");
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int source_id = filein.nextInt();
            int dest_id = filein.nextInt();
            double length = filein.nextDouble();
            double ffspd = filein.nextDouble() * 60;
            double capacity = filein.nextDouble();
            int numLanes = filein.nextInt();
            
            if(type == 100)
            {
                type = ReadNetwork.CENTROID;
            }
            else
            {
                type = ReadNetwork.CTM;
            }
            
            fileout.println(id+"\t"+type+"\t"+source_id+"\t"+dest_id+"\t"+length+"\t"+ffspd+"\t"+capacity+"\t"+numLanes);
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
        
        fileout.println("id\ttype\tlongitude\tlatitude\televation");
        
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
