/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.network;

import avdta.gui.DTAGUI;
import avdta.gui.GUI;
import avdta.gui.editor.visual.rules.LinkBusRule;
import avdta.gui.panels.GUIPanel;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.network.ReadNetwork;
import avdta.network.link.CTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.link.LinkRecord;
import avdta.project.DTAProject;
import avdta.project.Project;
import avdta.vehicle.Vehicle;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author micha
 */
public class LinksPanel extends GUIPanel
{
    private Project project;

    private JTextArea data;
    
    private static final String[] CTM_LINK_OPTIONS = new String[]{"CTM", "DLR", "Shared transit", "Split transit"};
    private static final String[] LTM_LINK_OPTIONS = new String[]{"LTM", "CACC"};
    
    private JRadioButton ctm, ltm;
    private JComboBox<String> ctmOptions;
    private JComboBox<String> ltmOptions;
    private JButton save, reset;
    
    
    private JTextField HVtau, AVtau;
    
    private JTextField mesoDelta;
    private JTextField timestep;
    
    public LinksPanel(NetworkPanel parent)
    {
        super(parent);
        
        data = new JTextArea(5, 20);
        data.setEditable(false);
        
        HVtau = new JTextField(5);
        AVtau = new JTextField(5);
        
        ctmOptions = new JComboBox<String>(CTM_LINK_OPTIONS);
        ltmOptions = new JComboBox<String>(LTM_LINK_OPTIONS);
        
        mesoDelta = new JTextField(5);
        timestep = new JTextField(5);
        
        ctm = new JRadioButton("CTM");
        ltm = new JRadioButton("LTM");
        
        ButtonGroup group = new ButtonGroup();
        group.add(ctm);
        group.add(ltm);
        
        
        
        save = new JButton("Save");
        reset = new JButton("Reset");
        
        save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                save();
            }
        });
        
        reset.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                reset();
            }
        });
        
        ctm.addChangeListener(new ChangeListener()
        {
           public void stateChanged(ChangeEvent e)
           {
               ctmOptions.setEnabled(ctm.isSelected());
           }
        });
        
        ltm.addChangeListener(new ChangeListener()
        {
           public void stateChanged(ChangeEvent e)
           {
               ltmOptions.setEnabled(ltm.isSelected());
           }
        });
        
        
        setLayout(new GridBagLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        JScrollPane scroll = new JScrollPane(data);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        panel.setBorder(BorderFactory.createTitledBorder("Links"));
        constrain(panel, scroll, 0, 1, 2, 1);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, new JLabel("Flow model"), 0, 0, 1, 1);
        constrain(p, ctm, 0, 1, 1, 1);
        constrain(p, ltm, 0, 2, 1, 1);
        constrain(p, ctmOptions, 1, 1, 1, 1);
        constrain(p, ltmOptions, 1, 2, 1, 1);
        constrain(panel, p, 0, 3, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, new JLabel("Mesoscopic delta: "), 0, 0, 1, 1);
        constrain(p, mesoDelta, 1, 0, 1, 1);
        constrain(p, new JLabel("Timestep: "), 0, 1, 1, 1);
        constrain(p, timestep, 1, 1, 1, 1);
        constrain(p, new JLabel("HV reaction time: "), 0, 2, 1, 1);
        constrain(p, HVtau, 1, 2, 1, 1);
        constrain(p, new JLabel("AV reaction time: "), 0, 3, 1, 1);
        constrain(p, AVtau, 1, 3, 1, 1);
        
        constrain(panel, p, 0, 4, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, save, 0, 0, 1, 1);
        constrain(p, reset, 1, 0, 1, 1);
        constrain(panel, p, 0, 5, 2, 1);
        
        constrain(this, panel, 0, 0, 1, 1);
        
        reset();
    }
    
    public void reset()
    {
        data.setText("");
        
        
        ctm.setSelected(false);
        ltm.setSelected(false);
        
        if(project != null)
        {
            
            mesoDelta.setText("");
            timestep.setText(project.getOption("simulation-mesoscopic-step"));
            HVtau.setText(project.getOption("hv-reaction-time"));
            AVtau.setText(project.getOption("av-reaction-time"));
            
            // count link data
            int total = 0;
            int ctm = 0;
            int ltm = 0;
            int centroid = 0;
            
            for(Link l : project.getSimulator().getLinks())
            {
                if(l.isCentroidConnector())
                {
                    centroid++;
                }
                else if(l instanceof LTMLink)
                {
                    ltm++;
                }
                else if(l instanceof CTMLink)
                {
                    ctm++;
                }
                total ++;
            }
            
            data.append(total+"\tlinks\n\n");
            if(ctm > 0)
            {
                data.append(ctm+"\tCTM links\n");
            }
            if(ltm > 0)
            {
                data.append(ltm+"\tLTM links\n");
            }
            if(centroid > 0)
            {
                data.append(centroid+"\tcentroid connectors\n");
            }
            setEnabled(true);
            
            
        }
        else
        {
            setEnabled(false);
        }
    }
    
    public void setEnabled(boolean e)
    {
        e = e && project != null;
        ctm.setEnabled(e);
        ltm.setEnabled(e);
        save.setEnabled(e);
        reset.setEnabled(e);     
        mesoDelta.setEditable(e);
        timestep.setEditable(e);
        HVtau.setEditable(e);
        AVtau.setEditable(e);
        ctmOptions.setEnabled(e && ctm.isSelected());
        ltmOptions.setEnabled(e && ltm.isSelected());
        super.setEnabled(e);
    }
    
    public void setProject(Project project)
    {
        this.project = project;
        
        reset();
    }
    
    public void save()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                double mesodelta = -1;;
                
                try
                {
                   mesodelta = Double.parseDouble(mesoDelta.getText().trim());
                }
                catch(Exception ex)
                {
                }
                
                try
                {
                    project.setOption("simulation-mesoscopic-step", ""+Integer.parseInt(timestep.getText().trim()));
                }
                catch(Exception ex)
                {
                }
                
                try
                {
                    project.setOption("hv-reaction-time", ""+Double.parseDouble(HVtau.getText().trim()));
                }
                catch(Exception ex)
                {
                }
                
                try
                {
                    project.setOption("av-reaction-time", ""+Double.parseDouble(AVtau.getText().trim()));
                }
                catch(Exception ex)
                {
                }
                
                try
                {
                    project.writeOptions();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
        
                if(ctm.isSelected() || ltm.isSelected())
                {
                    int newtype = 0;

                    if(ctm.isSelected())
                    {
                        newtype = ReadNetwork.CTM;
                    }
                    else if(ltm.isSelected())
                    {
                        newtype = ReadNetwork.LTM;
                    }

                    if(ctm.isSelected())
                    {
                        if(ctmOptions.getSelectedItem().equals("DLR"))
                        {
                            newtype += ReadNetwork.DLR;
                        }
                    }
                    else if(ltm.isSelected())
                    {
                        if(ltmOptions.getSelectedItem().equals("CACC"))
                        {
                            newtype += ReadNetwork.CACC;
                        }
                    }

                    ArrayList<LinkRecord> temp = new ArrayList<LinkRecord>();
                    
                    LinkBusRule busRule = new LinkBusRule(project);
                    

                    try
                    {

                        Scanner filein = new Scanner(project.getLinksFile());
                        filein.nextLine();


                        while(filein.hasNext())
                        {
                            LinkRecord link = new LinkRecord(filein.nextLine());



                            if(link.getType()/100 != ReadNetwork.CENTROID/100)
                            {
                                link.setType(newtype);
                            }

                            if(ctm.isSelected() && ctmOptions.getSelectedItem().equals("Shared transit") && link.getNumLanes() > 1 && busRule.contains(link.getId()))
                            {
                                link.setType(link.getType()+ ReadNetwork.SHARED_TRANSIT);
                            }
                            
                            if(ctm.isSelected() && ctmOptions.getSelectedItem().equals("Split transit") && link.getNumLanes() > 1 && busRule.contains(link.getId()))
                            {
                                link.setType(link.getType()+ ReadNetwork.SPLIT_TRANSIT);
                            }
                            
                            if(mesodelta > 0)
                            {
                                link.setWavespd(link.getFFSpd() * mesodelta);
                            }

                            temp.add(link);
                        }

                        filein.close();

                        PrintStream fileout = new PrintStream(new FileOutputStream(project.getLinksFile()), true);

                        fileout.println(ReadNetwork.getLinksFileHeader());
                        
                        for(LinkRecord x : temp)
                        {
                            fileout.println(x);
                        }

                        fileout.close();
                        
                        project.loadSimulator();
                    }
                    catch(IOException ex)
                    {
                        GUI.handleException(ex);
                    }

                }

                parentReset();

                parentSetEnabled(true);
                
            }
        };
        t.start();
    }
 
    
}
