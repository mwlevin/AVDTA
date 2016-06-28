/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import javax.swing.JPanel;
import static avdta.gui.GraphicUtils.*;
import avdta.project.DTAProject;
import avdta.project.Project;
import java.awt.GridBagLayout;

/**
 *
 * @author micha
 */
public class NetworkPane extends JPanel
{
    
    private LinksPane linksPane;
    private NodesPane nodesPane;
    private ImportNetworkPane importPane;
    
    public NetworkPane()
    {
        linksPane = new LinksPane(this);
        nodesPane = new NodesPane(this);
        importPane = new ImportNetworkPane(this);
        
        setLayout(new GridBagLayout());
        
        constrain(this, importPane, 0, 0, 1, 1);
        constrain(this, linksPane, 1, 0, 1, 1);
        constrain(this, nodesPane, 2, 0, 1, 1);
    }
    
    public void setProject(Project project)
    {
        linksPane.setProject(project);
        nodesPane.setProject(project);
        importPane.setProject(project);
    }
    
    public void reset()
    {
        nodesPane.reset();
        linksPane.reset();
    }
    
    public void enable()
    {
        nodesPane.enable();
        linksPane.enable();
        importPane.enable();
    }
    
    public void disable()
    {
        nodesPane.disable();
        linksPane.disable();
        importPane.disable();
    }
}
