/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.analysis;

import avdta.dta.DTASimulator;
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
    private JButton tt, volume, busTT, importResults;
    
    private Project project;
    
    public AnalysisPanel(AbstractGUIPanel parent)
    {
        super(parent);
        
        final JPanel panel = this;
        
        tt = new JButton("Travel times");
        volume = new JButton("Link volume");
        busTT = new JButton("Bus TT");
        importResults = new JButton("Import results");

        tt.setEnabled(false);
        volume.setEnabled(false);
        busTT.setEnabled(false);
        importResults.setEnabled(false);
        
        
        
       
        
        linkids = new JFileField(10, null, "projects/")
        {
            public void valueChanged(File file)
            {
                boolean enable = file != null && project != null;
                tt.setEnabled(enable);
                volume.setEnabled(enable);
            }
        };
        
        
        busTT.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               Simulator sim = project.getSimulator();
               String filename = project.getResultsFolder()+"/bus_tt_"+project.getName()+".txt";
               
               try
               {
                    sim.printBusTime(new File(filename));
               
                    JOptionPane.showMessageDialog(panel, "Bus travel time data printed to "+filename, "Complete", JOptionPane.INFORMATION_MESSAGE);
               }
               catch(IOException ex)
               {
                   GUI.handleException(ex);
               }
           }
        });
        
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
                        
                        String filename;
                        
                        try
                        {
                            if(linkids.getFile() != null)
                            {
                                String name = getFilename();
                        
                                filename = project.getResultsFolder()+"/link_tt_"+name+".txt";
                                sim.printLinkTT(0, sim.getLastExitTime()+sim.ast_duration, new File(filename), readLinkIds());
                            }
                            else
                            {
                                filename = project.getResultsFolder()+"/link_tt.txt";
                                sim.printLinkTT(0, sim.getLastExitTime()+sim.ast_duration, new File(filename));
                            }
                            
                            JOptionPane.showMessageDialog(panel, "Travel time data printed to "+filename, "Complete", JOptionPane.INFORMATION_MESSAGE);
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
        });
        
        importResults.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               DTASimulator sim = (DTASimulator)project.getSimulator();
               
               try
               {
                    sim.importResults();
               }
               catch(IOException ex)
               {
                   GUI.handleException(ex);
               }
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
                        
                        String filename;
                        
                        try
                        {
                            
                            sim.postProcess();
                            
                            if(linkids.getFile() != null)
                            {
                                String name = getFilename();
                        
                                filename = project.getResultsFolder()+"/link_flow_"+name+".txt";
                                sim.printLinkFlow(0, sim.getLastExitTime()+sim.ast_duration, new File(filename), readLinkIds());
                            }
                            else
                            {
                                filename = project.getResultsFolder()+"/link_flow.txt";
                                sim.printLinkFlow(0, sim.getLastExitTime()+sim.ast_duration, new File(filename));
                            }
                            
                            JOptionPane.showMessageDialog(panel, "Link flow data printed to "+filename, "Complete", JOptionPane.INFORMATION_MESSAGE);
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
        constrain(p2, busTT, 0, 2, 1, 1);
        constrain(p2, importResults, 0, 3, 1, 1);
        
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
        
        setEnabled(project != null);
    }
    
    public void setEnabled(boolean e)
    {
        boolean enable = e && project != null;
        
        linkids.setEnabled(enable);
        tt.setEnabled(enable);
        volume.setEnabled(enable);
        busTT.setEnabled(enable);
        importResults.setEnabled(enable);
        
        super.setEnabled(e);
    }
}
