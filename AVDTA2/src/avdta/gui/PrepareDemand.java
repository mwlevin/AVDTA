/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.network.ReadNetwork;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author ml26893
 */
public class PrepareDemand extends JFrame
{
    private JCheckBox useDynamicOD;
    private JButton run;
    private JTextField prop, avprop;
    
    public PrepareDemand(final String network)
    {
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        
        useDynamicOD = new JCheckBox("Use dynamic trips");
        prop = new JTextField(5);
        prop.setText("100");
        avprop = new JTextField(5);
        avprop.setText("0");
        
        run = new JButton("Prepare demand");
        
        GraphicUtils.constrain(p, new JLabel("Prepare demand for "+network), 0, 0, 2, 1);
        GraphicUtils.constrain(p, new JLabel("Demand percentage: "), 0, 1, 1, 1);
        GraphicUtils.constrain(p, prop, 1, 1, 1, 1);
        GraphicUtils.constrain(p, useDynamicOD, 0, 2, 2, 1);
        GraphicUtils.constrain(p, new JLabel("AV percentage: "), 0, 3, 1, 1);
        GraphicUtils.constrain(p, avprop, 1, 3, 1, 1);
        GraphicUtils.constrain(p, run , 0, 4, 2, 1, GridBagConstraints.CENTER);
        
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                double p = 0;
                double avp = 0;
                
                try
                {
                    p = Double.parseDouble(prop.getText().trim());
                }
                catch(Exception ex)
                {
                    prop.setText("");
                    prop.requestFocus();
                    return;
                }
                
                try
                {
                    avp = Double.parseDouble(avprop.getText().trim());
                }
                catch(Exception ex)
                {
                    avprop.setText("");
                    avprop.requestFocus();
                    return;
                }
                
                

                prop.setEnabled(false);
                avprop.setEnabled(false);
                run.setEnabled(false);
                useDynamicOD.setEnabled(false);

                run(network, p, avp);

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
    
    public void run(final String network, final double p, final double avp)
    {
        final JFrame frame = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    ReadNetwork read = new ReadNetwork();
                    if(!useDynamicOD.isSelected())
                    {
                        read.createDynamicOD(network);
                    }
                    
                    int num = read.prepareDemand(network, p, avp);
                    
                    JOptionPane.showMessageDialog(frame, "Created "+num+" vehicle trips",
                            "Prepare demand complete", JOptionPane.PLAIN_MESSAGE);
                    
                    prop.setEnabled(true);
                    avprop.setEnabled(true);
                    run.setEnabled(true);
                    useDynamicOD.setEnabled(true);
                }
                catch(IOException ex)
                {
                    ex.printStackTrace(System.err);
                    GUI.handleException(frame, ex);
                }
            }
        };
        t.start();
                    
    }
}
