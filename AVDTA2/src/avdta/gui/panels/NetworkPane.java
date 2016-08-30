/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.gui.panels.NodesPane;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
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
        
        setMinimumSize(getPreferredSize());
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
    
    public void setEnabled(boolean e)
    {
        nodesPane.setEnabled(e);
        linksPane.setEnabled(e);
        importPane.setEnabled(e);
        super.setEnabled(e);
    }
    
}
