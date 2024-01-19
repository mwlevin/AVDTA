/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.editor;

import avdta.gui.editor.visual.rules.LinkDataRule;
import avdta.gui.editor.visual.rules.LinkRule;
import avdta.gui.editor.visual.rules.data.LinkDataSource;
import avdta.gui.util.JColorButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static avdta.gui.util.GraphicUtils.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author ml26893
 */
public abstract class LinkDataRulePanel extends JPanel implements AbstractLinkRulePanel
{
    private static final LinkDataSource[] SOURCES = new LinkDataSource[]{LinkDataSource.tt, LinkDataSource.ffspd, LinkDataSource.capacity, 
        LinkDataSource.numLanes, LinkDataSource.volume};
    
    private LinkDataRule prev;
    
    private JTextField minWidth, maxWidth;
    private JColorButton minColor, maxColor;
    private JTextField minValue, maxValue;
    private JButton save;
    
    private JComboBox sources;
    
    public LinkDataRulePanel()
    {
        minWidth = new JTextField(3);
        maxWidth = new JTextField(3);
        minWidth.setText("3");
        maxWidth.setText("3");
        
        save = new JButton("Save");
        JButton cancel = new JButton("Cancel");
        
        sources = new JComboBox(SOURCES);
        sources.setRenderer(new DefaultListCellRenderer()
        {
           public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
           {
               JComponent comp = (JComponent)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
               
               if(value != null)
               {
                   list.setToolTipText(((LinkDataSource)value).getDescription());
               }
               return comp;
           }
        });
        
        minValue = new JTextField(4);
        maxValue = new JTextField(4);
        
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
        
        minColor = new JColorButton()
        {
            public void setColor(Color c)
            {
                save.setEnabled(true);
                super.setColor(c);
            }
        };
        
        maxColor = new JColorButton()
        {
            public void setColor(Color c)
            {
                save.setEnabled(true);
                super.setColor(c);
            }
        };
        

        sources.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                save.setEnabled(true);
            }
        });
        
        DocumentListener changeListener = new DocumentListener()
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
        };
        
        minWidth.getDocument().addDocumentListener(changeListener);
        maxWidth.getDocument().addDocumentListener(changeListener);
        minValue.getDocument().addDocumentListener(changeListener);
        maxValue.getDocument().addDocumentListener(changeListener);
        
        setLayout(new GridBagLayout());
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JLabel("Data: "), 0, 0, 1, 1);
        constrain(p, sources, 1, 0, 1, 1);
        
        constrain(this, p, 0, 0, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("Bounds"));
        constrain(p, new JLabel("Min: "), 1, 0, 1, 1);
        constrain(p, new JLabel("Max: "), 2, 0, 1, 1);
        
        constrain(p, new JLabel("Value: "), 0, 1, 1, 1);
        constrain(p, minValue, 1, 1, 1, 1);
        constrain(p, maxValue, 2, 1, 1, 1);
        
        constrain(p, new JLabel("Color: "), 0, 2, 1, 1);
        constrain(p, minColor, 1, 2, 1, 1);
        constrain(p, maxColor, 2, 2, 1, 1);
        
        constrain(p, new JLabel("Width: "), 0, 3, 1, 1);
        constrain(p, minWidth, 1, 3, 1, 1);
        constrain(p, maxWidth, 2, 3, 1, 1);
        
        constrain(this, p, 0, 1, 2, 1);
        constrain(this, save, 0, 2, 1, 1);
        constrain(this, cancel, 1, 2, 1, 1);
        
        
        
        
        setMinimumSize(getPreferredSize());
    }
    
    public LinkDataRulePanel(LinkDataRule prev)
    {
        this();
        this.prev = prev;
        
        minColor.setColor(prev.getMinColor());
        maxColor.setColor(prev.getMaxColor());
        minWidth.setText(""+prev.getMinWidth());
        maxWidth.setText(""+prev.getMaxWidth());
        minValue.setText(""+prev.getMinValue());
        maxValue.setText(""+prev.getMaxValue());
        
        for(int i = 0; i < SOURCES.length; i++)
        {
            if(SOURCES[i] == prev.getDataSource())
            {
                sources.setSelectedIndex(i);
                break;
            }
        }
    }

    public boolean save()
    {
        int minWidth_ = 0;
        int maxWidth_ = 0;
        double minValue_ = 0;
        double maxValue_ = 0;
        
        try
        {
            minValue_ = Double.parseDouble(minValue.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Min. value must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            minValue.requestFocus();
            return false;
        }
        
        try
        {
            maxValue_ = Double.parseDouble(maxValue.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Max. value must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            maxValue.requestFocus();
            return false;
        }
        
        try
        {
            minWidth_ = Integer.parseInt(minWidth.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Min. width must be a non-negative integer.", "Error", JOptionPane.ERROR_MESSAGE);
            minWidth.requestFocus();
            return false;
        }
        
        if(minWidth_ < 0)
        {
            JOptionPane.showMessageDialog(this, "Min. width must be a non-negative  integer.", "Error", JOptionPane.ERROR_MESSAGE);
            minWidth.requestFocus();
            return false;
        }
        
        try
        {
            maxWidth_ = Integer.parseInt(maxWidth.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Max. width must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
            maxWidth.requestFocus();
            return false;
        }
        
        if(maxWidth_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Max. width must be a positive integer.", "Error", JOptionPane.ERROR_MESSAGE);
            maxWidth.requestFocus();
            return false;
        }
        
        
        LinkDataSource source = (LinkDataSource)sources.getSelectedItem();
        
        
        boolean addRule = false;
        
        if(prev == null)
        {
            prev = new LinkDataRule();
            addRule = true;
        }
        
        prev.setMinWidth(minWidth_);
        prev.setMaxWidth(maxWidth_);
        prev.setMinColor(minColor.getColor());
        prev.setMaxColor(maxColor.getColor());
        prev.setMinValue(minValue_);
        prev.setMaxValue(maxValue_);
        prev.setDataSource(source);
        
        
        
        if(addRule)
        {
            addRule(prev);
        }
        else
        {
            saveRule(prev);
        }
        
        return true;
    }
}
