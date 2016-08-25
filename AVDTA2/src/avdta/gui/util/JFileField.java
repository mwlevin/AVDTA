/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.util;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author micha
 */
public class JFileField extends JTextField
{
    private File file;
    private FileFilter filter;
    private String root;
    
    public JFileField(int len, FileFilter filter, String root)
    {
        super(len);
        
        this.root = root;
        this.filter = filter;
        setEditable(false);
        
        
        
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e) 
            {
                if(isEnabled())
                {
                    File file = chooseFile();

                    if(file != null)
                    {
                        setFile(file);
                        valueChanged(file);
                    }
                }
            }
        });
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
    
    public void setEnabled(boolean e)
    {
        super.setEnabled(e);
    }
    
    public File chooseFile()
    {
        JFileChooser chooser;
        if(file == null)
        {
            chooser = new JFileChooser(new File(root));
        }
        else
        {
            chooser = new JFileChooser(file);
        }
        
        if(filter != null)
        {
            chooser.addChoosableFileFilter(filter);
        }
        
        int returnVal = chooser.showOpenDialog(this);
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            return chooser.getSelectedFile();
        }
        else
        {
            return null;
        }
    }
    
    public void setFile(File f)
    {
        file = f;
        
        if(f != null)
        {
            setText(f.getName());
        }
        else
        {
            setText("");
        }
    }
    
    public File getFile()
    {
        return file;
    }
    
    public void valueChanged(File f)
    {
        
    }
}
