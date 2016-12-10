/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.fourstep;

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        JScrollPane scroll = new JScrollPane(data);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        constrain(panel, scroll, 0, 0, 1, 1);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder("DTA"));
        
        constrain(p2, new JLabel("Partial demand: "), 0, 0, 1, 1);
        constrain(p2, partial_demand, 1, 0, 1, 1);
        constrain(p2, new JLabel("Max. iteration:"), 0, 1, 1, 1);
        constrain(p2, dta_max_iter, 1, 1, 1, 1);
        constrain(p2, new JLabel("Min. gap:"), 0, 2, 1, 1);
        constrain(p2, dta_min_gap, 1, 2, 1, 1);
        
        constrain(p, p2, 0, 0, 2, 1);
        
        constrain(p, new JLabel("Four-step iterations: "), 0, 1, 1, 1);
        constrain(p, fs_max_iter, 1, 1, 1, 1);
        constrain(p, run, 0, 2, 2, 1, GridBagConstraints.CENTER);
        
        constrain(p, status, 0, 3, 2, 1, GridBagConstraints.CENTER);
        
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
                    
                    
                    results = sim.four_step(fs_max, max, pd, gap, new File(project.getResultsFolder()+"/fourstep_log.txt"));
                   
                    
                    double tstt = sim.getTSTT() / 3600.0;
                    double avg = tstt * 60 / sim.getNumVehicles();
                    
                    JOptionPane.showMessageDialog(panel, "Iterations: "+(sim.getIteration()-1)+"\nGap: "+String.format("%.2f", results.getGapPercent())+
                            "\nNon-exiting: "+results.getNonExiting()+"\t"+
                            "\nTSTT: "+String.format("%.1f", tstt)+" hr\nAvg. time: "+String.format("%.2f", avg)+" min\n"+
                            "HV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.HV)/60)+" min\n"+
                            "AV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.AV)/60)+" min",
                            "DTA complete", JOptionPane.PLAIN_MESSAGE);

                    showResults(results);
                    
                    status.update(0, 0, "");
                    status.resetTime();

                
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
        dta_max_iter.setText("30");
        fs_max_iter.setText("1");
        dta_min_gap.setText("1");
    }
    
    public void setProject(FourStepProject project)
    {
        this.project = project;
        
        data.setText("");
        
        if(project != null)
        {
            fs_max_iter.setText("15");
            dta_max_iter.setText("30");
            dta_min_gap.setText("1");
            partial_demand.setText("0");
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
    
    public void showResults(DTAResults results)
    {
        data.setText("");
        data.append("Gap:\t"+String.format("%.2f", results.getGapPercent())+"%\n");
        data.append("TSTT:\t"+String.format("%.1f", results.getTSTT())+" hr\n");
        data.append("Avg. TT:\t"+String.format("%.2f", results.getAvgTT())+" min\n");
        data.append("Non-exit:\t"+results.getNonExiting());
        
    }
}
