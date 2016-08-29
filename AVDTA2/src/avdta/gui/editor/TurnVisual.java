/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.link.Link;
import avdta.network.node.Turn;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.swing.JComponent;

/**
 *
 * @author ml26893
 */
public class TurnVisual extends JComponent
{
    private Link i, j;
    
    public TurnVisual()
    {
        setPreferredSize(new Dimension(100, 100));
        setBackground(Color.white);
    }
    
    public void setLinks(Link i, Link j)
    {
        this.i = i;
        this.j = j;
        repaint();
    }
    
    public void setTurn(Turn t)
    {
        setLinks(t.i, t.j);
    }
    
    public void paint(Graphics window)
    {
        Graphics2D g = (Graphics2D)window;
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(getForeground());
        g.drawRect(0, 0, getWidth(), getHeight());
        
        g.setColor(Color.black);
        g.setStroke(new BasicStroke(3));
        
        int radius = (int)Math.min(getWidth()/2, getHeight()/2)-5;
        
        if(i != null && j != null)
        {
            double angle1 = i.getIncomingAngle();
            double angle2 = j.getOutgoingAngle();

            Point center = new Point(getWidth()/2, getHeight()/2);

            Point p1 = new Point(center.x + (int)Math.round(Math.cos(angle1)*radius), center.y + (int)Math.round(Math.sin(angle1)*radius));
            Point p2 = new Point(center.x + (int)Math.round(Math.cos(angle2)*radius), center.y + (int)Math.round(Math.sin(angle2)*radius));

            g.drawLine(center.x, center.y, p1.x, p1.y);
            g.setColor(Color.red);
            g.drawLine(center.x, center.y, p2.x, p2.y);

            if(Math.abs(angle1 - angle2) < 0.1)
            {
                g.setColor(Color.black);
                g.drawString("U-turn", 20, 40);
            }
        }
    }
}
