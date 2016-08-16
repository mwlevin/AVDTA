/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import static avdta.gui.util.GraphicUtils.constrain;
import avdta.project.TransitProject;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

/**
 *
 * @author Michael
 */
public class TransitPane extends JPanel
{
    private ImportTransitPane importPane;
    private TransitViewPane viewPane;
    
    public TransitPane()
    {
        importPane = new ImportTransitPane(this);
        viewPane = new TransitViewPane(this);
        
        setLayout(new GridBagLayout());
        
        constrain(this, importPane, 0, 0, 1, 1);
        constrain(this, viewPane, 1, 0, 1, 1);
        
        setEnabled(false);
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
