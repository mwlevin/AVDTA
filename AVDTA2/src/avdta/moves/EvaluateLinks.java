/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.moves;

import avdta.dta.DTASimulator;
import avdta.network.Simulator;
import avdta.network.link.CTMLink;
import avdta.network.link.Link;
import avdta.network.Path;
import avdta.project.DTAProject;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 *
 * @author ml26893
 */
public class EvaluateLinks 
{
    public static final int START_TIME = 0;
    public static final int END_TIME = 7200;
    
    private Map<Link, LinkData> data;
    
    private String name, scenario;
    
    public EvaluateLinks()
    {
        name = "";
        scenario = "";
    }
    
    public void calculate(DTAProject project) throws IOException
    {
        DTASimulator sim = (DTASimulator)project.getSimulator();
        
        File file = new File(sim.getProject().getResultsFolder()+"/moves/");
        file.mkdirs();
        
        data = new TreeMap<Link, LinkData>();
        
        for(Link l : sim.getLinks())
        {
            if(!l.isCentroidConnector())
            {
                data.put(l, new LinkData());
            }
        }
        
        Scanner filein = new Scanner(new File(project.getAssignmentsFolder()+"/"+sim.getAssignment().getName()+"/sim.vat"));
        
        for(Vehicle x : sim.getVehicles())
        {
            
            int type = filein.nextInt();
            int id = filein.nextInt();
            filein.nextDouble();
            filein.nextDouble();
            
            int size = filein.nextInt();
            
            int[] arr_times = new int[size];
            
            for(int i = 0; i < size; i++)
            {
                filein.nextInt();
                arr_times[i] = (int)filein.nextDouble();
            }
            
            PersonalVehicle v = (PersonalVehicle)x;
            
            if(id != v.getId())
            {
                throw new RuntimeException("Vehicles out of order");
            }
            
            
            
            Path p = v.getPath();
            
            
            
            for(int i = 1; i < p.size()-1; i++)
            {
                int enter = arr_times[i];
                int exit = arr_times[i+1];
                
                Link l = p.get(i);
                
                data.get(l).addVehicle(l, enter, exit);
            }
        }
    }
    
    public void parseCellEnter(Simulator sim) throws IOException
    {
        parseCellEnter(sim, new File("results/"+name+"/"+scenario+"/cell_enter.txt"));
    }
    
    public void parseCellEnter(Simulator sim, File input) throws IOException
    {
        for(Link l : sim.getLinks())
        {
            if(l.isCentroidConnector())
            {
                continue;
            }
            
            Scanner filein = new Scanner(input);
        
            List<CellRecord> records = new ArrayList<CellRecord>();

            filein.nextLine();
            
            while(filein.hasNextInt())
            {
                int vehid = filein.nextInt();
                int linkid = filein.nextInt();
                int enter = filein.nextInt();
                int exit = filein.nextInt();
                
                
                if(linkid == l.getId())
                {
                    CellRecord r = new CellRecord(vehid, enter, exit);
                    records.add(r);
                }
                
            }
            
            filein.close();

            Collections.sort(records);
            
            File dir = new File("results/"+name+"/"+scenario+"/moves/links");
            dir.mkdirs();
            PrintStream fileout = new PrintStream(new FileOutputStream(new File("results/"+name+"/"+scenario+"/moves/links/"+l.getId()+".txt")), true);
            
            fileout.println("Vehicle ID\tTime (sec)\tSpeed (mph)\tGrade (percent)");
            
  
            for(CellRecord r : records)
            {
                double speed = ((CTMLink)l).getCellLength() / ((r.getExit() - r.getEnter())/3600.0);
                
                for(int t = r.getEnter(); t < r.getExit(); t++)
                {
                    fileout.println(r.getVehId()+"\t"+t+"\t"+speed+"\t"+(l.getAvgGrade()*100));
                }
            }
            
            fileout.close();
        }
        
    }
    
    public void printLinkSource() throws IOException
    {
        printLinkSource(new File("results/"+name+"/"+scenario+"/moves/linksource.txt"));
    }
    
    public void printLinkSource(File output) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(output), true);
        
        fileout.println("linkID\tsourceTypeID\tsourceTypeHourFraction");
        
        for(Link l : data.keySet())
        {
            fileout.println(l.getId()+"\t21\t1");
        }
        
        fileout.close();
    }
    
    public void printLink() throws IOException
    {
        printLink(new File("results/"+name+"/"+scenario+"/moves/link.txt"));
    }
    
    public void printLink(File output) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(output), true);
        
        fileout.println("linkID\tcountyID\tzoneID\troadTypeID\tlinkLength\tlinkVolume\tlinkAvgSpeed\tlinkDescription\tlinkAvgGrade");
        
        for(Link l : data.keySet())
        {
            LinkData d = data.get(l);
            fileout.println(l.getId()+"\t"+getCountyId(l)+"\t"+getZoneId(l)+"\t"+
                    estimateRoadType(l)+"\t"+l.getLength()+"\t"+
                    d.getVolume()+"\t"+d.getAvgSpeed()+"\t\t"+
                    l.getAvgGrade()*100);
        }
        
        fileout.close();
    }
    
    public int estimateRoadType(Link l)
    {
        if(l.getFFSpeed() >= 55)
        {
            return 4;
        }
        else
        {
            return 5;
        }
    }
    
    public int getCountyId(Link l)
    {
        return 48453;
    }
    
    public int getZoneId(Link l)
    {
        return 484530;
    }
}
