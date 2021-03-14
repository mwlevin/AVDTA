/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.demand;

import avdta.gui.panels.demand.DemandPanel;
import avdta.dta.ReadDTANetwork;
import avdta.gui.GUI;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.GUIPanel;
import avdta.project.DemandProject;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import static avdta.gui.util.GraphicUtils.*;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.fuel.VehicleClass;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
/**
 *
 * @author micha
 */
public class VehiclesPanel extends GUIPanel
{
    private DemandProject project;
    
    private JTextArea data;
    
    private JTextField prop;
    private JButton prepareDemand;
    
    public VehiclesPanel(AbstractGUIPanel parent)
    {
        super(parent);
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
        JScrollPane scroll = new JScrollPane(data);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Demand"));
        constrain(p, scroll, 0, 0, 2, 1);
        constrain(p, new JLabel("Percent of dynamic OD: "), 0, 1, 1, 1);
        constrain(p, prop, 1, 1, 1, 1);
        constrain(p, prepareDemand, 0, 2, 2, 1);
        
        constrain(this, p, 0, 0, 1, 1);
        
        reset();
    }
    
    public void setEnabled(boolean e)
    {
        prop.setEditable(e);
        prepareDemand.setEnabled(e);
        super.setEnabled(e);
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
        parentSetEnabled(false);
        
        ReadDTANetwork read = new ReadDTANetwork();
        read.prepareDemand(project, Double.parseDouble(prop.getText().trim())/100.0);
        
        project.loadSimulator();
     
        prop.setText("100");
        
        parentReset();
        parentSetEnabled(true);
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
                
                if(v.getVehClass() == VehicleClass.icv)
                {
                    ICV++;
                }
                else if(v.getVehClass() == VehicleClass.bev)
                {
                    BEV++;
                }
                
                if(v instanceof Bus)
                {
                    bus++;
                }   
                else if(v instanceof PersonalVehicle)
                {
                    personal++;
                }
                     
                
            }
            data.append(project.getSimulator().getVehicles().size()+"\ttotal vehicles\n\n");
            
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
            
            setEnabled(true);
        }
        else
        {
            setEnabled(false);
        }
    }
    
    public void setProject(DemandProject project)
    {
        this.project = project;
        reset();
    }
}
