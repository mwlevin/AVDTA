/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import javax.swing.JPanel;
import static avdta.gui.GraphicUtils.*;
import avdta.network.ReadNetwork;
import avdta.network.link.CTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.Link;
import avdta.network.node.AuctionPolicy;
import avdta.network.node.BackPressureObj;
import avdta.network.node.Diverge;
import avdta.network.node.FCFSPolicy;
import avdta.network.node.Intersection;
import avdta.network.node.IntersectionControl;
import avdta.network.node.IntersectionPolicy;
import avdta.network.node.MCKSTBR;
import avdta.network.node.Merge;
import avdta.network.node.Node;
import avdta.network.node.ObjFunction;
import avdta.network.node.P0Obj;
import avdta.network.node.PhasedTBR;
import avdta.network.node.PriorityTBR;
import avdta.network.node.SignalWeightedTBR;
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
    private JButton save, reset;
    
    public NodesPane(NetworkPane parent)
    {
        this.parent = parent;
        data = new JTextArea(5, 30);
        data.setEditable(false);
        
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
                // update drop down menu
            }
        };
        
        signal.addChangeListener(change);
        reservation.addChangeListener(change);
        stop.addChangeListener(change);
        
        save = new JButton("Save");
        reset = new JButton("Reset");
        
        save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    save();
                }
                catch(IOException ex)
                {
                    DTAGUI.handleException(ex);
                }
            }
        });
        
        reset.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                reset();
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
        
        constrain(this, p, 0, 2, 2, 1);
        
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, save, 0, 0, 1, 1);
        constrain(p, reset, 1, 0, 1, 1 );
        constrain(this, p, 0, 4, 2, 1);
        
        
        
        
        reset();
    }
    
    public void reset()
    {
        signal.setSelected(false);
        stop.setSelected(false);
        reservation.setSelected(false);
        
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
            
            
            data.setText("");
            
            data.append(total+"\tnodes\n");
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
                data.append(reservationCount + "\treservations\n");
                
                if(fcfsCount > 0)
                {
                    data.append("\t"+fcfsCount+"\tFCFS\n");
                }
                if(auctionCount > 0)
                {
                    data.append("\t"+auctionCount+"\tauctions\n");
                }
                if(pressureCount > 0)
                {
                    data.append("\t"+pressureCount+"\tbackpressure\n");
                }
                if(p0Count > 0)
                {
                    data.append("\t"+p0Count+"\tP0\n");
                }
                if(weightedCount > 0)
                {
                    data.append("\t"+weightedCount+"\tweighted\n");
                }
                if(phasedCount > 0)
                {
                    data.append("\t"+phasedCount+"\tphased\n");
                }
            }
            
            enable();
        }
        else
        {
            HVsUseReservations.setSelected(false);
            disable();
        }
        
    }
    
    public void save() throws IOException
    {
        
    }
    
    public void enable()
    {
        save.setEnabled(true);
        reset.setEnabled(true);
        signal.setEnabled(true);
        stop.setEnabled(true);
        reservation.setEnabled(true);
        HVsUseReservations.setEnabled(true);
    }
    public void disable()
    {
        save.setEnabled(false);
        reset.setEnabled(false);
        signal.setEnabled(false);
        stop.setEnabled(false);
        reservation.setEnabled(false);
        HVsUseReservations.setEnabled(false);
        
    }
    
    public void setProject(Project project)
    {
        this.project = project;
        
        reset();
    }
}
