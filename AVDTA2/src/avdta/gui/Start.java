/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.network.ReadNetwork;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author ml26893
 */
public class Start extends JFrame
{
    public Start()
    {
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        final JList<String> list = new JList<String>();
        
        Set<String> networks = ReadNetwork.listNetworks();
        
        String[] data = new String[networks.size()];
        
        
        int idx = 0;
        
        for(String x : networks)
        {
            data[idx++] = x;
        }
        list.setListData(data);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        final JButton networkOptions = new JButton("Network options");
        final JButton dta = new JButton("DTA");
        final JButton fourstep = new JButton("Fourstep");
        final JButton savs = new JButton("SAV simulator");
        final JButton simulate = new JButton("Simulate");
        
        final JButton createNetwork = new JButton("Create network folder");
        
        networkOptions.setEnabled(false);
        dta.setEnabled(false);
        fourstep.setEnabled(false);
        savs.setEnabled(false);
        simulate.setEnabled(false);
        
        final JFrame frame = this;
        
        createNetwork.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createNetwork.setEnabled(false);
                
                String name = JOptionPane.showInputDialog(frame, "Enter the network name: ", "Network name?", JOptionPane.QUESTION_MESSAGE);
                
                if(name != null && name.trim().length() > 0)
                {
                    try
                    {
                        ReadNetwork.createNetwork(name.trim());
                    }
                    catch(IOException ex)
                    {
                        ex.printStackTrace(System.err);
                    }
                }
                
                createNetwork.setEnabled(true);
            }
        });
        
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                boolean enable = list.getSelectedValue() != null;
                
                networkOptions.setEnabled(enable);
                dta.setEnabled(enable);
                simulate.setEnabled(enable);
                //fourstep.setEnabled(enable);
                savs.setEnabled(enable);
            }
        });
        
        networkOptions.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                new NetworkOptions(list.getSelectedValue());
            }
        });
        
        dta.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               setVisible(false);
               
               new CreateSimulator(list.getSelectedValue(), false);
           }
        });
        
        simulate.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               setVisible(false);
               
               new CreateSimulator(list.getSelectedValue(), true);
           }
        });
        
        savs.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);
                new CreateSAVSimulator(list.getSelectedValue());
            }
        });
        
        GraphicUtils.constrain(p, new JLabel("Network: "), 0, 0, 1, 1);
        GraphicUtils.constrain(p, new JScrollPane(list), 0, 1, 1, 1);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        
        GraphicUtils.constrain(p2, networkOptions, 0, 0, 1, 1);
        GraphicUtils.constrain(p2, dta, 0, 1, 1, 1);
        GraphicUtils.constrain(p2, simulate, 0, 2, 1, 1);
        GraphicUtils.constrain(p2, fourstep, 0, 3, 1, 1);
        GraphicUtils.constrain(p2, savs, 0, 4, 1, 1);
        GraphicUtils.constrain(p, p2, 1, 0, 1, 2);
        
        GraphicUtils.constrain(p, createNetwork, 0, 2, 2, 1);
        
        
        
        
        
        
        
        
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
