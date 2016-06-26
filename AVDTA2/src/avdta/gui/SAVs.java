/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.sav.SAVSimulator;
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
public class SAVs extends JFrame
{
    private StatusBar status;
    
    private SAVSimulator sim;
    
    private JCheckBox drs, prl;
    private JButton run;
    
    public SAVs(SAVSimulator sim)
    {
        this.sim = sim;
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        status = new StatusBar();
        
        sim.setStatusUpdate(status);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        GraphicUtils.constrain(p, new JLabel("Simulate SAVs on "+sim.getName()), 0, 0, 2, 1);
        GraphicUtils.constrain(p, new JLabel("Scenario: "+sim.getScenario()), 0, 1, 2, 1);
        
        final JTextField numSAVs = new JTextField(8);
        numSAVs.setText(""+sim.getNumTrips() / 4);
        
        GraphicUtils.constrain(p, new JLabel("Num. trips: "+sim.getNumTrips()), 0, 2, 2, 1);
        GraphicUtils.constrain(p, new JLabel("Num. SAVs: "), 0, 3, 1, 1);
        GraphicUtils.constrain(p, numSAVs, 1, 3, 1, 1);
        
        drs = new JCheckBox("Dynamic ride-sharing");
        prl = new JCheckBox("Premptive relocation");
        
        GraphicUtils.constrain(p, drs, 0, 4, 2, 1);
        GraphicUtils.constrain(p, prl, 0, 5, 2, 1);
        
        run = new JButton("Simulate");
        
        GraphicUtils.constrain(p, run, 0, 6, 2, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, status, 0, 7, 2, 1, GridBagConstraints.CENTER);
        
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int fleetsize = 0;
                
                try
                {
                    fleetsize = Integer.parseInt(numSAVs.getText().trim());
                }
                catch(Exception ex)
                {
                    numSAVs.setText("");
                    numSAVs.requestFocus();
                    return;
                }
                
                run.setEnabled(false);
                drs.setEnabled(false);
                prl.setEnabled(false);
                numSAVs.setEnabled(false);
                
                run(fleetsize);
                
                

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
                System.exit(0);
            }
        });
        
        setVisible(true);
    }
    
    public void run(final int fleetsize)
    {
        Thread t = new Thread()
        {
            public void run()
            {
                SAVSimulator.relocate = prl.isSelected();
                SAVSimulator.ride_share = drs.isSelected();
                
                sim.initialize();
        
                sim.createTaxis(fleetsize);
                
                try
                {
                    sim.simulate();
                }
                catch(IOException ex)
                {
                    ex.printStackTrace(System.err);
                }
        
                run.setEnabled(true);
                drs.setEnabled(true);
                prl.setEnabled(true);
            }
        };
        
        t.start();

    }
}
