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
 *
 * @author ml26893
 */
public class JColorButton extends JButton
{
    private Color color;

    public JColorButton()
    {
        setIcon(new ColorIcon());
        
        addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                chooseColor();
            }
        });
        
        setColor(Color.black);
    }
    
    public void chooseColor()
    {
        Color output = JColorChooser.showDialog(this, "Choose color", color);
        
        if(output != null)
        {
            setColor(output);
        }
    }
    
    public void setColor(Color c)
    {
        this.color = c;
        repaint();
    }
    
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
