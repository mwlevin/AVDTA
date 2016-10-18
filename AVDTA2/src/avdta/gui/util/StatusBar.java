/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * This provides a graphical implementation of {@link StatusUpdate}. 
 * Updates are displayed in a small component that may be added to GUIs.
 * A progress bar with estimated percent completed is included.
 * Also, this class estimates the remaining time to completion based on when {@link StatusUpdate#update(double, double, java.lang.String)} was called.
 * @author Michael
 */
public class StatusBar extends JComponent implements StatusUpdate
{
    private double update, interval;
    
    private long eta;
    
    private long startTime;
    
    private String text;
    
    private static final int delay = 100;
    
    /**
     * Constructs this {@link StatusBar}.
     */
    public StatusBar()
    {
        eta = -1;
        update = 0.0;
        interval = 0.0;
        setPreferredSize(new Dimension(210, 65));
        text = "";
        
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
    
    /**
     * Paints this {@link StatusBar} on the specified {@link Graphics}.
     * {@link StatusBar#repaint()} is automatically called every 100ms.
     * @param g the {@link Graphics} to be painted on
     */
    public void paint(Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // calculate percent
        long time = System.nanoTime();
        
        int percent = (int)Math.round(update * 100.0);
        
        g.setColor(Color.green);
        g.fillRect(5, 25, (int)Math.round(percent /100.0 * (getWidth()-10)), 20);
        
        g.setColor(Color.black);
        g.drawRect(5, 25, getWidth()-10, 20);
        
        g.setFont(new Font("Arial", 0, 12));
        g.drawString(""+percent+"%", (getWidth()-10)/2, 40);
        
        if(eta > 0)
        {
            int rem = (int)Math.round(eta/1.0e9);
            g.drawString("Remaining: "+(rem/60)+" min "+(rem%60)+" sec", 10, 60);
        }
        
        g.drawString(text, 5, 20);
    }
    
    /**
     * Resets the start time and estimate of remaining time.
     */
    public void resetTime()
    {
        startTime = 0;
        eta = 0;
    }
    
    public boolean isFinished()
    {
        return update == 1;
    }
    
    /**
     * Calls {@link StatusBar#update(double, double)}, and also updates the status text.
     * @param p the estimate of the proportion completed
     * @param interval the update interval
     * @param text the status text
     */
    public void update(double p, double interval, String text)
    {
        this.text = text;
        update(p, interval);
    }
    
    /**
     * Updates the progress. 
     * If the progress is 0, this method calls {@link StatusBar#resetTime()}.
     * Otherwise, an estimate of the remaining time will be made based on the estimated progress.
     * @param p the estimate of the proportion completed
     * @param inter the update interval
     */
    public void update(double p, double inter)
    {
        if(p == 0)
        {
            resetTime();
        }
        long time = System.nanoTime();
        
        if(startTime > 0)
        {
            eta = (long)Math.round( (time - startTime) / p * (1-p));
        }
        else
        {
            startTime = time;
        }
        
        update = p;
        interval = inter;

    }
}
