/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import java.awt.Component;
import java.util.List;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author micha
 */
public class NodeRulePanel extends JPanel
{
    private JList list;
    
    private JButton up, down, newType, newData, edit, remove;
    
    private List<NodeRule> rules;
    private Component parent;
    
    public NodeRulePanel(Component parent_, List<NodeRule> rules_)
    {
        this.parent = parent_;
        this.rules = rules_;
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Node visualization"));
        
        up = new JButton("↑");
        down = new JButton("↓");
        
        
        newType = new JButton("New type rule");
        newData = new JButton("New data rule");
        edit = new JButton("Edit");
        remove = new JButton("Remove");
        
        remove.setEnabled(false);
        edit.setEnabled(false);
        up.setEnabled(false);
        down.setEnabled(false);
        
        list = new JList();
        list.setListData(new String[]{});
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellWidth(200);
        
        
        remove.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                rules.remove(list.getSelectedIndex());
                refresh();
            }
        });
        
        up.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = list.getSelectedIndex();
                
                NodeRule rule = rules.remove(idx);
                rules.add(idx-1, rule);
                refresh();
            }
        });
        
        down.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = list.getSelectedIndex();
                
                NodeRule rule = rules.remove(idx);
                rules.add(idx, rule);
                refresh();
            }
        });
        
        
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                boolean enable = list.getSelectedIndex() >= 0;
                
                up.setEnabled(enable && list.getSelectedIndex() > 0);
                down.setEnabled(enable && list.getSelectedIndex() < rules.size()-1);
                edit.setEnabled(enable);
                remove.setEnabled(enable);
            }
        });
        
        constrain(this, new JScrollPane(list), 0, 0, 3, 4);
        constrain(this, up, 0, 4, 1, 1);
        constrain(this, down, 1, 4, 1, 1);
        constrain(this, remove, 2, 4, 1, 1);
        constrain(this, newData, 3, 0, 1, 1);
        constrain(this, newType, 3, 1, 1, 1);
        constrain(this, edit, 3, 2, 1, 1);
    }
    
    
    
    public void setEnabled(boolean enable)
    {
        super.setEnabled(enable);
        
        list.setEnabled(enable);
        newType.setEnabled(enable);
        newData.setEnabled(enable);
        
        enable = enable && list.getSelectedIndex() >= 0;
                
        up.setEnabled(enable);
        down.setEnabled(enable);
        edit.setEnabled(enable);
        remove.setEnabled(enable);
        
    }
    
    public void refresh()
    {
        String[] data = new String[rules.size()];
        
        for(int i = 0; i < data.length; i++)
        {
            data[i] = rules.get(i).getName();
        }
        
        list.setSelectedIndex(-1);
        list.setListData(data);
    }
}
