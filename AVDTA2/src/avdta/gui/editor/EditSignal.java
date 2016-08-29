/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Phase;
import avdta.network.node.Signalized;
import avdta.network.node.Turn;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author micha
 */
public class EditSignal extends JPanel
{
    private Signalized signal;
    private Node node;
    
    private JList phasesList, turnsList;
    private JButton newPhase, savePhase, saveTurn;
    private JComboBox inc, out;
    private JButton removeTurn, removePhase;
    private JButton up, down;
    
    private List<Phase> phases;
    private Set<Turn> turns;
    
    private TurnVisual visual;
    
    private JTextField red, yellow, green;
    
    
    public EditSignal(Signalized signal_, Node node_)
    {
        this.signal = signal_;
        this.node = node_;
        
        visual = new TurnVisual();
        
        turns = new TreeSet<Turn>();
        phases = signal.getPhases();
        
        red = new JTextField(3);
        yellow = new JTextField(3);
        green = new JTextField(3);
        
        
        
        phasesList = new JList();
        phasesList.setListData(new String[]{});
        phasesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        phasesList.setFixedCellWidth(120);
        phasesList.setVisibleRowCount(6);
        
        turnsList = new JList();
        turnsList.setListData(new String[]{});
        turnsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        turnsList.setFixedCellWidth(120);
        turnsList.setVisibleRowCount(6);
        
        saveTurn = new JButton("Add turn");
        
        up = new JButton("↑");
        down = new JButton("↓");
        
        up.setEnabled(false);
        down.setEnabled(false);
        
        removeTurn = new JButton("Remove turn");
        removePhase = new JButton("Remove");
        
        Set<Link> set = new TreeSet<Link>();
        
        for(Link l : node.getIncoming())
        {
            set.add(l);
        }
        
        inc = new JComboBox(set.toArray());
        
        set = new TreeSet<Link>();
        
        for(Link l : node.getOutgoing())
        {
            set.add(l);
        }
        
        out = new JComboBox(set.toArray());
        
        visual.setLinks((Link)inc.getSelectedItem(), (Link)out.getSelectedItem());
        
        
        newPhase = new JButton("New phase");
        savePhase = new JButton("Save phase");
        savePhase.setEnabled(false);
        
        removeTurn.setEnabled(false);
        removePhase.setEnabled(false);
        
        savePhase.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                savePhase();
                phasesList.clearSelection();
            }
        });
        
        saveTurn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                turns.add(new Turn((Link)inc.getSelectedItem(), (Link)out.getSelectedItem()));
                inc.setSelectedIndex(0);
                out.setSelectedIndex(0);
                refreshTurns();
            }
        });
        
        newPhase.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                phasesList.clearSelection();
            }
        });
        
        DocumentListener changeListener = new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                savePhase.setEnabled(true);
            }
            public void changedUpdate(DocumentEvent e)
            {
                savePhase.setEnabled(true);
            }
            public void removeUpdate(DocumentEvent e)
            {
                savePhase.setEnabled(true);
            }
        };
        
        red.getDocument().addDocumentListener(changeListener);
        yellow.getDocument().addDocumentListener(changeListener);
        green.getDocument().addDocumentListener(changeListener);
        
        removeTurn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                savePhase.setEnabled(true);
                turns.remove((Turn)turnsList.getSelectedValue());
                refreshTurns();
            }
        });
        
        
        
        up.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = phasesList.getSelectedIndex();
                Phase p = phases.remove(idx);
                phases.add(idx-1, p);
                reorder();
                refreshPhases();
                phasesList.setSelectedIndex(idx-1);
            }
        });
        
        down.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = phasesList.getSelectedIndex();
                Phase p = phases.remove(idx);
                phases.add(idx+1, p);
                reorder();
                refreshPhases();
                phasesList.setSelectedIndex(idx+1);
            }
        });
        
        turnsList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                boolean enable = turnsList.getSelectedIndex() >= 0;
                removeTurn.setEnabled(enable);
                
                Turn t = (Turn)turnsList.getSelectedValue();
                
                if(t != null)
                {
                    inc.setSelectedItem(t.i);
                    out.setSelectedItem(t.j);
                }
                else
                {
                    inc.setSelectedIndex(0);
                    out.setSelectedIndex(0);
                }
            }
        });
        
        phasesList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                boolean enable = phasesList.getSelectedIndex() >= 0;
                
                removePhase.setEnabled(enable);
                up.setEnabled(phasesList.getSelectedIndex() > 0);
                down.setEnabled(enable && phasesList.getSelectedIndex() < phases.size()-1);
                
                enable = enable && turnsList.getSelectedIndex() >= 0;
                
                turns.clear();
                
                Phase phase = (Phase)phasesList.getSelectedValue();
                
                if(phase != null)
                {
                    green.setText(""+phase.getGreenTime());
                    yellow.setText(""+phase.getYellowTime());
                    red.setText(""+phase.getRedTime());

                    for(Turn t : phase.getTurns())
                    {
                        turns.add(t);
                    }
                }
                else
                {
                    green.setText("");
                    yellow.setText("");
                    red.setText("");
                }
                refreshTurns();
                
                savePhase.setEnabled(!enable);
            }
        });
        
        ItemListener itemListener = new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                visual.setLinks((Link)inc.getSelectedItem(), (Link)out.getSelectedItem());
                savePhase.setEnabled(true);
            }
        };
        inc.addItemListener(itemListener);
        out.addItemListener(itemListener);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Phase"));
        
        
        constrain(p, newPhase, 0, 0, 2, 1);
        constrain(p, new JLabel("Green time: "), 0, 1, 1, 1);
        constrain(p, green, 1, 1, 1, 1);
        constrain(p, new JLabel("Yellow time: "), 0, 2, 1, 1);
        constrain(p, yellow, 1, 2, 1, 1);
        constrain(p, new JLabel("Red time: "), 0, 3, 1, 1);
        constrain(p, red, 1, 3, 1, 1);
        constrain(p, savePhase, 0, 4, 2, 1);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        
        constrain(p2, new JLabel("Turns: "), 0, 0, 1, 1);
        constrain(p2, new JScrollPane(turnsList), 0, 1, 1, 1);
        constrain(p, p2, 2, 0, 1, 5);
        
        p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        
        constrain(p2, inc, 0, 0, 1, 1);
        constrain(p2, out, 0, 1, 1, 1);
        constrain(p2, saveTurn, 0, 2, 1, 1);
        constrain(p2, visual, 1, 0, 1, 3);
        constrain(p2, removeTurn, 0, 3, 2, 1);
        constrain(p, p2, 3, 0, 1, 5);
        
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Signal for "+node.getId()));
        constrain(this, new JLabel("Phases: "), 0, 0, 3, 1);
        constrain(this, new JScrollPane(phasesList), 0, 1, 3, 1);
        constrain(this, up, 0, 2, 1, 1);
        constrain(this, down, 1, 2, 1, 1);
        constrain(this, removePhase, 2, 2, 1, 1);
        constrain(this, p, 3, 0, 1, 3);
        
        setMinimumSize(getPreferredSize());
        
        refreshPhases();
    }
    
    public void reorder()
    {
        int seq = 1;
        
        for(Phase p : phases)
        {
            p.setSequence(seq++);
        }
    }
    public void savePhase()
    {
        double yellow_ = 0;
        double red_ = 0;
        double green_ = 0;
        
        try
        {
            yellow_ = Double.parseDouble(yellow.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Yellow time must be a non-negative number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if(yellow_ < 0)
        {
            JOptionPane.showMessageDialog(this, "Yellow time must be a non-negative number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        try
        {
            green_ = Double.parseDouble(green.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Green time must be a non-negative number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if(green_ < 0)
        {
            JOptionPane.showMessageDialog(this, "Green time must be a non-negative number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        try
        {
            red_ = Double.parseDouble(yellow.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Red time must be a non-negative number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        if(red_ < 0)
        {
            JOptionPane.showMessageDialog(this, "Red time must be a non-negative number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        
        if(turns.size() == 0)
        {
            JOptionPane.showMessageDialog(this, "Warning: no turning movements added.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
        
        Turn[] turnArray = new Turn[turns.size()];
        
        int idx = 0;
        for(Turn t : turns)
        {
            turnArray[idx++] = t;
        }
        
        Phase phase;
        
        if(phasesList.getSelectedIndex() >= 0)
        {
            phase = (Phase)phasesList.getSelectedValue();
            phase.setGreenTime(green_);
            phase.setYellowTime(yellow_);
            phase.setRedTime(red_);
            phase.setTurns(turnArray);
        }
        else
        {
            phase = new Phase(phases.size()+1, turnArray, green_, yellow_, red_);
            phases.add(phase);
            refreshPhases();
        }
        
    }
    

    public void cancel()
    {
        
    }
    
    public void refreshPhases()
    {
        Phase[] data = new Phase[phases.size()];
        
        for(int i = 0; i < data.length; i++)
        {
            data[i] = phases.get(i);
        }
        
        phasesList.setListData(data);
    }
    
    public void refreshTurns()
    {
        Turn[] data = new Turn[turns.size()];
        
        int idx = 0;
        for(Turn t : turns)
        {
            data[idx++] = t;
        }
        
        turnsList.setListData(data);
        
        inc.setSelectedIndex(0);
        out.setSelectedIndex(0);
    }
}
