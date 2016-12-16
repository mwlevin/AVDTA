/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.analysis;

import avdta.gui.DTAGUI;
import avdta.gui.GUI;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.GUIPanel;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import static avdta.gui.util.GraphicUtils.*;
import avdta.gui.util.JFileField;
import avdta.network.Simulator;
import avdta.project.Project;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
/**
 *
 * @author ml26893
 */
public class AnalysisPanel extends GUIPanel
{
    private JFileField linkids;
    private JButton tt, volume, printLinkTT;
    
    private Project project;
    
    public AnalysisPanel(AbstractGUIPanel parent)
    {
        super(parent);
        
        final JPanel panel = this;
        
        tt = new JButton("Travel times");
        volume = new JButton("Link volume");
        volume.setToolTipText("Link volumes for LTM links");

        tt.setEnabled(false);
        volume.setEnabled(false);
        
        printLinkTT = new JButton("Link travel times");
        printLinkTT.setEnabled(false);
        
        printLinkTT.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int start, end;
                
                try
                {
                    start = Integer.parseInt(JOptionPane.showInputDialog(panel, "Enter start time (s)", "Link travel times", JOptionPane.QUESTION_MESSAGE));
                }
                catch(Exception ex)
                {
                    return;
                }
                
                try
                {
                    end = Integer.parseInt(JOptionPane.showInputDialog(panel, "Enter end time (s)", "Link travel times", JOptionPane.QUESTION_MESSAGE));
                }
                catch(Exception ex)
                {
                    return;
                }
                
                try
                {
                    if(linkids.getFile() != null)
                    {
                        project.getSimulator().printLinkTT(start, end, new File(project.getResultsFolder()+"/linkTT.txt"), readLinkIds());
                    }
                    else
                    {
                        project.getSimulator().printLinkTT(start, end, new File(project.getResultsFolder()+"/linkTT.txt"));
                    }
                
                    JOptionPane.showMessageDialog(panel, "Link travel times printed to \n"+project.getResultsFolder()+"/linkTT.txt", "Link travel times", JOptionPane.INFORMATION_MESSAGE);
                }
                catch(Exception ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        
        linkids = new JFileField(10, null, "projects/")
        {
            public void valueChanged(File file)
            {
                boolean enable = file != null && project != null;
                tt.setEnabled(enable);
                volume.setEnabled(enable);
            }
        };
        
        tt.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                parentSetEnabled(false);
                
                Thread t = new Thread()
                {
                    public void run()
                    {
                        Simulator sim = project.getSimulator();
                        String name = getFilename();
                        
                        String filename = project.getResultsFolder()+"/link_tt_"+name+".txt";
                        try
                        {
                            sim.printLinkTT(0, sim.getLastExitTime()+sim.ast_duration, new File(filename), readLinkIds());
                        }
                        catch(IOException ex)
                        {
                            GUI.handleException(ex);
                        }
                        
                        JOptionPane.showMessageDialog(panel, "Travel time data printed to "+filename, "Complete", JOptionPane.INFORMATION_MESSAGE);
                        parentSetEnabled(true);
                    }
                };
                t.start();
                
            }
        });
        
        volume.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

                parentSetEnabled(false);
                
                Thread t = new Thread()
                {
                    public void run()
                    {
                        Simulator sim = project.getSimulator();
                        String name = getFilename();
                        
                        String filename = project.getResultsFolder()+"/link_flow_"+name+".txt";
                        
                        try
                        {

                            sim.postProcess();
                            sim.printLinkFlow(0, sim.getLastExitTime()+sim.ast_duration, new File(filename), readLinkIds());
                        }
                        catch(IOException ex)
                        {
                            GUI.handleException(ex);
                        }
                        
                        JOptionPane.showMessageDialog(panel, "Link flow data printed to "+filename, "Complete", JOptionPane.INFORMATION_MESSAGE);

                        parentSetEnabled(true);
                    }
                };
                t.start();
                
            }
        });
        
        setLayout(new GridBagLayout());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, new JLabel("Link ids: "), 0, 0, 1, 1);
        constrain(p, linkids, 1, 0, 1, 1);
        
        constrain(this, p, 0, 0, 1, 1);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Analyses"));
        
        constrain(p2, tt, 0, 0, 1, 1);
        constrain(p2, volume, 0, 1, 1, 1);
        constrain(p2, printLinkTT, 0, 2, 1, 1);
        
        p2.setPreferredSize(new Dimension((int)p.getPreferredSize().getWidth(), (int)p2.getPreferredSize().getHeight()));
        
        constrain(this, p2, 0, 1, 1, 1);
        
    }
    
    private String getFilename()
    {
        String name = linkids.getFile().getName();
                
        if(name.indexOf("\\") > 0)
        {
            name = name.substring(name.lastIndexOf("\\")+1);
        }
        if(name.indexOf('.') > 0)
        {
            name = name.substring(0, name.indexOf('.'));
        }
        return name;
    }
    
    private Set<Integer> readLinkIds()
    {
        Set<Integer> output = new HashSet<Integer>();
        
        try
        {
            Scanner filein = new Scanner(linkids.getFile());
            
            while(!filein.hasNextInt())
            {
                filein.nextLine();
            }
            
            while(filein.hasNextInt())
            {
                output.add(filein.nextInt());
                
                if(filein.hasNextLine())
                {
                    filein.nextLine();
                }
            }
            filein.close();
        }
        catch(IOException ex)
        {
            GUI.handleException(ex);
        }
        
        return output;
    }
    
    public void setProject(Project project)
    {
        this.project = project;
        
        boolean enable = linkids.getFile() != null && project != null;
        tt.setEnabled(enable);
        volume.setEnabled(enable);
        printLinkTT.setEnabled(project != null);
    }
    
    public void setEnabled(boolean e)
    {
        linkids.setEnabled(e);
        
        boolean enable = linkids.getFile() != null && project != null;
        tt.setEnabled(e && enable);
        volume.setEnabled(e && enable);
        
        printLinkTT.setEnabled(e && project != null);
        
        super.setEnabled(e);
    }
}
