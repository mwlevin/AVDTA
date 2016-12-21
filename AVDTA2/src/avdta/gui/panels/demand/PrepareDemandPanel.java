/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.demand;

import avdta.demand.DynamicODTable;
import avdta.dta.ReadDTANetwork;
import avdta.gui.GUI;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.GUIPanel;
import javax.swing.JPanel;
import avdta.project.DTAProject;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import static avdta.gui.util.GraphicUtils.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author micha
 */
public class PrepareDemandPanel extends GUIPanel
{
    private DTAProject project;
    private JButton createDynamicOD, createStaticOD;
    
    private JTextField AVs;
    private JButton changeType;
    
    public PrepareDemandPanel(AbstractGUIPanel parent)
    {
       super(parent);
        setLayout(new GridBagLayout());
        
        changeType = new JButton("Change type");
        createDynamicOD = new JButton("Create dynamic OD");
        createStaticOD = new JButton("Create static OD");
        
        AVs = new JTextField(5);
        AVs.setText("0");
        
        setLayout(new GridBagLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Dynamic OD"));
        
        constrain(panel, createStaticOD, 0, 0, 2, 1);
        constrain(panel, new JLabel(""), 0, 1, 2, 1);
        constrain(panel, createDynamicOD, 0, 2, 2, 1);
        constrain(panel, new JLabel("Percent of AVs:"), 0, 3, 1, 1);
        constrain(panel, AVs, 1, 3, 1, 1);
        constrain(panel, changeType, 0, 4, 2, 1);
        
        constrain(this, panel, 0, 0, 1, 1);
        
        createDynamicOD.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createDynamicOD();
            }
        });
        
        createStaticOD.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createStaticOD();
            }
        });
        
        changeType.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                changeType();
            }
        });

        reset();
    }
    
    public void createStaticOD()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    DynamicODTable table = new DynamicODTable(project);
                    
                    table.printStaticOD(project);
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
                
                parentReset();
                parentSetEnabled(true); 
            }
        };
        t.start();
    }
    
    public void createDynamicOD()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    ReadDTANetwork read = new ReadDTANetwork();
        
                    read.createDynamicOD(project);

                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
                
                parentReset();
                parentSetEnabled(true);
            }
        };
        t.start();
        
        
    }
    
    public void changeType()
    {
        try
        {
            Double.parseDouble(AVs.getText().trim());
        }
        catch(Exception ex)
        {
            AVs.setText("0");
            AVs.requestFocus();
            return;
        }
        
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    Map<Integer, Double> proportionMap = new HashMap<Integer, Double>();
        
                    double prop = Double.parseDouble(AVs.getText().trim())/100.0;

                    proportionMap.put(ReadDTANetwork.AV + ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.ICV, prop);
                    proportionMap.put(ReadDTANetwork.HV + ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.ICV, 1-prop);

                    ReadDTANetwork read = new ReadDTANetwork();
                    read.changeDynamicType(project, proportionMap);

                    parentReset();
                }
                catch(IOException ex)
                {
                    
                }
            }
        };
        t.start();
        
        
    }
    

    public void setEnabled(boolean e)
    {
        createDynamicOD.setEnabled(e);
        AVs.setEditable(e);
        changeType.setEnabled(e);
        super.setEnabled(e);
    }
    
    
    public void reset()
    {
        if(project == null)
        {
            setEnabled(false);
        }
        else
        {
            setEnabled(true);
        }
    }
    
    public void setProject(DTAProject project)
    {
        this.project = project;
        reset();
    }
    
}
