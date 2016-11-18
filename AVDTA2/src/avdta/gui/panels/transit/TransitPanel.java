/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.transit;

import avdta.gui.DTAGUI;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.GUIPanel;
import static avdta.gui.util.GraphicUtils.constrain;
import avdta.project.TransitProject;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

/**
 *
 * @author Michael
 */
public class TransitPanel extends GUIPanel
{
    private ImportTransitPanel importPane;
    private TransitViewPanel viewPane;
    
    public TransitPanel(AbstractGUIPanel parent)
    {
        super(parent);
        importPane = new ImportTransitPanel(this);
        viewPane = new TransitViewPanel(this);
        
        setLayout(new GridBagLayout());
        
        constrain(this, importPane, 0, 0, 1, 1);
        constrain(this, viewPane, 1, 0, 1, 1);
        
        setMinimumSize(getPreferredSize());
    }
    
    public void setEnabled(boolean e)
    {
        importPane.setEnabled(e);
        viewPane.setEnabled(e);
        super.setEnabled(e);
    }
    
    public void setProject(TransitProject project)
    {
        importPane.setProject(project);
        viewPane.setProject(project);
    }
    
    public void reset()
    {
        importPane.reset();
        viewPane.reset();
    }

}
