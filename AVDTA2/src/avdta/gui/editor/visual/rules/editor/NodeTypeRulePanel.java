/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.editor;

import avdta.gui.editor.EditNode;
import avdta.gui.editor.visual.rules.NodeTypeRule;
import static avdta.gui.util.GraphicUtils.constrain;
import avdta.gui.util.JColorButton;
import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author micha
 */
public class NodeTypeRulePanel extends JPanel
{
    private JComboBox type, control, policy;
    private JButton save;
    private JColorButton color;
    private JTextField width;
    
    private NodeTypeRule prev;
    
    public NodeTypeRulePanel()
    {
        save = new JButton("Save");
        
        type = new JComboBox(EditNode.TYPES);
        control = new JComboBox(EditNode.CONTROLS);
        policy = new JComboBox(EditNode.POLICIES);
        
        width = new JTextField(3);
        width.setText("5");
        
        type.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                control.setEnabled(type.getSelectedIndex() == EditNode.INTERSECTION);
                policy.setEnabled(type.getSelectedIndex() == EditNode.INTERSECTION &&
                        control.getSelectedIndex() == EditNode.RESERVATIONS);
                save.setEnabled(true);
            }
        });
        
        control.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                policy.setEnabled(control.getSelectedIndex() == EditNode.RESERVATIONS);
                save.setEnabled(true);
            }
        });
        
        color = new JColorButton()
        {
            public void setColor(Color c)
            {
                save.setEnabled(true);
                super.setColor(c);
            }
        };
        
        JButton cancel = new JButton("Cancel");
        
        setLayout(new GridBagLayout());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Match"));
        constrain(p, new JLabel("Type: "), 0, 0, 1, 1);
        constrain(p, type, 1, 0, 1, 1);
        constrain(p, new JLabel("Control: "), 0, 1, 1, 1);
        constrain(p, control, 1, 1, 1, 1);
        constrain(p, new JLabel("Policy: "), 0, 2, 1, 1);
        constrain(p, policy, 1, 2, 1, 1);
        
        constrain(this, p, 0, 0, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Display"));
        
        constrain(p, new JLabel("Color: "), 0, 0, 1, 1);
        constrain(p, color, 1, 0, 1, 1);
        constrain(p, new JLabel("Radius: "), 0, 1, 1, 1);
        constrain(p, width, 1, 1, 1, 1);
        
        constrain(this, p, 0, 1, 2, 1);
        constrain(this, save, 0, 1, 1, 1);
        constrain(this, cancel, 1, 1, 1, 1);
        
        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cancel();
            }
        });
        
        save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if(save())
                {
                    cancel();
                }
            }
        });
        
        
        
        type.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                save.setEnabled(true);
            }
        });
        width.getDocument().addDocumentListener(new DocumentListener()
        {
            public void insertUpdate(DocumentEvent e)
            {
                save.setEnabled(true);
            }
            public void changedUpdate(DocumentEvent e)
            {
                save.setEnabled(true);
            }
            public void removeUpdate(DocumentEvent e)
            {
                save.setEnabled(true);
            }
        });
        
        setMinimumSize(getPreferredSize());
    }
    
    public NodeTypeRulePanel(NodeTypeRule prev)
    {
        this();
        this.prev = prev;
        
        width.setText(""+prev.getRadius());
        color.setColor(prev.getColor());
        policy.setSelectedIndex(prev.getPolicy());
        control.setSelectedIndex(prev.getControl());
        type.setSelectedIndex(prev.getType());
        save.setEnabled(false);
    }
    
    public boolean save()
    {
        int width_ = 0;
        
        try
        {
            width_ = Integer.parseInt(width.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Width must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
            width.requestFocus();
            return false;
        }
        
        if(width_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Width must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
            width.requestFocus();
            return false;
        }
        
        
        if(prev == null)
        {
            prev = new NodeTypeRule(type.getSelectedIndex(), control.getSelectedIndex(), policy.getSelectedIndex(),
                    color.getColor(), width_);
            addRule(prev);
        }
        else
        {
            prev.setRadius(width_);
            prev.setColor(color.getColor());
            prev.setType(type.getSelectedIndex());
            prev.setControl(control.getSelectedIndex());
            prev.setPolicy(policy.getSelectedIndex());
            saveRule();
        }
        
        return true;
    }
    
    public void addRule(NodeTypeRule rule)
    {
        
    }
    
    public void saveRule(){}
    public void cancel(){}
}

