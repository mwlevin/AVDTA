/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.demand;

import avdta.dta.ReadDTANetwork;
import avdta.gui.DTAGUI;
import avdta.gui.GUI;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.GUIPanel;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.project.DemandProject;
import avdta.sav.ReadSAVNetwork;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author micha
 */
public class SAVDemandPanel extends GUIPanel
{
    private ImportDemandPanel importDemandPane;
    private PrepareDemandPanel prepareDemandPane;
    private VehiclesPanel vehiclesPane;

    public SAVDemandPanel(AbstractGUIPanel parent)
    {
        super(parent);
        setLayout(new GridBagLayout());
        
        importDemandPane = new ImportDemandPanel(this);
        prepareDemandPane = new PrepareDemandPanel(this)
        {
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


                            proportionMap.put(ReadSAVNetwork.TRAVELER, prop);
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
        };
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
    
    public void setProject(DemandProject project)
    {
        vehiclesPane.setProject(project);
        prepareDemandPane.setProject(project);
        importDemandPane.setProject(project);
    }
}
