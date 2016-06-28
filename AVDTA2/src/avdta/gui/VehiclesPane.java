/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.dta.ReadDTANetwork;
import avdta.project.DTAProject;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import static avdta.gui.GraphicUtils.*;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.fuel.VehicleClass;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
/**
 *
 * @author micha
 */
public class VehiclesPane extends JPanel
{
    private DTAProject project;
    private DemandPane parent;
    
    private JTextArea data;
    
    private JTextField prop;
    private JButton prepareDemand;
    
    public VehiclesPane(DemandPane parent)
    {
        data = new JTextArea(10, 20);
        data.setEditable(false);
        
        prop = new JTextField(5);
        prepareDemand = new JButton("Prepare demand");
        
        prepareDemand.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    prepareDemand();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        
        prop.setText("100");
        
        setLayout(new GridBagLayout());
        constrain(this, new JScrollPane(data), 0, 0, 2, 1);
        constrain(this, new JLabel("Percent of dynamic OD: "), 0, 1, 1, 1);
        constrain(this, prop, 1, 1, 1, 1);
        constrain(this, prepareDemand, 0, 2, 2, 1);
        
        reset();
    }
    
    public void enable()
    {
        prop.setEditable(true);
        prepareDemand.setEnabled(true);
    }
    
    public void disable()
    {
        prop.setEditable(false);      
        prepareDemand.setEnabled(false);
    }
    
    public void prepareDemand() throws IOException
    {
        try
        {
            Double.parseDouble(prop.getText().trim());
        }
        catch(Exception ex)
        {
            prop.setText("100");
            prop.requestFocus();
            return;
        }
        parent.disable();
        
        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project, Double.parseDouble(prop.getText().trim())/100.0);
        
        project.loadSimulator();
     
        prop.setText("100");
        
        parent.reset();
        parent.enable();
    }
    
    public void reset()
    {
        data.setText("");
        
        if(project != null)
        {
            int HVs = 0;
            int AVs = 0;
            int personal = 0;
            int bus = 0;
            int SAVs = 0;
            int ICV = 0;
            int BEV = 0;
            
            
            
            for(Vehicle v : project.getSimulator().getVehicles())
            {
                if(v.getDriver() == DriverType.AV)
                {
                    AVs++;
                }
                else if(v.getDriver() == DriverType.HV)
                {
                    HVs++;
                }
                
                if(v.getVehicleClass() == VehicleClass.icv)
                {
                    ICV++;
                }
                else if(v.getVehicleClass() == VehicleClass.bev)
                {
                    BEV++;
                }
                
                if(v instanceof PersonalVehicle)
                {
                    personal++;
                }
                else if(v instanceof Bus)
                {
                    bus++;
                }        
                
            }
            data.append(project.getSimulator().getVehicles().size()+"\tvehicles\n\n");
            
            if(personal > 0)
            {
                data.append(personal+"\tpersonal vehicles\n");
            }
            if(bus > 0)
            {
                data.append(bus+"\tbuses\n");
            }
            if(personal > 0 || bus > 0)
            {
                data.append("\n");
            }
            
            if(HVs > 0)
            {
                data.append(HVs+"\tHVs\n");
            }
            if(AVs > 0)
            {
                data.append(AVs+"\tAVs\n");
            }
            if(HVs>0 || AVs>0)
            {
                data.append("\n");
            }
            
            if(ICV > 0)
            {
                data.append(ICV+"\tICVs\n");
            } 
            if(BEV > 0)
            {
                data.append(BEV+"\tBEVs\n");
            }
            
            enable();
        }
        else
        {
            disable();
        }
    }
    
    public void setProject(DTAProject project)
    {
        this.project = project;
        reset();
    }
}
