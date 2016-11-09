/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.gui.DTAGUI;
import avdta.gui.panels.ImportDemandPanel;
import avdta.gui.panels.VehiclesPanel;
import avdta.gui.panels.PrepareDemandPanel;
import avdta.project.DTAProject;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
/**
 *
 * @author micha
 */
public class DemandPanel extends GUIPanel
{
    private ImportDemandPanel importDemandPane;
    private PrepareDemandPanel prepareDemandPane;
    private VehiclesPanel vehiclesPane;

    public DemandPanel(DTAGUI parent)
    {
        super(parent);
        setLayout(new GridBagLayout());
        
        importDemandPane = new ImportDemandPanel(this);
        prepareDemandPane = new PrepareDemandPanel(this);
        vehiclesPane = new VehiclesPanel(this);
        
        constrain(this, importDemandPane, 0, 0, 1, 1);
        constrain(this, prepareDemandPane, 1, 0, 1, 1);
        constrain(this, vehiclesPane, 2, 0, 1, 1);
        
        setMinimumSize(getPreferredSize());
    }

    public void reset()
    {
        vehiclesPane.reset();
        prepareDemandPane.reset();
    }

    public void setEnabled(boolean e)
    {
        vehiclesPane.setEnabled(e);
        prepareDemandPane.setEnabled(e);
        importDemandPane.setEnabled(e);
        super.setEnabled(e);
    }
    
    public void setProject(DTAProject project)
    {
        vehiclesPane.setProject(project);
        prepareDemandPane.setProject(project);
        importDemandPane.setProject(project);
    }
}
