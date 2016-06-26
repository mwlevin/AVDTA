/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.moves.EvaluateLinks;
import avdta.network.Simulator;
import avdta.vehicle.DriverType;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
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
public class MoreResults extends JFrame
{
    private static final String[] options =  new String[]{"Import results", "Link flow", "Link travel time", "Node V/C", "Link V/C", "Driver type TT", "MOVES"};
    
    private JList list;
    private JButton run;
    
    public MoreResults(final Simulator sim)
    {
        setTitle(GUI.getTitle());
        setIconImage(GUI.getIcon());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        
        
        list = new JList();
        list.setListData(options);
        
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        run = new JButton("Run");
        
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                run.setEnabled(list.getSelectedValue() != null);
            }
        });
        
        run.setEnabled(false);
        
        GraphicUtils.constrain(p, new JScrollPane(list), 0, 0, 1, 1);
        GraphicUtils.constrain(p, run, 1, 0, 1, 1);
        
        
        run.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                run.setEnabled(false);
                list.setEnabled(false);
                
                run((String)list.getSelectedValue(), sim);
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
    
    public void run(final String option, final Simulator sim)
    {
        final JFrame frame = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                try
                {
                    if(option.equals("Import results"))
                    {
                        //sim.simulate(false);
                        sim.importResults();
                        JOptionPane.showMessageDialog(frame, "Import results complete", "Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if(option.equals("Link flow"))
                    {
                        int start = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter start time (s):", "Input", JOptionPane.QUESTION_MESSAGE).trim());
                        int end = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter end time (s):", "Input", JOptionPane.QUESTION_MESSAGE).trim());
                        sim.printLinkFlow(start, end);
                        JOptionPane.showMessageDialog(frame, "Link flows saved to results/"+sim.getName()+"/"+sim.getScenario()+"/linkq.txt", "Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if(option.equals("Link travel time"))
                    {
                        int start = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter start time (s):", "Input", JOptionPane.QUESTION_MESSAGE).trim());
                        int end = Integer.parseInt(JOptionPane.showInputDialog(frame, "Enter end time (s):", "Input", JOptionPane.QUESTION_MESSAGE).trim());
                        sim.printLinkTT(start, end);
                        JOptionPane.showMessageDialog(frame, "Link flows saved to results/"+sim.getName()+"/"+sim.getScenario()+"/linktt.txt", "Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if(option.equals("Node V/C"))
                    {
                        sim.printNodeVC();
                        JOptionPane.showMessageDialog(frame, "Node V/C saved to results/"+sim.getName()+"/"+sim.getScenario()+"/node_vc.txt", "Complete", JOptionPane.INFORMATION_MESSAGE);
                    
                    }
                    else if(option.equals("Link V/C"))
                    {
                        sim.printNodeVC();
                        JOptionPane.showMessageDialog(frame, "Node V/C saved to results/"+sim.getName()+"/"+sim.getScenario()+"/link_vc.txt", "Complete", JOptionPane.INFORMATION_MESSAGE);
                    
                    }
                    else if(option.equals("MOVES"))
                    {
                        //sim.simulate(false);
                        sim.writeVehicleResults();

                        EvaluateLinks test = new EvaluateLinks();
                        test.calculate(sim);
                        test.printLinkSource();
                        test.printLink();
                        
                        JOptionPane.showMessageDialog(frame, "MOVES results complete", "Complete", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if(option.equals("Driver type TT"))
                    {
                            JOptionPane.showMessageDialog(frame, "HV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.HV)/60)+" min\n"+
                            "AV TT: "+String.format("%.2f", sim.getAvgTT(DriverType.AV)/60)+" min", "Driver type TT", JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    
                    
                    
                    
                    
                }
                catch(Exception ex)
                {
                    ex.printStackTrace(System.err);
                    GUI.handleException(frame, ex);
                }
                
                run.setEnabled(true);
                list.setEnabled(true);
            }
        };
        
        t.start();
    }
}
