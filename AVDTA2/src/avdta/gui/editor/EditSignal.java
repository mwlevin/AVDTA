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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
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
    private JButton newTurn, newPhase, savePhase, saveTurn;
    private JComboBox inc, out;
    private JButton removeTurn, removePhase;
    private JButton up, down;
    
    private List<Phase> phases;
    private List<Turn> turns;
    
    
    public EditSignal(Signalized signal_, Node node_)
    {
        this.signal = signal_;
        this.node = node_;
        
        turns = new ArrayList<Turn>();
        phases = signal.getPhases();
        
        phasesList = new JList();
        phasesList.setListData(new String[]{});
        phasesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        phasesList.setFixedCellWidth(150);
        
        turnsList = new JList();
        turnsList.setListData(new String[]{});
        turnsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        turnsList.setFixedCellWidth(200);
        
        up = new JButton("↑");
        down = new JButton("↓");
        
        up.setEnabled(false);
        down.setEnabled(false);
        
        removeTurn = new JButton("Remove");
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
        
        
        newTurn = new JButton("New turn");
        newPhase = new JButton("New phase");
        
        newTurn.setEnabled(false);
        inc.setEnabled(false);
        out.setEnabled(false);
        removeTurn.setEnabled(false);
        removePhase.setEnabled(false);
        
        up.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = phasesList.getSelectedIndex();
                Phase p = phases.remove(idx);
                phases.add(idx-1, p);
                refreshPhases();
            }
        });
        
        down.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = phasesList.getSelectedIndex();
                Phase p = phases.remove(idx);
                phases.add(idx, p);
                refreshPhases();
            }
        });
        
        turnsList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                boolean enable = turnsList.getSelectedIndex() >= 0;
                removeTurn.setEnabled(enable);
            }
        });
        
        phasesList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                boolean enable = phasesList.getSelectedIndex() >= 0;
                
                removePhase.setEnabled(enable);
                inc.setEnabled(enable);
                out.setEnabled(enable);
                saveTurn.setEnabled(enable);
                newTurn.setEnabled(enable);
                
                up.setEnabled(phasesList.getSelectedIndex() > 0);
                down.setEnabled(phasesList.getSelectedIndex() < phases.size()-1);
                
                enable = enable && turnsList.getSelectedIndex() >= 0;
                removeTurn.setEnabled(enable);
            }
        });
    }
    
    public void save()
    {
    
    }
    
    public void cancel()
    {
        
    }
    
    public void refreshPhases()
    {
        String[] data = new String[phases.size()];
        
        for(int i = 0; i < data.length; i++)
        {
            data[i] = phases.get(i).toString();
        }
        
        phasesList.setListData(data);
    }
    
    public void refreshTurns()
    {
        String[] data = new String[turns.size()];
        
        for(int i = 0; i < data.length; i++)
        {
            data[i] = turns.get(i).toString();
        }
        
        turnsList.setListData(data);
    }
}
