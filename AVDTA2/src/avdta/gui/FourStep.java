/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.network.Simulator;
import java.awt.GridBagConstraints;
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
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author micha
 */
public class FourStep extends JFrame
{
    private StatusBar status;
    
    public FourStep(final Simulator sim)
    {
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        
        status = new StatusBar();
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        
        GraphicUtils.constrain(p, new JLabel("4-step planning on "+sim.getName()), 0, 0, 5, 1);
        GraphicUtils.constrain(p, new JLabel("Scenario: "+sim.getScenario()), 0, 1, 5, 1);
        
        final JTextField iter4step = new JTextField(5);
        iter4step.setText("15");
        
        final JTextField iterDTA = new JTextField(5);
        iterDTA.setText("50");
        
        final JTextField mingap = new JTextField(5);
        mingap.setText("0");
        
        final JCheckBox repositioning = new JCheckBox("Repositioning trips");
        
        GraphicUtils.constrain(p, new JLabel("4 step iterations: "), 0, 2, 1, 1);
        GraphicUtils.constrain(p, iter4step, 1, 2, 1, 1);
        GraphicUtils.constrain(p, new JLabel("DTA ierations: "), 0, 3, 1, 1);
        GraphicUtils.constrain(p, iterDTA, 1, 3, 1, 1);
        GraphicUtils.constrain(p, new JLabel("DTA min. gap"), 0, 4, 1, 1);
        GraphicUtils.constrain(p, mingap, 1, 4, 1, 1);
        
        final JButton run = new JButton("Run");
        final JButton importResults = new JButton("Import results");
        
        importResults.setEnabled(false);
        
        GraphicUtils.constrain(p, run, 0, 5, 2, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, status, 0, 6, 2, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, importResults, 0, 7, 2, 1, GridBagConstraints.CENTER);
        
        
        
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int i4step = 0;
                int iDTA = 0;
                double gap = 0;
                
                try
                {
                    i4step = Integer.parseInt(iter4step.getText().trim());
                }
                catch(Exception ex)
                {
                    iter4step.setText("");
                    iter4step.requestFocus();
                    return;
                }
                
                try
                {
                    iDTA = Integer.parseInt(iterDTA.getText().trim());
                }
                catch(Exception ex)
                {
                    iterDTA.setText("");
                    iterDTA.requestFocus();
                    return;
                }
                
                try
                {
                    gap = Double.parseDouble(mingap.getText().trim());
                }
                catch(Exception ex)
                {
                    mingap.setText("");
                    mingap.requestFocus();
                    return;
                }
                
                run.setEnabled(false);
                iter4step.setEnabled(false);
                iterDTA.setEnabled(false);
                mingap.setEnabled(false);
                
                try
                {
                    start(sim);
                    
                    
                    importResults.setEnabled(true);
                }
                catch(Exception ex)
                {

                }
                
                run.setEnabled(true);
                iter4step.setEnabled(true);
                iterDTA.setEnabled(true);
                mingap.setEnabled(true);
                
            }
        });
        
        
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
    
    public void start(final Simulator sim)
    {
        final JFrame frame = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    
                }
                catch(Exception ex)
                {
                    ex.printStackTrace(System.err);
                    GUI.handleException(frame, ex);
                }
            }
        };
        
        t.start();
    }
}
