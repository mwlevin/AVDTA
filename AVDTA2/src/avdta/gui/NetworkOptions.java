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
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author ml26893
 */
public class NetworkOptions extends JFrame
{
    public NetworkOptions(final String network)
    {
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        

        
        final JButton changeOptions = new JButton("Change options");
        final JButton prepareDemand = new JButton("Prepare demand");
        
        final JButton elevation = new JButton("Download elevation");
        
        changeOptions.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                
                ChangeOptions window = new ChangeOptions(network);
            }
        });
        
        prepareDemand.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                PrepareDemand window = new PrepareDemand(network);
            }
        });
        
        
        elevation.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                
                new Elevation(network);
            }
        });

        

        
        GraphicUtils.constrain(p, changeOptions, 0, 0, 1, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, prepareDemand, 0, 1, 1, 1, GridBagConstraints.CENTER);
        GraphicUtils.constrain(p, elevation, 0, 2, 1, 1, GridBagConstraints.CENTER);
        
        
        
        
        
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
}
