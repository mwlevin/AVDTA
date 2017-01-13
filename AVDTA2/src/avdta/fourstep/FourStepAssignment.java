/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.fourstep;

import avdta.dta.DTAResults;
import avdta.dta.MSAAssignment;
import avdta.project.FourStepProject;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author ml26893
 */
public class FourStepAssignment extends MSAAssignment
{
    private int fs_iter;
    
    public FourStepAssignment(File input) throws IOException
    {
        super(input);
    }
    
    public FourStepAssignment(FourStepProject project, DTAResults results, int fs_iter, int ta_iter)
    {
        super(project, results, ta_iter);
        
        this.fs_iter = fs_iter;
    }
    
    public FourStepAssignment(FourStepProject project, DTAResults results, String name, int fs_iter, int ta_iter)
    {
        super(project, results, name, ta_iter);
        
        this.fs_iter = fs_iter;
    }
    
    /**
     * This is an empty file used to check the type of assignment.
     * @return {@code msa.dat}
     */
    public File getIndicatorFile()
    {
        return new File(getAssignmentDirectory()+"/fourstep.dat");
    }
    
    /**
     * The last iteration of four-step for this assignment
     * @return last iteration of four-step for this assignment
     */
    public int getFourStepIter()
    {
        return fs_iter;
    }
    
    /**
     * Updates the last iteration of four-step for this assignment
     * @param iter the new last iteration of four-step for this assignment
     */
    public void setFourStepIter(int iter)
    {
        this.fs_iter = iter;
    }
    
    public File getStaticODFile()
    {
        return new File(getAssignmentDirectory()+"/static_od.txt");
    }
    
    public File getDynamicODFile()
    {
        return new File(getAssignmentDirectory()+"/dynamic_od.txt");
    }
    
    public File getDemandProfileFile()
    {
        return new File(getAssignmentDirectory()+"/demand_profile.txt");
    }
    
    public File getZonesFile()
    {
        return new File(getAssignmentDirectory()+"/zones.txt");
    }
}
