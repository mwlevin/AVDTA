/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.network.Path;
import avdta.network.PathList;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Relies on vehicles to be sorted by dtime then id
 * @author micha
 */
public class Assignment 
{
    private DTAResults results;
    
    public Assignment(File input) throws IOException
    {
        // read header data
        
        Scanner filein = new Scanner(input);
        
        double mintt = filein.nextDouble();
        double tstt = filein.nextDouble();
        int num_veh = filein.nextInt();
        int exiting = filein.nextInt();
        
        filein.close();
        
        results = new DTAResults(mintt, tstt, num_veh, exiting);
    }
    
    public Assignment(DTAResults results)
    {
        this.results = results;
    }
    
    
    public void writeToFile(List<Vehicle> vehicles, File file) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        fileout.println(results.getMinTT()+"\t"+results.getTSTT()+"\t"+results.getNumVeh()+"\t"+results.getNumExiting());
        
        for(Vehicle v : vehicles)
        {
            fileout.println(v.getId()+"\t"+v.getPath().getId());
        }
        
        fileout.close();
        
    }
    
    public void readFromFile(List<Vehicle> vehicles, PathList pathlist, File file) throws IOException
    {
        Map<Integer, Path> paths = pathlist.createPathIdsMap();
        
        Scanner filein = new Scanner(file);
        
        for(Vehicle v : vehicles)
        {
            int id = filein.nextInt();
            
            if(v.getId() != id)
            {
                throw new RuntimeException("Id mismatch when reading assignment");
            }
            
            Path p = paths.get(filein.nextInt());
            
            if(p == null)
            {
                throw new RuntimeException("Missing path when reading assignment");
            }
            
            v.setPath(p);
        }
    }
    
    
}
