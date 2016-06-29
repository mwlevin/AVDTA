/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 *
 * @author ml26893
 */
public class StatusBar extends JComponent implements StatusUpdate
{
    private int percent;
    private long eta;
    
    private long startTime;
    
    private static final int delay = 100;
    
    public StatusBar()
    {
        eta = -1;
        percent = 0;
        setPreferredSize(new Dimension(210, 45));
        
        
        Timer timer = new Timer(delay, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                eta = eta - (long)(delay * 1.0e6);
                repaint();
            }
        });
        timer.start();
    }
    
    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.green);
        g.fillRect(5, 5, (int)Math.round(percent /100.0 * (getWidth()-10)), 20);
        
        g.setColor(Color.black);
        g.drawRect(5, 5, getWidth()-10, 20);
        
        g.setFont(new Font("Arial", 0, 12));
        g.drawString(""+percent+"%", (getWidth()-10)/2, 20);
        
        if(eta > 0)
        {
            int rem = (int)Math.round(eta/1.0e9);
            g.drawString("Remaining: "+(rem/60)+" min "+(rem%60)+" sec", 10, 40);
        }
    }
    
    public void resetTime()
    {
        startTime = 0;
        eta = 0;
    }
    
    public boolean isFinished()
    {
        return percent == 100;
    }
    
    public void update(double p)
    {
        long time = System.nanoTime();
        
        if(startTime > 0)
        {
            eta = (long)Math.round( (time - startTime) / p * (1-p));
        }
        else
        {
            startTime = time;
        }
        
        percent = (int)Math.round(p*100);

    }
}
