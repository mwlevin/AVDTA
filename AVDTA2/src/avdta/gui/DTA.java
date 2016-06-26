/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.moves.EvaluateLinks;
import avdta.network.Results;
import avdta.network.Simulator;
import avdta.vehicle.DriverType;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.sql.ResultSet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author ml26893
 */
public class DTA extends JFrame
{
    
    private Simulator sim;
    
    private StatusBar status;
    
    private JButton run, importResults, moreResults;
    
    private JTextField max_iter, min_gap;
    
    private JCheckBox cont;
    
    public DTA(Simulator s)
    {
        this.sim = s;
        
        status = new StatusBar();
        
        sim.setStatusUpdate(status);
        
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        GraphicUtils.constrain(p, new JLabel("DTA on "+sim.getName()), 0, 0, 3, 1);
        GraphicUtils.constrain(p, new JLabel("Scenario: "+sim.getScenario()), 0, 1, 3, 1);
        
        max_iter = new JTextField(5);
        max_iter.setText("30");
        
        min_gap = new JTextField(5);
        min_gap.setText("0");
        
        GraphicUtils.constrain(p, new JLabel("Iterations: "), 0, 2, 1, 1);
        GraphicUtils.constrain(p, max_iter, 1, 2, 1, 1);
        GraphicUtils.constrain(p, new JLabel("Min. gap: "), 0, 3, 1, 1);
        GraphicUtils.constrain(p, min_gap, 1, 3, 1, 1);
        
        
        
        cont = new JCheckBox("Continue previous run");
        
        run = new JButton("Run");
        
        importResults = new JButton("Import results");
        moreResults = new JButton("More results");
        
        GraphicUtils.constrain(p, cont, 0, 4, 2, 1);
        
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                run.setEnabled(false);
                importResults.setEnabled(false);
                moreResults.setEnabled(false);
                max_iter.setEnabled(false);
                min_gap.setEnabled(false);
                cont.setEnabled(false);
                
                int iter = 0;
                double gap = 0;
                
                try
                {
                    iter = Integer.parseInt(max_iter.getText().trim());
                }
                catch(Exception ex)
                {
                    max_iter.setText("");
                    max_iter.requestFocus();
                }
                

                run(iter, gap);


                
                
                
            }
        });
        
        importResults.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                run.setEnabled(false);
                importResults.setEnabled(false);
                moreResults.setEnabled(false);
                max_iter.setEnabled(false);
                min_gap.setEnabled(false);
                cont.setEnabled(false);
                
                importResults();
            }
        });
        
        moreResults.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                new MoreResults(sim);
            }
        });
        
        importResults.setEnabled(false);
        moreResults.setEnabled(false);
        
        GraphicUtils.constrain(p, run, 0, 5, 2, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, status, 0, 6, 2, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, importResults, 0, 7, 2, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, moreResults, 0, 8, 2, 1, GridBagConstraints.CENTER);
        
        
        
        
        
        add(p);
        
        pack();
        setResizable(false);
        

        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(1);
            }
        });
        
        setVisible(true);
    }
    
    public void run(final int iter, final double gap)
    {
        final JFrame frame = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    Results results;
                    
                    if(cont.isSelected())
                    {
                        sim.readVehicles();
                        results = sim.msa_cont(iter + sim.getIteration()-1, gap);
                    }
                    else
                    {
                        results = sim.msa(iter, gap);
                    }
                    

                    double tstt = sim.getTSTT() / 3600.0;
                    double avg = tstt * 60 / sim.getNumVehicles();
                    
                    JOptionPane.showMessageDialog(frame, "Iterations: "+(sim.getIteration()-1)+"\nGap: "+String.format("%.2f", results.getGapPercent())+
                            "\nNon-exiting: "+results.getNonExiting()+"\t"+
                            "\nTSTT: "+String.format("%.1f", tstt)+" hr\nAvg. time: "+String.format("%.2f", avg)+" min\n"+
                            "HV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.HV)/60)+" min\n"+
                            "AV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.AV)/60)+" min",
                            "DTA complete", JOptionPane.PLAIN_MESSAGE);

                    status.update(0);
                    status.resetTime();

                    
                }
                catch(IOException ex)
                {
                    ex.printStackTrace(System.err);
                    GUI.handleException(frame, ex);
                }
                
                max_iter.setEnabled(true);
                min_gap.setEnabled(true);
                run.setEnabled(true);
                importResults.setEnabled(true);
                moreResults.setEnabled(true);
                cont.setEnabled(true);
            }
        };
        t.start();
    }
    
    public void importResults()
    {
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    sim.simulate(false);
                    sim.importResults();
                }
                catch(IOException ex)
                {
                    ex.printStackTrace(System.err);
                }
                
                run.setEnabled(true);
                importResults.setEnabled(true);
                moreResults.setEnabled(true);
            }
        };
        t.start();
    }
    

}
