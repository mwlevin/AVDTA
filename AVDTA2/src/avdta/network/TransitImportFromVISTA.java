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
 *
 * @author Michael
 */
public class TransitImportFromVISTA 
{
    public TransitImportFromVISTA(TransitProject project, File bus, File bus_period, File bus_route_link, File bus_frequency) throws IOException
    {
        convertBus(project, bus);
        convertBusPeriod(project, bus_period);
        convertBusRouteLink(project, bus_route_link);
        convertBusFrequency(project, bus_frequency);
    }
    
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
            
            fileout.println(route+"\t"+period+"\t"+frequency+"\t"+offset);
        }
        fileout.close();
        filein.close();
    }
    
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
            
            int next = filein.nextInt();
            
            boolean stop = false;
            int dwell = 0;
            
            if(next > 0)
            {
                stop = true;
                dwell = filein.nextInt();
            }
            
            filein.nextLine();
            
            fileout.println(route+"\t"+sequence+"\t"+link+"\t"+(stop?1:0)+"\t"+dwell);
        }
        fileout.close();
        filein.close();
    }
}
