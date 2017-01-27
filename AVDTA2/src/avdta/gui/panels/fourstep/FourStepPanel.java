/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.fourstep;

import avdta.dta.Assignment;
import avdta.dta.DTAResults;
import avdta.dta.DTASimulator;
import avdta.fourstep.FourStepSimulator;
import avdta.gui.GUI;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.GUIPanel;
import static avdta.gui.util.GraphicUtils.constrain;
import avdta.gui.util.StatusBar;
import avdta.project.DTAProject;
import avdta.project.FourStepProject;
import avdta.vehicle.DriverType;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author micha
 */
public class FourStepPanel extends GUIPanel
{
    
    private FourStepProject project;
    
    private JButton run;

    
    private JTextField dta_max_iter, dta_min_gap, partial_demand;
    private JTextField fs_max_iter;
    private JTextArea data;
    private JCheckBox repos, allAVs;
    private JButton update;
    
    private StatusBar status;
    
    public FourStepPanel(AbstractGUIPanel parent)
    {
        super(parent);
        
        dta_max_iter = new JTextField(5);       
        partial_demand = new JTextField(5);
        dta_min_gap = new JTextField(5);
        data = new JTextArea(10, 20);
        data.setEditable(false);
        
        fs_max_iter = new JTextField(5);
        
        status = new StatusBar();
        run = new JButton("Run four-step");
 
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                run();
            }
        });
        
        repos = new JCheckBox("Allow repositioning?");
        allAVs = new JCheckBox("100% AVs");
        update = new JButton("Update");
        
        update.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                project.setFourStepOption("all-avs", ""+allAVs.isSelected());
                project.setFourStepOption("allow-repositioning", ""+repos.isSelected());
                
                FourStepSimulator sim = project.getSimulator();
                
                sim.setAllAVs(allAVs.isSelected());
                sim.setAllowRepositioning(repos.isSelected());
                
                try
                {
                    project.writeOptions();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
                
                
            }
        });

        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        
        JScrollPane scroll = new JScrollPane(data);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        constrain(panel, scroll, 0, 0, 1, 1);
        
        JPanel p3 = new JPanel();
        p3.setLayout(new GridBagLayout());
        p3.setBorder(BorderFactory.createTitledBorder("Options"));
        
        constrain(p3, repos, 0, 0, 1, 1);
        constrain(p3, allAVs, 0, 1, 1, 1);
        constrain(p3, update, 0, 2, 0, 1);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder("DTA"));
        
        constrain(p2, new JLabel("Partial demand: "), 0, 0, 1, 1);
        constrain(p2, partial_demand, 1, 0, 1, 1);
        constrain(p2, new JLabel("Max. iteration:"), 0, 1, 1, 1);
        constrain(p2, dta_max_iter, 1, 1, 1, 1);
        constrain(p2, new JLabel("Min. gap:"), 0, 2, 1, 1);
        constrain(p2, dta_min_gap, 1, 2, 1, 1);
        
        constrain(p, p3, 0, 0, 1, 1);
        constrain(p, p2, 0, 1, 1, 1);
        
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        
        constrain(p1, new JLabel("Four-step iterations: "), 0, 0, 1, 1);
        constrain(p1, fs_max_iter, 1, 0, 1, 1);
        constrain(p1, run, 0, 1, 2, 1, GridBagConstraints.CENTER);
        
        constrain(p1, status, 0, 2, 2, 1, GridBagConstraints.CENTER);
        
        constrain(p, p1, 0, 2, 1, 1);
        
        
        p2.setPreferredSize(new Dimension((int)p.getPreferredSize().getWidth(), (int)p2.getPreferredSize().getHeight()));
        p3.setPreferredSize(new Dimension((int)p.getPreferredSize().getWidth(), (int)p3.getPreferredSize().getHeight()));
        
        constrain(panel, p, 1, 0, 1, 1);
        
        
        add(panel);
        
        setEnabled(false);
    }
    
    public void setEnabled(boolean e)
    {
        run.setEnabled(e);
        dta_min_gap.setEditable(e);
        dta_max_iter.setEditable(e);
        fs_max_iter.setEditable(e);
        partial_demand.setEditable(e);
        repos.setEnabled(e);
        allAVs.setEnabled(e);
        update.setEnabled(e);
        super.setEnabled(e);
    }
    
    public void run()
    {
        try
        {
            Integer.parseInt(fs_max_iter.getText().trim());
        }
        catch(Exception ex)
        {
            fs_max_iter.setText("");
            fs_max_iter.requestFocus();
            return;
        }
        
        try
        {
            Integer.parseInt(dta_max_iter.getText().trim());
        }
        catch(Exception ex)
        {
            dta_max_iter.setText("");
            dta_max_iter.requestFocus();
            return;
        }
        
        try
        {
            Integer.parseInt(partial_demand.getText().trim());
        }
        catch(Exception ex)
        {
            partial_demand.setText("");
            partial_demand.requestFocus();
            return;
        }
        
        try
        {
            Double.parseDouble(dta_min_gap.getText().trim());
        }
        catch(Exception ex)
        {
            dta_min_gap.setText("");
            dta_min_gap.requestFocus();
            return;
        }
        
        
        parentSetEnabled(false);
        
        
        final JPanel panel = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    
                    DTAResults results;
                    
                    FourStepSimulator sim = project.getSimulator();
                    sim.setStatusUpdate(status);
                    
                    int pd = Integer.parseInt(partial_demand.getText().trim());
                    
                    int max = Integer.parseInt(dta_max_iter.getText().trim());
                    double gap = Double.parseDouble(dta_min_gap.getText().trim());
                    int fs_max = Integer.parseInt(fs_max_iter.getText().trim());
                    
                    
                    results = sim.four_step(fs_max, max, pd, gap);
                   
                    
                    double tstt = sim.getTSTT() / 3600.0;
                    double avg = tstt * 60 / sim.getNumVehicles();
                    
                    JOptionPane.showMessageDialog(panel, "Iterations: "+(sim.getIteration()-1)+"\nGap: "+String.format("%.2f", results.getGapPercent())+
                            "\nNon-exiting: "+results.getNonExiting()+"\t"+
                            "\nTSTT: "+String.format("%.1f", tstt)+" hr\nAvg. time: "+String.format("%.2f", avg)+" min\n"+
                            "HV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.HV)/60)+" min\n"+
                            "AV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.AV)/60)+" min",
                            "DTA complete", JOptionPane.PLAIN_MESSAGE);

                    

                    Assignment assign = sim.getAssignment();
                    sim.printCosts(new File(assign.getAssignmentDirectory()+"/od_costs.txt"));
                    sim.printDemand(new File(assign.getAssignmentDirectory()+"/od_demand.txt"));
                    System.out.println(assign.getAssignmentDirectory());
                    
                    status.update(0, 0, "");
                    status.resetTime();
                    
                    showResults(results);
                    
                    
                
                }
                catch(Exception ex)
                {
                    GUI.handleException(ex);
                }
                
                parentReset();
                parentSetEnabled(true);
            }
        };
        t.start();
    }
    
    public void reset()
    {
        if(project != null)
        {
            fs_max_iter.setText("15");
            dta_max_iter.setText("30");
            dta_min_gap.setText("1");
            partial_demand.setText("0");
            allAVs.setSelected(project.getFourStepOption("all-avs").equalsIgnoreCase("true"));
            repos.setSelected(project.getFourStepOption("allow-repositioning").equalsIgnoreCase("true"));
            setEnabled(true);
        }
        else
        {
            fs_max_iter.setText("");
            dta_max_iter.setText("");
            dta_min_gap.setText("");
            partial_demand.setText("");
            setEnabled(false);
        }
    }
    
    public void setProject(FourStepProject project)
    {
        this.project = project;
        
        data.setText("");
        
        reset();
        
        
    }
    
    public void showResults(DTAResults results)
    {
        data.setText("");
        data.append("Gap:\t"+String.format("%.2f", results.getGapPercent())+"%\n");
        data.append("TSTT:\t"+String.format("%.1f", results.getTSTT())+" hr\n");
        data.append("Avg. TT:\t"+String.format("%.2f", results.getAvgTT())+" min\n");
        data.append("Non-exit:\t"+results.getNonExiting()+"\n");
        data.append("Num. vehicles:\t"+results.getTrips());
        
    }
}
