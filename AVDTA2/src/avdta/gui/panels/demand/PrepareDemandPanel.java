/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.demand;

import avdta.demand.DynamicODTable;
import avdta.dta.ReadDTANetwork;
import avdta.fourstep.ReadFourStepNetwork;
import avdta.gui.GUI;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.GUIPanel;
import javax.swing.JPanel;
import avdta.project.DemandProject;
import java.awt.GridBagLayout;
import static avdta.gui.util.GraphicUtils.*;
import avdta.project.FourStepProject;
import java.awt.Dimension;
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
    protected DemandProject project;
    private JButton createDynamicOD, createStaticOD, createZones;
    
    private JTextField AVs, demPct;
    private JButton changeType;
    
    public PrepareDemandPanel(AbstractGUIPanel parent)
    {
        this(parent, false);
    }
    
    public PrepareDemandPanel(AbstractGUIPanel parent, boolean fourstep)
    {
       super(parent);
        setLayout(new GridBagLayout());
        
        changeType = new JButton("Change type");
        createDynamicOD = new JButton("Create dynamic OD");
        createStaticOD = new JButton("Create static OD");
        
        
        
        AVs = new JTextField(5);
        AVs.setText("0");
        
        setLayout(new GridBagLayout());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Static OD"));
        
        constrain(p, createStaticOD, 0, 0, 1, 1);
        
        constrain(this, p, 0, 0, 1, 1);
        
        
        
        
        
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Dynamic OD"));
        
        constrain(p2, createDynamicOD, 0, 0, 2, 1);
        constrain(p2, new JLabel("Percent of AVs:"), 0, 1, 1, 1);
        constrain(p2, AVs, 1, 1, 1, 1);
        constrain(p2, changeType, 0, 2, 2, 1);
        
        constrain(this, p2, 0, 1, 1, 1);
        
        
        
        
        if(fourstep)
        {
            createZones = new JButton("Create zones");
            demPct = new JTextField(5);
            demPct.setText("100");
            
            p2 = new JPanel();
            p2.setLayout(new GridBagLayout());
            p2.setBorder(BorderFactory.createTitledBorder("Zones"));

            constrain(p2, new JLabel("Percent of demand:"), 0, 0, 1, 1);
            constrain(p2, demPct, 1, 0, 1, 1);
            constrain(p2, createZones, 0, 1, 2, 1);

            constrain(this, p2, 0, 2, 1, 1);
            
            createZones.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    createZones();
                }
            });
        }
        
        
        p.setPreferredSize(new Dimension((int)p2.getPreferredSize().getWidth(), (int)p.getPreferredSize().getHeight()));
        
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
    
    public void createZones()
    {
        try
        {
            Double.parseDouble(demPct.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            demPct.setText("");
            demPct.requestFocus();
            return;
        }
        
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    double dem = Double.parseDouble(demPct.getText().trim());
                    
                    ReadFourStepNetwork read = new ReadFourStepNetwork();
                    read.createZonesFile((FourStepProject)project, dem/100.0);
                }
                catch(Exception ex)
                {
                    GUI.handleException(ex);
                }
                
                parentReset();
                parentSetEnabled(true);
            }
        };
        t.start();
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
        
        changeType(Double.parseDouble(AVs.getText().trim())/100.0);
    }
    
    
    public void changeType(final double prop)
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    Map<Integer, Double> proportionMap = new HashMap<Integer, Double>();


                    proportionMap.put(ReadDTANetwork.AV + ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.ICV, prop);
                    proportionMap.put(ReadDTANetwork.HV + ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.ICV, 1-prop);

                    ReadDTANetwork read = new ReadDTANetwork();
                    read.changeDynamicType(project, proportionMap);

                    parentReset();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
                
                parentSetEnabled(true);
            }
        };
        t.start();
        
        
    }
    

    public void setEnabled(boolean e)
    {
        createDynamicOD.setEnabled(e);
        createStaticOD.setEnabled(e);
        AVs.setEditable(e);
        changeType.setEnabled(e);
        if(demPct != null)
        {
            demPct.setEditable(e);
            createZones.setEnabled(e);
        }
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
    
    public void setProject(DemandProject project)
    {
        this.project = project;
        reset();
    }
    
}
