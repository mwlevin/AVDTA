/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.DownloadElevation;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author ml26893
 */
public class Elevation extends JFrame
{
    private StatusBar status;
    
    
    private String network;
    
    public Elevation(String network)
    {
        this.network = network;
        
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        
        status = new StatusBar();
        
        final JButton run = new JButton("Start");
        
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                run.setEnabled(false);
                run();
            }
        });
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        GraphicUtils.constrain(p, new JLabel("Download elevations for "+network), 0, 0, 1, 1);
        GraphicUtils.constrain(p, run, 0, 1, 1, 1);
        GraphicUtils.constrain(p, status, 0, 2, 1, 1);
        
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
    
    public void run()
    {
        final JFrame frame = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                DownloadElevation test = new DownloadElevation();
                test.setUpdate(status);
                
                try
                {
                    test.download(network);
                }
                catch(IOException ex)
                {
                    ex.printStackTrace(System.err);
                    GUI.handleException(frame, ex);
                }
                
                JOptionPane.showMessageDialog(frame, "Finished downloading elevation data for "+network,
                            "Download complete", JOptionPane.PLAIN_MESSAGE);
                System.exit(0);
            }
        };
        
        t.start();
    }
}
