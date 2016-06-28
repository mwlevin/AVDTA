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
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author micha
 */
public class LinksPane extends JPanel
{
    private Project project;
    private NetworkPane parent;
    
    private JTextArea data;
    
    private JRadioButton ctm, ltm;
    private JButton save, reset;
    
    
    private JTextField HVtau, AVtau;
    
    private JTextField mesoDelta;
    private JTextField timestep;
    
    public LinksPane(NetworkPane parent)
    {
        this.parent = parent;
        
        data = new JTextArea(5, 30);
        data.setEditable(false);
        
        HVtau = new JTextField(5);
        AVtau = new JTextField(5);
        
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
        
        constrain(this, new JLabel("Links"), 0, 0, 2, 1);
        constrain(this, new JScrollPane(data), 0, 1, 2, 1);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, new JLabel("Flow model"), 0, 0, 1, 1);
        constrain(p, ctm, 0, 1, 1, 1);
        constrain(p, ltm, 0, 2, 1, 1);
        constrain(this, p, 0, 3, 1, 1);
        
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
        
        constrain(this, p, 1, 3, 1, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, save, 0, 0, 1, 1);
        constrain(p, reset, 1, 0, 1, 1);
        constrain(this, p, 0, 4, 2, 1);
        
        
        
        reset();
    }
    
    public void reset()
    {
        data.setText("");
        
        
        ctm.setSelected(false);
        ltm.setSelected(false);
        
        if(project != null)
        {
            
            mesoDelta.setText(project.getOption("simulation-mesoscopic-delta"));
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
            
            data.append(total+"\tlinks\n");
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
            enable();
            
            
        }
        else
        {
            disable();
        }
    }
    
    public void disable()
    {
        save.setEnabled(false);
        reset.setEnabled(false);
        ctm.setEnabled(false);
        ltm.setEnabled(false);
        mesoDelta.setText("");
        mesoDelta.setEditable(false);
        timestep.setText("");
        timestep.setEditable(false);
        HVtau.setText("");
        HVtau.setEditable(false);
        AVtau.setText("");
        AVtau.setEditable(false);
    }
    
    public void enable()
    {
        ctm.setEnabled(true);
        ltm.setEnabled(true);
        save.setEnabled(true);
        reset.setEnabled(true);     
        mesoDelta.setEditable(true);
        timestep.setEditable(true);
        HVtau.setEditable(true);
        AVtau.setEditable(true);
    }
    
    public void setProject(Project project)
    {
        this.project = project;
        
        reset();
    }

    public void save() throws IOException
    {
        parent.disable();
        
        
        project.setOption("simulation-mesoscopic-delta", ""+Double.parseDouble(mesoDelta.getText().trim()));
        project.setOption("simulation-mesoscopic-step", ""+Integer.parseInt(timestep.getText().trim()));
        project.setOption("hv-reaction-time", ""+Double.parseDouble(HVtau.getText().trim()));
        project.setOption("av-reaction-time", ""+Double.parseDouble(AVtau.getText().trim()));
        
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
            
            ArrayList<String> temp = new ArrayList<String>();
            
            Scanner filein = new Scanner(project.getLinksFile());
        
            temp.add(filein.nextLine());

            while(filein.hasNext())
            {
                int id = filein.nextInt();
                int type = filein.nextInt();
                int source_id = filein.nextInt();
                int dest_id = filein.nextInt();
                double length = filein.nextDouble() / 5280;
                double ffspd = filein.nextDouble();
                double capacity = filein.nextDouble();
                int numLanes = filein.nextInt();
                double jamd = 5280.0/Vehicle.vehicle_length;

                filein.nextLine();
                
                if(type != ReadNetwork.CENTROID)
                {
                    type = newtype;
                }
                
                temp.add(""+id+"\t"+type+"\t"+source_id+"\t"+dest_id+"\t"+length+"\t"+ffspd+"\t"+capacity+"\t"+numLanes+"\t"+jamd);
            }
            
            filein.close();
            
            PrintStream fileout = new PrintStream(new FileOutputStream(project.getLinksFile()), true);
            
            for(String x : temp)
            {
                fileout.println(x);
            }
            
            fileout.close();
            
        }
        
        project.loadSimulator();
        parent.reset();
        
        parent.enable();
    }
 
    
}
