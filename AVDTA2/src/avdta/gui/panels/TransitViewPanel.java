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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

/**
 *
 * @author Michael
 */
public class TransitViewPanel extends GUIPanel
{
    private TransitProject project;
    
    private JTextArea data;
    private JButton createBuses, clearBuses;
    
    
    public TransitViewPanel(TransitPanel parent)
    {
        super(parent);
        
        data = new JTextArea(5, 20);
        createBuses = new JButton("Create buses");
        clearBuses = new JButton("Clear buses");
        
        data.setEditable(false);
        
        
        createBuses.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createBuses();
            }
        });
        
        clearBuses.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                clearBuses();
            }
        });
        
        setLayout(new GridBagLayout());
        
        JScrollPane scroll = new JScrollPane(data);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Create transit"));
        constrain(p, scroll, 0, 0, 1, 1);
        constrain(p, createBuses, 0, 1, 1, 1);
        constrain(p, clearBuses, 0, 2, 1, 1);
        
        constrain(this, p, 0, 0, 1, 1);
    }
    
    public void clearBuses()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    PrintStream fileout = new PrintStream(new FileOutputStream(project.getBusFile()), true);
                    fileout.println(ReadNetwork.getBusFileHeader());
                    fileout.close();
                    
                    project.loadSimulator();
                    parentReset();
                    parentSetEnabled(true);
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        };
        t.start();
    }
    
    public void createBuses()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    ReadNetwork read = new ReadNetwork();
                    read.createTransit(project);
                    project.loadSimulator();
                    parentReset();
                    parentSetEnabled(true);
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
        clearBuses.setEnabled(e);
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
