/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.project.DTAProject;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import static avdta.gui.GraphicUtils.*;
/**
 *
 * @author micha
 */
public class DemandPane extends JPanel
{
    private ImportDemandPane importDemandPane;
    private PrepareDemandPane prepareDemandPane;
    private VehiclesPane vehiclesPane;
    
    public DemandPane()
    {
        setLayout(new GridBagLayout());
        
        importDemandPane = new ImportDemandPane(this);
        prepareDemandPane = new PrepareDemandPane(this);
        vehiclesPane = new VehiclesPane(this);
        
        constrain(this, importDemandPane, 0, 0, 1, 1);
        constrain(this, prepareDemandPane, 1, 0, 1, 1);
        constrain(this, vehiclesPane, 2, 0, 1, 1);
    }
    
    public void reset()
    {
        vehiclesPane.reset();
        prepareDemandPane.reset();
    }
    
    public void enable()
    {
        vehiclesPane.enable();
        prepareDemandPane.enable();
        importDemandPane.enable();
    }
    
    public void disable()
    {
        vehiclesPane.disable();
        prepareDemandPane.disable();
        importDemandPane.disable();
    }
    
    public void setProject(DTAProject project)
    {
        vehiclesPane.setProject(project);
        prepareDemandPane.setProject(project);
        importDemandPane.setProject(project);
    }
}
