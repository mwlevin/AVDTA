/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import avdta.network.node.PhasedTBR;

/**
 *
 * @author micha
 */
public class GUI extends JFrame
{
    private static JFrame frame;
    private static Image icon;
    
    
    public static void main(String[] args) throws IOException
    {
        //System.setErr(new PrintStream(new FileOutputStream(new File("error_log.txt")), true));
        
        new DTAGUI();
        
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
    
    
    
    public static void handleException(Exception ex)
    {
        JOptionPane.showMessageDialog(frame, ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
    
    public GUI()
    {
        setTitle("AVDTA");
        //setIconImage(GUI.getIcon());
        
        frame = this;
    }
}
