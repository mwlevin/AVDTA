/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.util;

import avdta.gui.editor.EditSignal;
import avdta.gui.editor.Editor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * This is a special {@link JButton} that allows the user to choose colors.
 * It displays a small rectangle indicating the selected color.
 * When clicked, it opens a color chooser dialog.
 * To interact with this {@link JColorButton}, see {@link JColorButton#getColor()} and {@link JColorButton#setColor(java.awt.Color)}.
 * @author Michael
 */
public class JColorButton extends JButton
{
    private Color color;

    /**
     * Constructs this {@link JColorButton} with an initial color of {@link Color#BLACK}.
     * This calls {@link JColorButton#JColorButton(java.awt.Color)}.
     */
    public JColorButton()
    {
        this(Color.black);
    }
    
    /**
     * Constructs this {@link JColorButton} with the specified initial color.
     * @param c the initial color
     */
    public JColorButton(Color c)
    {
        setIcon(new ColorIcon());
        
        addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                chooseColor();
            }
        });
        
        setColor(c);
    }
    
    private void chooseColor()
    {
        Color output = JColorChooser.showDialog(this, "Choose color", color);
        
        if(output != null)
        {
            setColor(output);
        }
    }
    
    /**
     * Updates the chosen color.
     * @param c the new chosen color
     */
    public void setColor(Color c)
    {
        this.color = c;
        repaint();
    }
    
    /**
     * Returns the chosen color.
     * @return the chosen color
     */
    public Color getColor()
    {
        return color;
    }
    
    private class ColorIcon implements Icon
    {
        public int getIconHeight()
        {
            return 15;
        }
        public int getIconWidth()
        {
            return 15;
        }
        
        public void paintIcon(Component c, Graphics g, int x, int y)
        {
            if(color != null)
            {
                g.setColor(color);
            }
            else
            {
                setColor(getBackground());
            }
            g.fillRect(x, y, getIconWidth(), getIconHeight());
        }
    }
}
