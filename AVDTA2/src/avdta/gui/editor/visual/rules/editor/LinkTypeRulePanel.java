/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.editor;

import avdta.gui.editor.EditLink;
import avdta.gui.editor.visual.rules.LinkBusRule;
import avdta.gui.editor.visual.rules.LinkRule;
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
import avdta.network.ReadNetwork;
import avdta.network.type.Type;
import avdta.project.Project;
import avdta.util.Util;
import java.awt.Color;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;

/**
 *
 * @author ml26893
 */
public abstract class LinkTypeRulePanel extends JPanel implements AbstractLinkRulePanel
{
    private JComboBox type;
    private JButton save;
    private JColorButton color;
    private JTextField width;
    
    private LinkTypeRule prev;
    
    private Project project;
    
    private static Type[] types;
    
    public LinkTypeRulePanel(Project project)
    {
        this.project = project;
        save = new JButton("Save");
        
        if(types == null)
        {
            types = new Type[ReadNetwork.LINK_ALL_OPTIONS.length+2];
            for(int i = 0; i < ReadNetwork.LINK_ALL_OPTIONS.length; i++)
            {
                types[i] = ReadNetwork.LINK_ALL_OPTIONS[i];
            }
            types[types.length-2] = new Type(10000, "Bus");
            types[types.length-1] = new Type(10001, "DTL");
        }
        
        type = new JComboBox(types);
        
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
    
    public LinkTypeRulePanel(Project project, LinkTypeRule prev)
    {
        this(project);
        this.prev = prev;
        
        width.setText(""+prev.getWidth());
        color.setColor(prev.getColor());
        type.setSelectedIndex(Util.indexOf(types, prev.getType()));
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
        
        
        
        if(type.getSelectedIndex() < ReadNetwork.LINK_ALL_OPTIONS.length)
        {
            if(prev == null)
            {
                prev = new LinkTypeRule((Type)type.getSelectedItem(), color.getColor(), width_);
                addRule(prev);
            }
            else
            {
                prev.setWidth(width_);
                prev.setColor(color.getColor());
                prev.setType((Type)type.getSelectedItem());
                saveRule(prev);
            }
        }
        else
        {
            if(type.getSelectedIndex() == ReadNetwork.LINK_ALL_OPTIONS.length)
            {
                addRule(new LinkBusRule(project, false));  
            }
            else if(type.getSelectedIndex() == ReadNetwork.LINK_ALL_OPTIONS.length+1)
            {
                addRule(new LinkBusRule(project, true));   
            }
        }
        
        
        return true;
    }
    
    
}
