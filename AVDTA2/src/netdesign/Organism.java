/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import avdta.dta.Assignment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public abstract class Organism<T extends Organism> implements Comparable<Organism>
{
    private static int org_count = 0;
    
    public static int getNumOrganisms()
    {
        return org_count;
    }
    
    private Assignment assign;
    private double obj;
    
    public Organism()
    {
        org_count++;
    }
    
    // assume minimize
    public int compareTo(Organism o)
    {
        if(obj > o.obj)
        {
            return 1;
        }
        else if(obj < o.obj)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
    
    public void setAssignment(Assignment assign)
    {
        this.assign = assign;
    }
    
    public void setObj(double obj)
    {
        this.obj = obj;
    }
    
    public double getObj()
    {
        return obj;
    }
    
    public Assignment getAssignment()
    {
        return assign;
    }
    
    public void writeToFile(File file) throws IOException
    {
        PrintStream out = new PrintStream(new FileOutputStream(file), true);
        writeToFile(out);
        out.close();
    }
    
    protected void writeToFile(PrintStream out)
    {
        out.println(obj);
        out.println(assign.getAssignmentFolder());
    }
    
    public void readFromFile(File file) throws IOException
    {
        Scanner in = new Scanner(file);
        readFromFile(in);
        in.close();
    }
    
    public abstract Organism cross(T rhs);
    
    
    protected void readFromFile(Scanner in) throws IOException
    {
        obj = in.nextDouble();
        in.nextLine();
        assign = new Assignment(new File(in.nextLine().trim()));
    }
}
