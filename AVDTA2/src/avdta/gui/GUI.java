/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


/**
 *
 * @author micha
 */
public class GUI 
{
    
    private static Image icon;
    
    
    public static void main(String[] args) throws IOException
    {
        System.setErr(new PrintStream(new FileOutputStream(new File("error_log.txt")), true));
    }
    
    
    public static Image getIcon()
    {
        if(icon == null)
        {
            try
            {
                icon = ImageIO.read(new File("icon.png"));
            }
            catch(IOException ex){}
        }
        
        return icon;
    }
    
    public static String getTitle()
    {
        return "AVDTA";
    }
    
    public static void handleException(JFrame frame, Exception ex)
    {
        JOptionPane.showMessageDialog(frame, ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}
