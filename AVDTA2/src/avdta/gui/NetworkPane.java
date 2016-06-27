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
    private Project project;
    
    private LinksPane linksPane;
    private NodesPane nodesPane;
    
    public NetworkPane()
    {
        linksPane = new LinksPane();
        nodesPane = new NodesPane();
        
        setLayout(new GridBagLayout());
        
        constrain(this, linksPane, 1, 0, 1, 1);
        constrain(this, nodesPane, 2, 0, 1, 1);
    }
    
    public void setProject(Project project)
    {
        this.project = project;
        
        linksPane.setProject(project);
        nodesPane.setProject(project);
    }
}
