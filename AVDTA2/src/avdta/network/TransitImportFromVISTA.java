/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import avdta.project.TransitProject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * This class defines methods to convert transit data from the VISTA data format to the AVDTA data format. 
 * This class uses the following tables from VISTA: bus, bus_period, bus_route_link, bus_frequency
 * To use it, copy the required tables into files, and construct a new {@link TransitImportFromVISTA} with the files. 
 * The constructor will call all conversion methods.
 * @author Michael
 */
public class TransitImportFromVISTA 
{
    /**
     * Converts the transit data from VISTA from the following files.
     * @param project the {@link TransitProject}
     * @param bus the file containing the bus table
     * @param bus_period the file containing the bus_period table
     * @param bus_route_link the file containing the bus_route_link table
     * @param bus_frequency the file containing the bus_frequency table
     * @throws IOException if a file cannot be accessed
     */
    public TransitImportFromVISTA(TransitProject project, File bus, File bus_period, File bus_route_link, File bus_frequency) throws IOException
    {
        convertBus(project, bus);
        convertBusPeriod(project, bus_period);
        convertBusRouteLink(project, bus_route_link);
        convertBusFrequency(project, bus_frequency);
    }
    
    /**
     * Converts the bus table.
     * @param project the {@link TransitProject}
     * @param input the file containing the database table
     * @throws IOException if a file cannot be accessed
     */
    public void convertBus(TransitProject project, File input) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getBusFile()), true);
        Scanner filein = new Scanner(input);
        
        fileout.println(ReadNetwork.getBusFileHeader());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int route = filein.nextInt();
            int starttime = filein.nextInt();
            int preemption = filein.nextInt();
            
            int newtype = ReadNetwork.BUS + ReadNetwork.AV + ReadNetwork.ICV;
            
            fileout.println(id+"\t"+newtype+"\t"+route+"\t"+starttime);
        }
        fileout.close();
        filein.close();
    }
    
    /**
     * Converts the bus_period table.
     * @param project the {@link TransitProject}
     * @param input the file containing the database table
     * @throws IOException if a file cannot be accessed
     */
    public void convertBusPeriod(TransitProject project, File input) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getBusPeriodFile()), true);
        Scanner filein = new Scanner(input);
        
        fileout.println(ReadNetwork.getBusPeriodFileHeader());
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int starttime = filein.nextInt();
            int endtime = filein.nextInt();
            
            fileout.println(id+"\t"+starttime+"\t"+endtime);
        }
        fileout.close();
        filein.close();
    }
    
    /**
     * Converts the bus_frequency table.
     * @param project the {@link TransitProject}
     * @param input the file containing the database table
     * @throws IOException if a file cannot be accessed
     */
    public void convertBusFrequency(TransitProject project, File input) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getBusFrequencyFile()), true);
        Scanner filein = new Scanner(input);
        
        fileout.println(ReadNetwork.getBusFrequencyFileHeader());
        
        while(filein.hasNextInt())
        {
            int route = filein.nextInt();
            int period = filein.nextInt();
            int frequency = filein.nextInt();
            int offset = filein.nextInt();
            int preemption = filein.nextInt();
            filein.nextLine();
            
            fileout.println(route+"\t"+period+"\t"+frequency+"\t"+offset);
        }
        fileout.close();
        filein.close();
    }
    
    /**
     * Converts the bus_route_link table.
     * @param project the {@link TransitProject}
     * @param input the file containing the database table
     * @throws IOException if a file cannot be accessed
     */
    public void convertBusRouteLink(TransitProject project, File input) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getBusRouteLinkFile()), true);
        Scanner filein = new Scanner(input);
        
        fileout.println(ReadNetwork.getBusRouteLinkFileHeader());
        
        while(filein.hasNextInt())
        {
            int route = filein.nextInt();
            int sequence = filein.nextInt();
            int link = filein.nextInt();
            
            boolean stop = false;
            
            if(filein.hasNextInt())
            {
                stop = true;
            }
            else
            {
                filein.next();
            }

            int dwell = filein.nextInt();
            
            
            filein.nextLine();
            
            fileout.println(route+"\t"+sequence+"\t"+link+"\t"+(stop?1:0)+"\t"+dwell);
        }
        fileout.close();
        filein.close();
    }
}
