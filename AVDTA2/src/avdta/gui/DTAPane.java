/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.dta.Assignment;
import avdta.project.DTAProject;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import static avdta.gui.GraphicUtils.*;

/**
 *
 * @author micha
 */
public class DTAPane extends JPanel
{
    private AssignmentPane assignPane;
    private MSAPane msaPane;
    
    public DTAPane()
    {
        assignPane = new AssignmentPane(this);
        msaPane = new MSAPane(this);
        
        setLayout(new GridBagLayout());
        constrain(this, assignPane, 0, 0, 1, 1);
        constrain(this, msaPane, 1, 0, 1, 1);
    }
    
    
    public void setProject(DTAProject project)
    {
        assignPane.setProject(project);
        msaPane.setProject(project);
    }
    
    public void enable()
    {
        assignPane.enable();
        msaPane.enable();
    }
    
    
    public void disable()
    {
        assignPane.disable();
        msaPane.disable();
    }
    
    public void reset()
    {
        assignPane.reset();
        msaPane.reset();
    }
    
    public void showAssignment(Assignment assign)
    {
        msaPane.showAssignment(assign);
    }
    public void loadAssignment(Assignment assign)
    {
        msaPane.loadAssignment(assign);
    }
}
