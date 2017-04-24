/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdesign;

import avdta.dta.Assignment;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class TBRIndividual extends Individual<TBRIndividual>
{
    private int[] controls;
    private int hash;
    
    public TBRIndividual(int num_inter, int fillType)
    {
        controls = new int[num_inter];
        
        for(int i = 0; i < controls.length; i++)
        {
            controls[i] = fillType;
        }
        computeHash();
    }
    
    public TBRIndividual(int[] controls)
    {
        this.controls = controls;
        computeHash();
    }
    
    public void setAssignment(Assignment assign)
    {
        super.setAssignment(assign);
        setObj(assign.getResults().getTSTT());
    }
    
    protected void computeHash()
    {
        hash = 0;
        
        for(int i = 0; i < controls.length; i++)
        {
            hash += (100*controls[i]) / (i+1);
        }
    }
    
    public TBRIndividual cross(TBRIndividual rhs)
    {
        int[] newControls = new int[controls.length];
        
        for(int i = 0; i < newControls.length; i++)
        {
            if(Math.random() < 0.5)
            {
                newControls[i] = controls[i];
            }
            else
            {
                newControls[i] = rhs.controls[i];
            }
        }
        
        
        return new TBRIndividual(newControls);
    }
    
    public boolean equals(TBRIndividual rhs)
    {
        if(rhs.controls.length != controls.length)
        {
            return false;
        }
        
        for(int i = 0; i < controls.length; i++)
        {
            if(controls[i] != rhs.controls[i])
            {
                return false;
            }
        }
        
        return true;
    }
    
    public int hashCode()
    {
        return hash;
    }
    
    public int getControl(int idx)
    {
        return controls[idx];
    }
    
    public int[] getControls()
    {
        return controls;
    }
    
    protected void writeToFile(PrintStream out)
    {
        super.writeToFile(out);
        
        for(int i : controls)
        {
            out.println(i);
        }
    }
    
    protected void readFromFile(Scanner in) throws IOException
    {
        super.readFromFile(in);
        
        for(int i = 0; i < controls.length; i++)
        {
            controls[i] = in.nextInt();
        }
        computeHash();
    }
}
