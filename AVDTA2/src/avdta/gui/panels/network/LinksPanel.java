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
import avdta.network.link.CACCLTMLink;
import avdta.network.link.CTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.link.LinkRecord;
import avdta.network.link.SharedTransitCTMLink;
import avdta.network.link.SplitCTMLink;
import avdta.network.type.ExtendedType;
import avdta.network.type.Type;
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
import java.util.List;
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
    

    
    private JRadioButton[] types;
    private JComboBox[] options;
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
        
        types = new JRadioButton[ReadNetwork.LINK_OPTIONS.length];
        options = new JComboBox[ReadNetwork.LINK_OPTIONS.length];
        
        ButtonGroup group = new ButtonGroup();
        for(int i = 0; i < types.length; i++)
        {
            types[i] = new JRadioButton(ReadNetwork.LINK_OPTIONS[i].toString());
            group.add(types[i]);
            
            List<Type> temp = new ArrayList<Type>();
            
            temp.add(ReadNetwork.LINK_OPTIONS[i]);
            
            for(ExtendedType type : ReadNetwork.LINK_EXT_OPTIONS)
            {
                if(type.getBase() == ReadNetwork.LINK_OPTIONS[i])
                {
                    temp.add(type);
                }
            }
            
            if(temp.size() > 1)
            {
                options[i] = new JComboBox(temp.toArray());
            }
        }

        
        mesoDelta = new JTextField(5);
        timestep = new JTextField(5);


        
        
        
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
        
        for(int i = 0; i < types.length; i++)
        {
            if(options[i] != null)
            {
                final int idx = i;

                types[i].addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent e)
                    {
                        options[idx].setEnabled(types[idx].isSelected());
                    }
                });
            }
        }

        
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
        
        for(int i = 0; i < types.length; i++)
        {
            constrain(p, types[i], 0, 1+i, 1, 1);
            
            if(options[i] != null)
            {
                constrain(p, options[i], 1, 1+i, 1, 1);
            }
        }
        
        
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
        
        for(JRadioButton btn : types)
        {
            btn.setSelected(false);
        }

        
        if(project != null)
        {
            
            mesoDelta.setText("");
            timestep.setText(project.getOption("simulation-mesoscopic-step"));
            HVtau.setText(project.getOption("hv-reaction-time"));
            AVtau.setText(project.getOption("av-reaction-time"));
            
            // count link data
            int total = 0;
            
            int[] totals = new int[ReadNetwork.LINK_TYPES.length];
            int[] counts = new int[ReadNetwork.LINK_EXT_TYPES.length];
            
            for(Link l : project.getSimulator().getLinks())
            {
                Type type = l.getType();
                Type base = type.getBase();
                
                for(int t = 0; t < ReadNetwork.LINK_TYPES.length; t++)
                {
                    if(ReadNetwork.LINK_TYPES[t] == base)
                    {
                        totals[t]++;
                    }
                }
                
                if(type instanceof ExtendedType)
                {
                    for(int t = 0; t < ReadNetwork.LINK_EXT_TYPES.length; t++)
                    {
                        if(type == ReadNetwork.LINK_EXT_TYPES[t])
                        {
                            counts[t] ++;
                        }
                    }
                }
                
                total ++;
            }
            
            data.append(total+"\tlinks\n\n");
            
            for(int i = 0; i < totals.length; i++)
            {
                if(totals[i] > 0)
                {
                    data.append(totals[i]+"\t"+ReadNetwork.LINK_TYPES[i].getDescription()+"\n");
                }
            }
            data.append("\n");
            
            for(int i = 0; i < counts.length; i++)
            {
                if(counts[i] > 0)
                {
                    data.append(counts[i]+"\t"+ReadNetwork.LINK_EXT_TYPES[i].getDescription()+"\n");
                }
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
        
        save.setEnabled(e);
        reset.setEnabled(e);     
        mesoDelta.setEditable(e);
        timestep.setEditable(e);
        HVtau.setEditable(e);
        AVtau.setEditable(e);
        
        for(int i = 0; i < options.length; i++)
        {
            types[i].setEnabled(e);
            if(options[i] != null)
            {
                options[i].setEnabled(e && types[i].isSelected());
            }
        }

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
        
                Type newtype = null;
                
                for(int i = 0; i < types.length; i++)
                {
                    if(types[i].isSelected())
                    {
                        if(options[i] != null)
                        {
                            newtype = (Type)options[i].getSelectedItem();
                        }
                        else
                        {
                            newtype = ReadNetwork.LINK_OPTIONS[i];
                        }
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



                        if(link.getType()/100 != ReadNetwork.CENTROID.getCode()/100)
                        {
                            if(newtype.isValid(link))
                            {
                                link.setType(newtype);
                            }
                            else if(newtype instanceof ExtendedType)
                            {
                                link.setType(((ExtendedType)newtype).getBase());
                            }
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

            

                parentReset();

                parentSetEnabled(true);
                
            }
        };
        t.start();
    }
 
    
}
