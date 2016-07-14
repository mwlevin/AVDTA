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

/**
 *
 * @author micha
 */
public class FileTransfer 
{
    private static final int BUFFER_SIZE = 1024;
    
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
