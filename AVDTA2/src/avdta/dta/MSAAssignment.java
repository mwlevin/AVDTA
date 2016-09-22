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
 * This represents an assignment using the method of successive averages, and also stores the iteration number.
 * This can be used to continue an assignment from where it left off.
 * @author Michael
 */
public class MSAAssignment extends Assignment
{
    private int iter;
    
    /**
     * Constructs the assignment from the specified directory.
     * @param input the input file
     * @throws IOException if a file cannot be accessed
     */
    public MSAAssignment(File input) throws IOException
    {
        super(input);
    }
    
    /**
     * Constructs the assignment for the specified project and results with the last iteration number.
     * The name is set to the current date
     * The folder name is set to an unique integer.
     * @param project the project
     * @param results the results from simulating this assignment
     * @param iter the last iteration
     */
    public MSAAssignment(DTAProject project, DTAResults results, int iter)
    {
        super(project, results);
        this.iter = iter;
    }
    
    /**
     * Constructs the assignment for the specified project and results with the last iteration number with the specified name.
     * The folder name is set to an unique integer.
     * @param project the project
     * @param results the results from simulating this assignment
     * @param iter the last iteration
     * @param name the assignment name
     */
    public MSAAssignment(DTAProject project, DTAResults results, String name, int iter)
    {
        super(project, results, name);
        this.iter = iter;
    }
    
    /**
     * Saves the assignment to the file.
     * @param vehicles the list of vehicles to be saved
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
    public void writeToFile(List<Vehicle> vehicles, DTAProject project) throws IOException
    {
        super.writeToFile(vehicles, project);
        
        
        PrintStream fileout = new PrintStream(new FileOutputStream(getIndicatorFile()), true);
        fileout.close();
    }
    
    /**
     * This is an empty file used to check the type of assignment.
     * @return {@code msa.dat}
     */
    public File getIndicatorFile()
    {
        return new File(getAssignmentDirectory()+"/msa.dat");
    }
    
    /**
     * The last iteration of MSA for this assignment
     * @return last iteration of MSA for this assignment
     */
    public int getIter()
    {
        return iter;
    }
    
    /**
     * Updates the last iteration of MSA for this assignment
     * @param iter the new last iteration of MSA for this assignment
     */
    public void setIter(int iter)
    {
        this.iter = iter;
    }
    
    /**
     * Reads the assignment name and results
     * @param filein the input source
     */
    public void readAssignment(Scanner filein)
    {
        super.readAssignment(filein);
        
        iter = filein.nextInt();
    }
    
    /**
     * Returns the results data in {@link String} form to be written to file.
     * @return the results data in {@link String} form
     */
    public String getResultsData()
    {
        return super.getResultsData()+"\t"+iter;
    }
}
