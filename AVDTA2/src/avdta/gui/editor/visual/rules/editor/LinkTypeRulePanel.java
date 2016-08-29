/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.editor;

import avdta.gui.editor.EditLink;
import avdta.gui.editor.visual.rules.LinkTypeRule;
import avdta.gui.util.JColorButton;
import avdta.network.link.CACCLTMLink;
import avdta.network.link.CTMLink;
import avdta.network.link.CentroidConnector;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.SharedTransitCTMLink;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import static avdta.gui.util.GraphicUtils.*;
import java.awt.Color;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 *
 * @author ml26893
 */
public class LinkTypeRulePanel extends JPanel
{
    private JComboBox type;
    private JButton save;
    private JColorButton color;
    private JTextField width;
    
    private LinkTypeRule prev;
    
    public LinkTypeRulePanel()
    {
        save = new JButton("Save");
        
        type = new JComboBox(EditLink.FLOW_MODELS);
        
        width = new JTextField(3);
        width.setText("3");
        
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
        constrain(p, new JLabel("Flow model: "), 0, 0, 1, 1);
        constrain(p, type, 1, 0, 1, 1);
        
        constrain(this, p, 0, 0, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Display"));
        constrain(p, new JLabel("Color: "), 0, 0, 1, 1);
        constrain(p, color, 1, 0, 1, 1);
        constrain(p, new JLabel("Width: "), 0, 1, 1, 1);
        constrain(p, width, 1, 1, 1, 1);
        
        constrain(this, p, 0, 1, 2, 1);
        constrain(this, save, 0, 2, 1, 1);
        constrain(this, cancel, 1, 2, 1, 1);
        
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
    
    public LinkTypeRulePanel(LinkTypeRule prev)
    {
        this();
        this.prev = prev;
        
        width.setText(""+prev.getWidth());
        color.setColor(prev.getColor());
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
            prev = new LinkTypeRule(type.getSelectedIndex(), color.getColor(), width_);
            addRule(prev);
        }
        else
        {
            prev.setWidth(width_);
            prev.setColor(color.getColor());
            prev.setType(type.getSelectedIndex());
            saveRule();
        }
        
        return true;
    }
    
    public void addRule(LinkTypeRule rule)
    {
        
    }
    
    public void saveRule(){}
    public void cancel(){}
}
