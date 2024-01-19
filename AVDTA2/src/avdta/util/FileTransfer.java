/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import avdta.project.Project;

/**
 * This class provides a static method to make a copy of a file. 
 * It is used in {@link Project} when cloning files.
 * @author Michael
 */
public class FileTransfer 
{
    private static final int BUFFER_SIZE = 1024;
    
    /**
     * Creates a copy of the origin {@link File} at the destination {@link File}.
     * The destination {@link File} will be overwritten.
     * This uses a buffered byte-wise transfer, and does not depend on the file type.
     * @param origin the {@link File} to be copied
     * @param dest the {@link File} to be copied to. This {@link File} does not need to exist yet.
     * @throws IOException if a {@link File} cannot be accessed
     */
    public static void copy(File origin, File dest) throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(origin));
        
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));
        
        int len = 0;
        
        byte[] buffer = new byte[BUFFER_SIZE];
        
        while(true)
        {
            len = in.read(buffer);
            
            if(len > 0)
            {
                out.write(buffer, 0, len);
            }
            else
            {
                break;
            }
        }
        
        in.close();
        out.close();
    }
}
