/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.dta.ReadDTANetwork;
import avdta.gui.GUI;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 *
 * @author micha
 */
public class PrepareDemandPane extends GUIPanel
{
    private DTAProject project;
    private JButton createDynamicOD;
    
    private JTextField AVs;
    private JButton changeType;
    
    public PrepareDemandPane(GUIPane parent)
    {
       super(parent);
        setLayout(new GridBagLayout());
        
        changeType = new JButton("Change type");
        createDynamicOD = new JButton("Create dynamic OD");
        
        AVs = new JTextField(5);
        AVs.setText("0");
        
        setLayout(new GridBagLayout());
        constrain(this, createDynamicOD, 0, 0, 2, 1);
        constrain(this, new JLabel("Percent of AVs:"), 0, 1, 1, 1);
        constrain(this, AVs, 1, 1, 1, 1);
        constrain(this, changeType, 0, 2, 2, 1);
        
        
        
        createDynamicOD.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createDynamicOD();
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
