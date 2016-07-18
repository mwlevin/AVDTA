/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.gui.DTAGUI;
import avdta.gui.GUI;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.gui.util.StatusBar;
import avdta.network.DownloadElevation;
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
import avdta.network.node.obj.ObjFunction;
import avdta.network.node.obj.P0Obj;
import avdta.network.node.PhasedTBR;
import avdta.network.node.PriorityTBR;
import avdta.network.node.policy.SignalWeightedTBR;
import avdta.network.node.StopSign;
import avdta.network.node.TrafficSignal;
import avdta.network.node.Zone;
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
public class NodesPane extends JPanel
{
    private NetworkPane parent;
    private Project project;
    
    private JTextArea data;
    
    private JCheckBox HVsUseReservations;
    private JRadioButton signal, stop, reservation;
    private JButton save, reset, download;
    
    private StatusBar update;
    
    private static final String[] NODE_OPTIONS = new String[]{"FCFS", "backpressure", "P0", "Phased", "Signal weighted"};
    private JComboBox<String> nodeOptions;
    
    public NodesPane(NetworkPane parent)
    {
        this.parent = parent;
        data = new JTextArea(5, 20);
        data.setEditable(false);
        
        nodeOptions = new JComboBox<String>(NODE_OPTIONS);
        
        HVsUseReservations = new JCheckBox("HVs use reservations");
        
        signal = new JRadioButton("Signals");
        reservation = new JRadioButton("Reservations");
        stop = new JRadioButton("Stop sign");
        
        
        
        ButtonGroup group = new ButtonGroup();
        group.add(signal);
        group.add(reservation);
        group.add(stop);
        
        ChangeListener change = new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                nodeOptions.setEnabled(reservation.isSelected());
            }
        };

        reservation.addChangeListener(change);
        
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
        
        constrain(this, new JLabel("Nodes"), 0, 0, 2, 1);
        constrain(this, new JScrollPane(data), 0, 1, 2, 1);
       
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JLabel("Intersection control"), 0, 0, 1, 1);
        constrain(p, signal, 0, 1, 1, 1);
        constrain(p, reservation, 0, 2, 1, 1);
        constrain(p, stop, 0, 3, 1, 1);
        constrain(p, nodeOptions, 1, 2, 1, 2);
        
        constrain(this, p, 0, 2, 2, 1);
        
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, save, 0, 0, 1, 1);
        constrain(p, reset, 1, 0, 1, 1 );
        constrain(this, p, 0, 4, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, download, 0, 0, 1, 1);
        constrain(p, update, 0, 1, 1, 1);
        constrain(this, p, 0, 5, 2, 1);
        
        
        
        reset();
    }
    
    public void reset()
    {
        signal.setSelected(false);
        stop.setSelected(false);
        reservation.setSelected(false);
        
        data.setText("");
        
        if(project != null)
        {
            HVsUseReservations.setSelected(project.getOption("hvs-use-reservations").equals("true"));
            
            int signalCount = 0;
            int stopCount = 0;
            int reservationCount = 0;
            int centroidCount = 0;
            int mergeCount = 0;
            int divergeCount = 0;
            int total = project.getSimulator().getNodes().size();
            
            int pressureCount = 0;
            int fcfsCount = 0;
            int phasedCount = 0;
            int weightedCount = 0;
            int p0Count = 0;
            int auctionCount = 0;
            
            for(Node n : project.getSimulator().getNodes())
            {
                if(n instanceof Zone)
                {
                    centroidCount++;
                }
                else
                {
                    IntersectionControl c = ((Intersection)n).getControl();
                    
                    if(c instanceof Merge)
                    {
                        mergeCount++;
                    }
                    else if(c instanceof Diverge)
                    {
                        divergeCount++;
                    }
                    else if(c instanceof StopSign)
                    {
                        stopCount++;
                    }
                    else if(c instanceof TrafficSignal)
                    {
                        signalCount++;
                    }
                    else if(c instanceof MCKSTBR)
                    {
                        reservationCount++;
                        ObjFunction func = ((MCKSTBR)c).getObj();
                     
                        if(func instanceof BackPressureObj)
                        {
                            pressureCount++;
                        }
                        else if(func instanceof P0Obj)
                        {
                            p0Count++;
                        }
                    }
                    else if(c instanceof PhasedTBR)
                    {
                        reservationCount++;
                        phasedCount++;
                    }
                    else if(c instanceof SignalWeightedTBR)
                    {
                        reservationCount++;
                        weightedCount++;
                    }
                    else if(c instanceof PriorityTBR)
                    {
                        reservationCount++;
                        
                        IntersectionPolicy policy = ((PriorityTBR)c).getPolicy();
                        
                        if(policy instanceof FCFSPolicy)
                        {
                            fcfsCount++;
                        }
                        else if(policy instanceof AuctionPolicy)
                        {
                            auctionCount++;
                        }
                    }
                    
                }
               
            }
            
            
            
            
            data.append(total+"\tnodes\n\n");
            if(centroidCount > 0)
            {
                data.append(total+"\tcentroids\n");
            }
            if(signalCount > 0)
            {
                data.append(signalCount+"\tsignals\n");
            }
            if(stopCount > 0)
            {
                data.append(stopCount+"\tstop signs\n");
            }
            if(reservationCount > 0)
            {
                data.append(reservationCount + "\treservations\n\n");
                
                if(fcfsCount > 0)
                {
                    data.append(fcfsCount+"\tFCFS\n");
                }
                if(auctionCount > 0)
                {
                    data.append(auctionCount+"\tauctions\n");
                }
                if(pressureCount > 0)
                {
                    data.append(pressureCount+"\tbackpressure\n");
                }
                if(p0Count > 0)
                {
                    data.append(p0Count+"\tP0\n");
                }
                if(weightedCount > 0)
                {
                    data.append(weightedCount+"\tweighted\n");
                }
                if(phasedCount > 0)
                {
                    data.append(phasedCount+"\tphased\n");
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
        parent.setEnabled(false);
        
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
                
                parent.setEnabled(true);
            }
        };
        t.start();
    }
    
    public void save()
    {
        parent.setEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                ArrayList<String> temp = new ArrayList<String>();
        
                try
                {
                    Scanner filein = new Scanner(project.getNodesFile());

                    temp.add(filein.nextLine());

                    int newtype = 0;

                    if(signal.isSelected())
                    {
                        newtype = ReadNetwork.SIGNAL;
                    }
                    else if(stop.isSelected())
                    {
                        newtype = ReadNetwork.STOPSIGN;
                    }
                    else if(reservation.isSelected())
                    {
                        newtype = ReadNetwork.RESERVATION;

                        if(nodeOptions.getSelectedItem().equals("FCFS"))
                        {
                            newtype += ReadNetwork.FCFS;
                        }
                        else if(nodeOptions.getSelectedItem().equals("backpressure"))
                        {
                            newtype += ReadNetwork.PRESSURE;
                        }
                        else if(nodeOptions.getSelectedItem().equals("P0"))
                        {
                            newtype += ReadNetwork.P0;
                        }
                        else if(nodeOptions.getSelectedItem().equals("Phased"))
                        {
                            newtype += ReadNetwork.PHASED;
                        }
                        else if(nodeOptions.getSelectedItem().equals("Signal weighted"))
                        {
                            newtype += ReadNetwork.WEIGHTED;
                        }
                    }

                    while(filein.hasNextInt())
                    {
                        int id = filein.nextInt();
                        int type = filein.nextInt();
                        double x = filein.nextDouble();
                        double y = filein.nextDouble();
                        double elevation = filein.nextDouble();
                        String line = filein.nextLine();

                        if(type != ReadNetwork.CENTROID)
                        {
                            type = newtype;
                        }

                        temp.add(id+"\t"+type+"\t"+x+"\t"+y+"\t"+elevation+line);
                    }
                    filein.close();

                    PrintStream fileout = new PrintStream(new FileOutputStream(project.getNodesFile()), true);

                    for(String x : temp)
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
                
                
                parent.reset();

                parent.setEnabled(true);
            }
        };
        t.start();
        
        
        
        
        
    }
    
    public void setEnabled(boolean e)
    {
        save.setEnabled(e);
        reset.setEnabled(e);
        signal.setEnabled(e);
        stop.setEnabled(e);
        reservation.setEnabled(e);
        HVsUseReservations.setEnabled(e);
        nodeOptions.setEnabled(e && reservation.isSelected());
        download.setEnabled(e);
        super.setEnabled(e);
    }
    
    public void setProject(Project project)
    {
        this.project = project;
        
        reset();
    }
}
