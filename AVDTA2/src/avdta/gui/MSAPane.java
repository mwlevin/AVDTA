/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.dta.Assignment;
import avdta.dta.DTAResults;
import avdta.dta.DTASimulator;
import avdta.dta.MSAAssignment;
import avdta.project.DTAProject;
import avdta.vehicle.DriverType;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static avdta.gui.GraphicUtils.*;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author ml26893
 */
public class MSAPane extends JPanel
{
    
    private DTAProject project;
    private DTAPane parent;
    
    private StatusBar status;
    
    private JButton run;
    
    private JTextField max_iter, min_gap;
    private JTextField start_iter;
    private JTextArea data;
    
    
    public MSAPane(DTAPane parent)
    {
        this.parent = parent;
        
        max_iter = new JTextField(5);       
        start_iter = new JTextField(5);
        min_gap = new JTextField(5);
        data = new JTextArea(5, 20);
        data.setEditable(false);
        
        status = new StatusBar();
        run = new JButton("Run MSA");
        
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                run();
            }
        });
        
        
        setLayout(new GridBagLayout());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(this, new JScrollPane(data), 0, 0, 1, 1);
        constrain(p, new JLabel("Start iteration:"), 0, 0, 1, 1);
        constrain(p, start_iter, 1, 0, 1, 1);
        constrain(p, new JLabel("Max. iteration:"), 0, 1, 1, 1);
        constrain(p, max_iter, 1, 1, 1, 1);
        constrain(p, new JLabel("Min. gap:"), 0, 2, 1, 1);
        constrain(p, min_gap, 1, 2, 1, 1);
        constrain(this, p, 0, 1, 1, 1);
        constrain(this, run, 0, 2, 2, 1);
        constrain(this, status, 0, 3, 1, 1);
        
        disable();
    }
    
    public void disable()
    {
        run.setEnabled(false);
        min_gap.setEditable(false);
        max_iter.setEditable(false);
        start_iter.setEditable(false);
    }
    
    public void enable()
    {
        run.setEnabled(true);
        min_gap.setEditable(true);
        max_iter.setEditable(true);
        start_iter.setEditable(true);
    }
    
    
    public void setProject(DTAProject project)
    {
        this.project = project;
        
        if(project != null)
        {
            start_iter.setText("1");
            max_iter.setText("30");
            min_gap.setText("1");
            enable();
        }
        else
        {
            start_iter.setText("");
            max_iter.setText("");
            min_gap.setText("");
            disable();
        }
    }
    
    public void loadAssignment(Assignment assign)
    {
        if(assign instanceof MSAAssignment)
        {
            start_iter.setText(""+((MSAAssignment)assign).getIter());
        }
        
        showAssignment(assign);
    }
    
    public void showAssignment(Assignment assign)
    {
        data.setText("");
        
        if(assign != null)
        {
            DTAResults results = assign.getResults();
            data.append("Gap:\t"+String.format("%.2f", results.getGapPercent())+"%\n");
            data.append("TSTT:\t"+String.format("%.1f", results.getTSTT())+" hr\n");
            data.append("Non-exit:\t"+results.getNonExiting());
        }
    }
    
    public void run()
    {
        try
        {
            Integer.parseInt(start_iter.getText().trim());
        }
        catch(Exception ex)
        {
            start_iter.setText("");
            start_iter.requestFocus();
            return;
        }
        
        try
        {
            Integer.parseInt(max_iter.getText().trim());
        }
        catch(Exception ex)
        {
            max_iter.setText("");
            max_iter.requestFocus();
            return;
        }
        
        try
        {
            Double.parseDouble(min_gap.getText().trim());
        }
        catch(Exception ex)
        {
            min_gap.setText("");
            min_gap.requestFocus();
            return;
        }
        
        
        parent.disable();
        
        
        final JPanel panel = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    
                    DTAResults results;
                    
                    DTASimulator sim = project.getSimulator();
                    sim.setStatusUpdate(status);
                    
                    int start = Integer.parseInt(start_iter.getText().trim());
                    int max = Integer.parseInt(max_iter.getText().trim());
                    double gap = Double.parseDouble(min_gap.getText().trim());
                    
                    if(start> 1)
                    {
                        results = sim.msa_cont(start, max, gap);
                    }
                    else
                    {
                        results = sim.msa(max, gap);
                    }
                    
                    sim.saveAssignment(new MSAAssignment(results, sim.getIteration()));
                    
                    double tstt = sim.getTSTT() / 3600.0;
                    double avg = tstt * 60 / sim.getNumVehicles();
                    
                    JOptionPane.showMessageDialog(panel, "Iterations: "+(sim.getIteration()-1)+"\nGap: "+String.format("%.2f", results.getGapPercent())+
                            "\nNon-exiting: "+results.getNonExiting()+"\t"+
                            "\nTSTT: "+String.format("%.1f", tstt)+" hr\nAvg. time: "+String.format("%.2f", avg)+" min\n"+
                            "HV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.HV)/60)+" min\n"+
                            "AV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.AV)/60)+" min",
                            "DTA complete", JOptionPane.PLAIN_MESSAGE);

                    status.update(0);
                    status.resetTime();

                    parent.reset();
                    parent.enable();
                }
                catch(IOException ex)
                {
                    ex.printStackTrace(System.err);
                    GUI.handleException(ex);
                }
                
                max_iter.setEnabled(true);
                min_gap.setEnabled(true);
                run.setEnabled(true);
            }
        };
        t.start();
    }
  
    public void reset()
    {
        max_iter.setText("30");
        start_iter.setText("1");
        min_gap.setText("1");
    }

}
