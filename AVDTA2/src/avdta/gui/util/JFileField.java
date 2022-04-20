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
 * This is a customized uneditable {@link JTextField} that allows the user to select files.
 * It displays the file name, and is used when the user needs to select multiple files.
 * When the user clicks on this {@link JFileField}, it will open a {@link JFileChooser} to edit the selected file.
 * To indicate the ability to select files, the hand cursor is used when hovering over this {@link FileFilter} while enabled.
 * @author Michael
 */
public class JFileField extends JTextField
{
    private File file;
    private FileFilter filter;
    private String root;
    
    /**
     * Constructs this {@link FileFilter} with the specified {@link FileFilter}, root directory, and display length.
     * @param len the display length (number of characters). This is used in the constructor call {@link JTextField#JTextField(int)}.
     * @param filter the {@link FileFilter} for choosing files
     * @param root the root directory for choosing files
     */
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
    
    /**
     * Updates the selected file.
     * @param f the new selected file.
     */
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
    
    /**
     * Returns the selected file.
     * @return the selected file
     */
    public File getFile()
    {
        return file;
    }
    
    /**
     * This is called whenever the selected file changes.
     * It may be overwritten by other classes to provide additional behavior.
     * @param f the new selected file
     */
    public void valueChanged(File f)
    {
        
    }
}
