/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

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
/**
 *
 * @author micha
 */
public class VehiclesPane extends JPanel
{
    private DTAProject project;
    private DemandPane parent;
    
    private JTextArea data;
    
    public VehiclesPane(DemandPane parent)
    {
        data = new JTextArea(10, 30);
        data.setEditable(false);
        
        setLayout(new GridBagLayout());
        constrain(this, new JScrollPane(data), 0, 0, 1, 1);
    }
    
    public void enable()
    {
        
    }
    
    public void disable()
    {
        
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
            
        }
        
    }
    
    public void setProject(DTAProject project)
    {
        this.project = project;
        reset();
    }
}
