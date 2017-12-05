/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.network;

import avdta.gui.DTAGUI;
import avdta.gui.GUI;
import avdta.gui.panels.GUIPanel;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.gui.util.StatusBar;
import avdta.util.DownloadElevation;
import avdta.network.ReadNetwork;
import avdta.network.link.CTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.node.policy.AuctionPolicy;
import avdta.network.node.obj.BackPressureObj;
import avdta.network.node.Diverge;
import avdta.network.node.policy.FCFSPolicy;
import avdta.network.node.Intersection;
import avdta.network.node.IntersectionControl;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.network.node.policy.MCKSTBR;
import avdta.network.node.Merge;
import avdta.network.node.Node;
import avdta.network.node.NodeRecord;
import avdta.network.node.obj.ObjFunction;
import avdta.network.node.obj.P0Obj;
import avdta.network.node.PhasedTBR;
import avdta.network.node.PriorityTBR;
import avdta.network.node.policy.SignalWeightedTBR;
import avdta.network.node.StopSign;
import avdta.network.node.TrafficSignal;
import avdta.network.node.Zone;
import avdta.network.node.obj.MaxPressureObj;
import avdta.network.node.policy.EmergencyPolicy;
import avdta.network.node.policy.TransitFirst;
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
import javax.swing.JCheckBox;
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
public class NodesPanel extends GUIPanel
{
    private Project project;
    
    private JTextArea data;
    
    private JCheckBox HVsUseReservations;
    private JRadioButton[] types;
    private JComboBox[] options;
    
    
    private JButton save, reset, download;
    
    private StatusBar update;
    
    private static final String[] NODE_OPTIONS = new String[]{"FCFS", "backpressure", "P0", "Phased", "Signal-weighted", "Transit-FCFS", "Emergency-FCFS"};

    
    public NodesPanel(NetworkPanel parent)
    {
        super(parent);
        data = new JTextArea(5, 20);
        data.setEditable(false);
        

        
        HVsUseReservations = new JCheckBox("HVs use reservations");
        
        types =  new JRadioButton[ReadNetwork.NODE_OPTIONS.length];
        options = new JComboBox[ReadNetwork.NODE_EXT_OPTIONS.length];
        
        ButtonGroup group = new ButtonGroup();
        for(int i = 0; i < types.length; i++)
        {
            types[i] = new JRadioButton(ReadNetwork.NODE_OPTIONS[i].getDescription());
            group.add(types[i]);
            
            List<Type> temp = new ArrayList<Type>();
            
            temp.add(ReadNetwork.NODE_OPTIONS[i]);
            
            for(Type type : ReadNetwork.NODE_EXT_OPTIONS)
            {
                if(type.getBase() == ReadNetwork.NODE_OPTIONS[i])
                {
                    temp.add(type);
                }
            }
            

            if(temp.size() > 1)
            {
                options[i] = new JComboBox(temp.toArray());
            }
        }

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
        
        save = new JButton("Save");
        reset = new JButton("Reset");
        
        download = new JButton("Download elevation");
        update = new StatusBar();
        
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
        
        download.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                downloadElevation();
            }
        });
        
        
        setLayout(new GridBagLayout());
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nodes"));
        
        JScrollPane scroll = new JScrollPane(data);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        constrain(panel, scroll, 0, 1, 2, 1);
       
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JLabel("Intersection control"), 0, 0, 1, 1);
        
        for(int i = 0; i < types.length; i++)
        {
            constrain(p, types[i], 0, 1+i, 1, 1);
            if(options[i] != null)
            {
                constrain(p, options[i], 1, 1+i, 1, 1);
            }
        }

        
        constrain(panel, p, 0, 2, 2, 1);
        
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, save, 0, 0, 1, 1);
        constrain(p, reset, 1, 0, 1, 1 );
        constrain(panel, p, 0, 4, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, download, 0, 0, 1, 1);
        constrain(p, update, 0, 1, 1, 1);
        constrain(panel, p, 0, 5, 2, 1);
        
        constrain(this, panel, 0, 0, 1, 1);
        
        reset();
    }
    
 
    public void reset()
    {
        for(JRadioButton btn : types)
        {
            btn.setSelected(false);
        }
        
        data.setText("");
        
        if(project != null)
        {
            HVsUseReservations.setSelected(project.getOption("hvs-use-reservations").equals("true"));
            
            int total = 0;
            int[] totals = new int[ReadNetwork.NODE_TYPES.length];
            int[] counts = new int[ReadNetwork.NODE_EXT_TYPES.length];
            
            for(Node n : project.getSimulator().getNodes())
            {
                total++;
                
                Type type = n.getType();
                
                for(int i = 0; i < ReadNetwork.NODE_TYPES.length; i++)
                {
                    if(type.getBase() == ReadNetwork.NODE_TYPES[i])
                    {
                        totals[i]++;
                    }
                }
                
                for(int i = 0; i < ReadNetwork.NODE_EXT_TYPES.length; i++)
                {
                    if(type == ReadNetwork.NODE_EXT_TYPES[i])
                    {
                        counts[i]++;
                    }
                }
               
            }
            
            
            
            
            data.append(total+"\tnodes\n\n");
            
            for(int i = 0; i < totals.length; i++)
            {
                if(totals[i] > 0)
                {
                    data.append(totals[i]+"\t"+ReadNetwork.NODE_TYPES[i].getDescription()+"\n");
                }
            }
            data.append("\n");
            
            for(int i = 0; i < counts.length; i++)
            {
                if(counts[i] > 0)
                {
                    data.append(counts[i]+"\t"+ReadNetwork.NODE_EXT_TYPES[i].getDescription()+"\n");
                }
            }
            
            setEnabled(true);
        }
        else
        {
            HVsUseReservations.setSelected(false);
            setEnabled(false);
        }
        
    }
    
    public void downloadElevation()
    {
        parentSetEnabled(false);
        
        final JPanel panel = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    DownloadElevation dl = new DownloadElevation(update);
                    dl.download(panel, project);
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
    
    public void save()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                ArrayList<NodeRecord> temp = new ArrayList<NodeRecord>();
        
                try
                {
                    Scanner filein = new Scanner(project.getNodesFile());
                    filein.nextLine();

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
                                newtype = ReadNetwork.NODE_OPTIONS[i];
                            }
                        }
                    }
                    

                    Map<Integer, Node> nodes = project.getSimulator().createNodeIdsMap();
                    
                    while(filein.hasNextInt())
                    {
                        NodeRecord node = new NodeRecord(filein.nextLine());
                        
                        Node n = nodes.get(node.getId());

                        if(node.getType()/100 != ReadNetwork.CENTROID.getCode()/100)
                        {
                            if(n.getIncoming().size() == 1)
                            {
                                if(n.getOutgoing().size() == 1)
                                {
                                    node.setType(ReadNetwork.CONNECTOR);
                                }
                                else
                                {
                                    node.setType(ReadNetwork.DIVERGE);
                                }
                            }
                            else if(n.getOutgoing().size() == 1)
                            {
                                node.setType(ReadNetwork.MERGE);
                            }
                            else
                            {
                                node.setType(newtype);
                            }
                        }

                        temp.add(node);
                    }
                    filein.close();

                    PrintStream fileout = new PrintStream(new FileOutputStream(project.getNodesFile()), true);
                    fileout.println(ReadNetwork.getNodesFileHeader());

                    for(NodeRecord x : temp)
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
    
    public void setEnabled(boolean e)
    {
        e = e && project != null;
        save.setEnabled(e);
        reset.setEnabled(e);
        
        for(int i = 0; i < types.length; i++)
        {
            types[i].setEnabled(e);
            
            if(options[i] != null)
            {
                options[i].setEnabled(e && types[i].isSelected());
            }
        }

        HVsUseReservations.setEnabled(e);

        download.setEnabled(e);
        super.setEnabled(e);
    }
    
    public void setProject(Project project)
    {
        this.project = project;
        
        reset();
    }
}
