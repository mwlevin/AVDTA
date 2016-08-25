/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.gui.GUI;
import avdta.project.TransitProject;
import avdta.vehicle.Bus;
import avdta.vehicle.Vehicle;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import static avdta.gui.util.GraphicUtils.*;
import avdta.network.ReadNetwork;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JScrollPane;

/**
 *
 * @author Michael
 */
public class TransitViewPane extends JPanel
{
    private TransitPane parent;
    
    private TransitProject project;
    
    private JTextArea data;
    private JButton createBuses;
    
    
    public TransitViewPane(TransitPane parent_)
    {
        this.parent = parent_;
        
        data = new JTextArea(5, 20);
        createBuses = new JButton("Create buses");
        
        data.setEditable(false);
        
        
        createBuses.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createBuses();
            }
        });
        
        setLayout(new GridBagLayout());
        
        constrain(this, new JScrollPane(data), 0, 0, 1, 1);
        constrain(this, createBuses, 0, 1, 1, 1);
    }
    
    public void createBuses()
    {
        setEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    ReadNetwork read = new ReadNetwork();
                    read.createTransit(project);
                    project.loadSimulator();
                    parent.reset();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        };
        t.start();
        
    }
    
    public void setProject(TransitProject project)
    {
        this.project = project;
        
        reset();
    }
    
    public void setEnabled(boolean e)
    {
        createBuses.setEnabled(e);
        
        super.setEnabled(e);
    }
    
    
    
    public void reset()
    {
        if(project != null)
        {
            data.setText("");
            
            int total = 0;
            
            int AV = 0;
            
            for(Vehicle v : project.getSimulator().getVehicles())
            {
                if(v instanceof Bus)
                {
                    total ++;
                    
                    if(v.getDriver().isAV())
                    {
                        AV++;
                    }
                }
            }
            
            data.append(total+"\tbuses\n");
            data.append("\n"+(total-AV)+"\tHVs\n");
            data.append(AV+"\tAVs\n");
            setEnabled(true);
        }
        else
        {
            setEnabled(false);
        }
    }
}
