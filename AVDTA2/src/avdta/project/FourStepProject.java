/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.project;

import avdta.demand.ReadDemandNetwork;
import fourstep.ReadFourStepNetwork;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 *
 * @author micha
 */
public class FourStepProject extends DTAProject
{
    public FourStepProject()
    {
        
    }
    
    public FourStepProject(File dir) throws IOException
    {
        super(dir);
    }
    
    /**
     * Creates project subfolders inside the specified directory
     * @param dir the project directory
     * @throws IOException if a file cannot be created
     */
    public void createProjectFolders(File dir) throws IOException
    {
        super.createProjectFolders(dir);
        
        String dirStr = dir.getCanonicalPath();
        
     
        
        File file = new File(dirStr+"/fourstep");
        file.mkdirs();
        
        
    }/**
     * Writes empty FourStep files. Also calls {@link DTAProject#writeEmptyFiles()}
     * @throws IOException if a file cannot be created
     */
    public void writeEmptyFiles() throws IOException
    {
        super.writeEmptyFiles();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(getProductionsFile()), true);
        fileout.println(ReadFourStepNetwork.getProductionsFileHeader());
        fileout.close();
        
        fileout = new PrintStream(new FileOutputStream(getAttractionsFile()), true);
        fileout.println(ReadFourStepNetwork.getAttractionsFileHeader());
        fileout.close();
    }
    
    /**
     * Returns the productions file
     * @return {@link Project#getProjectDirectory()}/fourstep/productions.txt
     */
    public File getProductionsFile()
    {
        return new File(getProjectDirectory()+"/fourstep/productions.txt");
    }
    
    /**
     * Returns the attractions file
     * @return {@link Project#getProjectDirectory()}/fourstep/attractions.txt
     */
    public File getAttractionsFile()
    {
        return new File(getProjectDirectory()+"/fourstep/attractions.txt");
    }
    
}
