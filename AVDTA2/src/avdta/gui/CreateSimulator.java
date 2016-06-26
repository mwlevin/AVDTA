/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.network.node.BackPressureObj;
import avdta.network.node.FCFSPolicy;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author micha
 */
public class CreateSimulator extends JFrame
{
    private boolean scenarioMatches;
    private boolean simulateOnly;
    
    public CreateSimulator(final String name, boolean simOnly)
    {
        simulateOnly = simOnly;
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        GraphicUtils.constrain(p, new JLabel("Read network "+name), 0, 0, 3, 1);
        
        final JTextField scenario = new JTextField(15);
        
        
        GraphicUtils.constrain(p, new JLabel("Scenario name: "), 0, 1, 1, 1);
        GraphicUtils.constrain(p, scenario, 1, 1, 2, 1);
        
        final JRadioButton ctm = new JRadioButton("CTM");
        final JRadioButton ltm = new JRadioButton("LTM");
        
        ButtonGroup group = new ButtonGroup();
        group.add(ctm);
        group.add(ltm);
        
        ctm.setSelected(true);
        
        GraphicUtils.constrain(p, new JLabel("Link model: "), 0, 2, 1, 1);
        GraphicUtils.constrain(p, ctm, 1, 2, 1, 1);
        GraphicUtils.constrain(p, ltm, 2, 2, 1, 1);
        
        final JList<String> list = new JList<String>();
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        list.setListData(nodetypes);
        list.setSelectedIndex(0);
        
        GraphicUtils.constrain(p, new JLabel("Node model: "), 0, 3, 1, 1);
        GraphicUtils.constrain(p, new JScrollPane(list), 1, 3, 2, 1);
        
        final JButton run = new JButton(simulateOnly?"Simulate":"Read network");
        
        GraphicUtils.constrain(p, run, 0, 4, 2, 1, GridBagConstraints.CENTER);
        
        scenarioMatches = true;
        
        final JFrame frame = this;
        
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int linktype = 0;
                if(ctm.isSelected())
                {
                    linktype = Link.CTM;
                }
                else if(ltm.isSelected())
                {
                    linktype = Link.LTM;
                }
                
                if(scenario.getText().trim().length() == 0)
                {
                    scenario.setText(
                            JOptionPane.showInputDialog(frame, "Enter the scenario name: ", "Scenario name?", JOptionPane.QUESTION_MESSAGE)
                            );
                    
                }
                
                if(scenario.getText().trim().length() == 0)
                {
                    scenario.setText("");
                    scenario.requestFocus();
                    return;
                }
 
                scenario.setEnabled(false);
                ctm.setEnabled(false);
                ltm.setEnabled(false);
                list.setEnabled(false);
                run.setEnabled(false);

                start(name, scenario.getText().trim(), linktype, list.getSelectedValue());
                    

            }
        });
        
        /*
        list.addListSelectionListener(new ListSelectionListener()
        {
           public void valueChanged(ListSelectionEvent e)
           {
               if(list.getSelectedValue())
           }
        });
        */
        
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
    
    private static final String[] nodetypes = new String[]{"signals", "TBR-FCFS", "TBR-pressure", "TBR-phased", "mixed"};
    
    public void start(final String name, final String scenario, final int linktype, final String nodetype)
    {
        final JFrame frame = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    readNetwork(name, scenario, linktype, nodetype);
                    setVisible(false);
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
    
    public void readNetwork(String name, String scenario, int linktype, String nodetype) throws IOException
    {
        ReadNetwork input = new ReadNetwork();
        Simulator sim = new Simulator(name);
        
        sim.setScenario(scenario);
        
        readNodes(input, sim, linktype, nodetype);
        input.readVehicles(sim);
        
        
        sim.initialize();
        
        if(simulateOnly)
        {
            try
            {
                sim.readVehicles();
                sim.simulate(false);
            }
            catch(Exception ex)
            {
                ex.printStackTrace(System.err);
                GUI.handleException(this, ex);
            }
            
            setVisible(false);
            new MoreResults(sim);
        }
        else
        {
            setVisible(false);
            new DTA(sim);
        }
        
    }
    
    public void readNodes(ReadNetwork input, Simulator sim, int linktype, String nodetype) throws IOException
    {
        input.readNetwork(sim, linktype);
        
        if(nodetype.equals("signals"))
        {
            input.readSignals(sim);
        }
        else if(nodetype.equals("TBR-FCFS"))
        {
            input.readTBR(sim, new FCFSPolicy(), Node.CR);
        }
        else if(nodetype.equals("TBR-pressure"))
        {
            input.readTBR(sim, new BackPressureObj(), Node.MCKS);
        }
        else if(nodetype.equals("TBR-phased"))
        {
            input.readTBR(sim, null, Node.PHASED_TBR);
        }
        else if(nodetype.equals("mixed"))
        {
            JFileChooser chooser = new JFileChooser(new File("data/"+sim.getName()+"/"));
            
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text files", "txt");
            chooser.setFileFilter(filter);
            
            JOptionPane.showMessageDialog(this, "Select file containing reservation nodes", "Select file", JOptionPane.PLAIN_MESSAGE);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) 
            {
               input.readPhases(sim, new BackPressureObj(), Node.MIX_SIGNAL_TBR, chooser.getSelectedFile());
            }
            else
            {
                input.readPhases(sim, new BackPressureObj(), Node.MIX_SIGNAL_TBR);
            }
            
        }
    }
    
    
}
