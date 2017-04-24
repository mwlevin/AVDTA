/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels.sav;

import avdta.gui.GUI;
import avdta.gui.panels.AbstractGUIPanel;
import avdta.gui.panels.GUIPanel;
import avdta.gui.util.StatusBar;
import avdta.project.SAVProject;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import static avdta.gui.util.GraphicUtils.constrain;
import avdta.network.Simulator;
import avdta.sav.ReadSAVNetwork;
import avdta.sav.SAVSimulator;
import avdta.sav.Taxi;
import avdta.sav.SAVTraveler;
import avdta.sav.dispatch.Dispatch;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import static avdta.gui.util.GraphicUtils.constrain;


/**
 *
 * @author micha
 */
public class SAVPanel extends GUIPanel
{
    private StatusBar status;
    
    private JButton simulate;
    private JTextArea results;
    
    private JRadioButton pct, num;
    private JRadioButton equal, proportional;
    private JTextField pctTxt, numTxt;
    private JButton createFleet;
    
    
    private JCheckBox preemptiveRelocate;
    private JCheckBox drs;
    private JComboBox dispatch;
    private JButton save;
    
    
    
    
    private SAVProject project;
    
    public SAVPanel(AbstractGUIPanel parent)
    {
        super(parent);
        
        
        
        preemptiveRelocate = new JCheckBox("Preemptive relocation");
        drs = new JCheckBox("Dynamic ride-sharing");
        dispatch = new JComboBox(Dispatch.DISPATCHERS);
        dispatch.setSelectedIndex(Dispatch.DEFAULT);
        save = new JButton("Save");
        
        
        
        equal = new JRadioButton("Uniform");
        proportional = new JRadioButton("Proportional to productions");
        
        pct = new JRadioButton();
        num = new JRadioButton();
        
        pctTxt = new JTextField(5);
        numTxt = new JTextField(5);
        createFleet = new JButton("Create fleet");
        
        simulate = new JButton("Simulate");
        status = new StatusBar();
        
        results = new JTextArea(10, 20);
        results.setEditable(false);
        
        ChangeListener change = new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                pctTxt.setEditable(pct.isSelected());
                numTxt.setEditable(num.isSelected());
            }
        };
        
        pct.addChangeListener(change);
        num.addChangeListener(change);
        
        pct.setSelected(true);
        proportional.setSelected(true);
        
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(pct);
        buttonGroup.add(num);
        
        buttonGroup = new ButtonGroup();
        buttonGroup.add(equal);
        buttonGroup.add(proportional);
        
        save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                project.setOption("relocate", ""+preemptiveRelocate.isSelected());
                project.setOption("ride-sharing", ""+drs.isSelected());
                project.setOption("dispatcher", Dispatch.DISPATCHERS[dispatch.getSelectedIndex()]);
                
                try
                {
                    project.writeOptions();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        
        createFleet.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createFleet();
            }
        });
        
        simulate.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                simulate();
            }
        });
        
        
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        
        JPanel left = new JPanel();
        left.setLayout(new GridBagLayout());
        
        
        JPanel right = new JPanel();
        right.setLayout(new GridBagLayout());
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Options"));
        
        constrain(p2, drs, 0, 0, 2, 1);
        constrain(p2, preemptiveRelocate, 0, 1, 2, 1);
        constrain(p2, new JLabel("Dispatcher: "), 0, 2, 1, 1);
        constrain(p2, dispatch, 1, 2, 1, 1);
        constrain(p2, save, 0, 3, 2, 1, GridBagConstraints.CENTER);
        
        constrain(left, p2, 0, 0, 1, 1);
        
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        JPanel p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        
        constrain(p1, pct, 0, 0, 1, 1);
        constrain(p1, pctTxt, 1, 0, 1, 1);
        constrain(p1, new JLabel("% of trips"), 2, 0, 1, 1);
        
        constrain(p, p1, 0, 0, 1, 1);
        
        p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        
        constrain(p1, num, 0, 0, 1, 1);
        constrain(p1, numTxt, 1, 0, 1, 1);
        constrain(p1, new JLabel("SAVs"), 2, 0, 1, 1);
        
        constrain(p, p1, 0, 1, 1, 1);    
        
        p1 = new JPanel();
        p1.setLayout(new GridBagLayout());
        p1.setBorder(BorderFactory.createTitledBorder("Distribution"));
        
        constrain(p1, proportional, 0, 0, 1, 1);
        constrain(p1, equal, 0, 1, 1, 1);
        
        constrain(p, p1, 0, 2, 1, 1);
        
        constrain(p, createFleet, 0, 3, 1, 1, GridBagConstraints.CENTER);
        
        p.setBorder(BorderFactory.createTitledBorder("Fleet"));
        
        
        constrain(left, p, 0, 1, 1, 1);
        
        p2.setPreferredSize(new Dimension((int)p.getPreferredSize().getWidth(), (int)p2.getPreferredSize().getHeight()));
        
        
        constrain(right, new JScrollPane(results), 0, 0, 1, 1);
        constrain(right, simulate, 0, 1, 1, 1, GridBagConstraints.CENTER);
        constrain(right, status, 0, 2, 1, 1);
        
        
        
        constrain(panel, left, 0, 0, 1, 1);
        constrain(panel, right, 2, 0, 1, 1);
        
        add(panel);
        setEnabled(false);
    }
    
    public void createFleet()
    {
        parentSetEnabled(false);
        
        Thread t = new Thread()
        {
            public void run()
            {
                int taxis = 0;
        
                if(pct.isSelected())
                {
                    try
                    {
                        taxis = (int)Math.round(Double.parseDouble(pctTxt.getText()) * project.getSimulator().getNumTrips());
                    }
                    catch(Exception ex)
                    {
                        pctTxt.setText("");
                        pctTxt.requestFocus();
                        parentSetEnabled(true);
                        return;
                    }
                }
                else
                {
                    try
                    {
                        taxis = Integer.parseInt(numTxt.getText());
                    }
                    catch(Exception ex)
                    {
                        numTxt.setText("");
                        numTxt.requestFocus();
                        parentSetEnabled(true);
                        return;
                    }
                }
        
            
                ReadSAVNetwork read = new ReadSAVNetwork();
        
                try
                {
                    if(equal.isSelected())
                    {
                        read.createFleetEq(project, taxis);
                    }
                    else
                    {
                        read.createFleet(project, taxis);
                    }
                }
                catch(Exception ex)
                {
                    GUI.handleException(ex);
                }
                
                parentSetEnabled(true);
            }
        };
        t.start();
        
    }
    
    public void simulate()
    {
        final JPanel frame = this;
        
        Thread t = new Thread()
        {
            public void run()
            {
                parentSetEnabled(false);
                
                try
                {
                    project.loadSimulator();
                    SAVSimulator test = project.getSimulator();

                    test.setStatusUpdate(status);
                    
                    Dispatch dispatch = Dispatch.createDispatch(project.getOption("dispatcher"));
                    
                    test.setDispatch(dispatch);
                    
                    long time = System.nanoTime();
                    
                    test.initialize();
                    test.simulate();
                    
                    time = System.nanoTime() - time;
                    
                    String text = "";
                    
                    text += String.format("Time: %.2f s\n", time/1.0e9);
        
                    text += ("Avg. delay: "+test.getAvgWait())+"\n";
                    text += ("Avg. IVTT: "+test.getAvgIVTT())+"\n";
                    text += ("Avg. TT: "+test.getAvgTT())+"\n";
                    text += ("Total energy: "+test.getTotalEnergy())+"\n";
                    text += ("Avg. MPG: "+test.getAvgMPG())+"\n";
                    text += ("Total VMT: "+test.getTotalVMT())+"\n";
                    text += ("Empty VMT: "+test.getEmptyVMT())+"\n";
                    text += ("TSTT: "+test.getTSTT())+"\n";
                    
                    int inTaxi = 0;
                    int exited = 0;
                    int notDeparted = 0;

                    for(Taxi t : test.getTaxis())
                    {
                        inTaxi += t.getNumPassengers();
                    }

                    Map<Integer, Integer> errors = new TreeMap<Integer, Integer>();

                    for(SAVTraveler t : test.getTravelers())
                    {
                        if(t.isExited())
                        {
                            exited++;
                        }
                        else if(t.getDepTime() > Simulator.time)
                        {
                            notDeparted ++;
                        }
                        else
                        {
                            if(errors.containsKey(t.getOrigin().getId()))
                            {
                                errors.put(t.getOrigin().getId(), errors.get(t.getOrigin().getId())+1);
                            }
                            else
                            {
                                errors.put(t.getOrigin().getId(), 1);
                            }
                        }
                    }
        
                    text += ("---")+"\n";
                    text += ("Exited: "+exited)+"\n";
                    text += ("In taxi: "+inTaxi)+"\n";
                    text += ("Departing later: "+notDeparted)+"\n";

                    JOptionPane.showMessageDialog(frame, "SAV simulation complete", "SAV complete", JOptionPane.INFORMATION_MESSAGE);
                    
                    results.setText(text);
                    
                    
                }
                catch(Exception ex)
                {
                    GUI.handleException(ex);
                }
                
                parentSetEnabled(true);
            }
        };
        t.start();
    }
    
    public void setProject(SAVProject p)
    {
        project = p;
        
        reset();
    }
    
    
    public void reset()
    {
        results.setText("");
        
        if(project != null)
        {
            numTxt.setText("1000");
            pctTxt.setText("50");
            dispatch.setSelectedIndex(Dispatch.findDispatch(project.getOption("dispatcher")));
            drs.setSelected(project.getOption("ride-sharing").equalsIgnoreCase("true"));
            preemptiveRelocate.setSelected(project.getOption("relocate").equalsIgnoreCase("true"));
            setEnabled(true);
        }
        else
        {
            drs.setSelected(false);
            preemptiveRelocate.setSelected(false);
            dispatch.setSelectedIndex(Dispatch.DEFAULT);
            numTxt.setText("");
            pctTxt.setText("");
            setEnabled(false);
        }
    }
    
    
    
    public void setEnabled(boolean e)
    {
        boolean enable = e && project != null;
        createFleet.setEnabled(enable);
        simulate.setEnabled(enable);
        pct.setEnabled(enable);
        num.setEnabled(enable);
        pctTxt.setEditable(enable && pct.isSelected());
        numTxt.setEditable(enable && num.isSelected());
        dispatch.setEnabled(enable);
        drs.setEnabled(enable);
        preemptiveRelocate.setEnabled(enable);
        save.setEnabled(enable);
        equal.setEnabled(enable);
        proportional.setEnabled(enable);
    }
}
