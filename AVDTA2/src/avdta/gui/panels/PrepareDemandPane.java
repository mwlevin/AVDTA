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
public class PrepareDemandPane extends JPanel
{
    private DTAProject project;
    private DemandPane parent;
    
    private JButton createDynamicOD;
    
    private JTextField AVs;
    private JButton changeType;
    
    public PrepareDemandPane(DemandPane parent)
    {
        this.parent = parent;
        
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
                try
                {
                    createDynamicOD();
                }
                catch(IOException ex)
                {
                   GUI.handleException(ex);
                }
            }
        });
        changeType.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    changeType();
                }
                catch(IOException ex)
                {
                   GUI.handleException(ex);
                }
            }
        });

        reset();
    }
    
    public void createDynamicOD() throws IOException
    {
        parent.setEnabled(false);
        
        ReadDTANetwork read = new ReadDTANetwork();
        
        read.createDynamicOD(project);
        
        parent.reset();
        parent.setEnabled(true); 
    }
    
    public void changeType() throws IOException
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
        
        parent.setEnabled(false);
        
        Map<Integer, Double> proportionMap = new HashMap<Integer, Double>();
        
        double prop = Double.parseDouble(AVs.getText().trim())/100.0;
        
        proportionMap.put(ReadDTANetwork.AV + ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.ICV, prop);
        proportionMap.put(ReadDTANetwork.HV + ReadDTANetwork.DA_VEHICLE + ReadDTANetwork.ICV, 1-prop);
        
        ReadDTANetwork read = new ReadDTANetwork();
        read.changeType(project, proportionMap);
        
        parent.reset();
        parent.setEnabled(true);
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
