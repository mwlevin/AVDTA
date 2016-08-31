/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.project.DTAProject;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class MSAAssignment extends Assignment
{
    private int iter;
    
    public MSAAssignment(File input) throws IOException
    {
        super(input);
    }
    
    public MSAAssignment(DTAProject project, DTAResults results, int iter)
    {
        super(project, results);
        this.iter = iter;
    }
    
    public MSAAssignment(DTAProject project, DTAResults results, String name, int iter)
    {
        super(project, results, name);
        this.iter = iter;
    }
    
    
    public void writeToFile(List<Vehicle> vehicles, DTAProject project) throws IOException
    {
        super.writeToFile(vehicles, project);
        
        String dir = project.getAssignmentsFolder()+"/"+getName();
        
        File indicator = new File(dir+"/msa.dat");
        
        PrintStream fileout = new PrintStream(new FileOutputStream(indicator), true);
        fileout.close();
    }
    
    public int getIter()
    {
        return iter;
    }
    
    public void setIter(int iter)
    {
        this.iter = iter;
    }
    public void readAssignment(Scanner filein)
    {
        super.readAssignment(filein);
        
        iter = filein.nextInt();
    }
    
    public String getResultsData()
    {
        return super.getResultsData()+"\t"+iter;
    }
}
