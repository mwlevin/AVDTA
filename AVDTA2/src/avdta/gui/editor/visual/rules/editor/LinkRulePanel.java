/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.editor;

import avdta.gui.GUI;
import avdta.gui.editor.visual.rules.editor.LinkDataRulePanel;
import avdta.gui.editor.Editor;
import avdta.gui.editor.visual.rules.LinkDataRule;
import avdta.gui.editor.visual.rules.LinkFileRule;
import avdta.gui.editor.visual.rules.LinkRule;
import avdta.gui.editor.visual.rules.LinkTypeRule;
import java.awt.Component;
import java.util.List;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author micha
 */
public class LinkRulePanel extends JPanel
{
    private JList list;
    
    private JButton up, down, newType, newData, newFile, edit, remove;
    
    private List<LinkRule> rules;
    private Editor editor;
    
    public LinkRulePanel(Editor editor_, List<LinkRule> rules_)
    {
        this.editor = editor_;
        this.rules = rules_;
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Links"));
        
        up = new JButton("↑");
        down = new JButton("↓");
        
        
        newType = new JButton("New type rule");
        newData = new JButton("New data rule");
        newFile = new JButton("New file rule");
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
        list.setFixedCellHeight(15);
        list.setVisibleRowCount(6);
        
        
        
        
        edit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                final JInternalFrame frame;
                
                LinkRule rule = rules.get(list.getSelectedIndex());
                
                if(rule instanceof LinkTypeRule)
                {
                    frame = new JInternalFrame("Edit type rule");
                     
                    frame.add(new LinkTypeRulePanel(editor.getProject(), (LinkTypeRule)rule)
                    {
                        public void cancel()
                        {
                            frame.setVisible(false);
                        }
                        public void addRule(LinkRule rule)
                        {
                            rule.initialize(editor.getProject());
                            newRule(rule);
                        }

                        public void saveRule(LinkRule rule)
                        {
                            rule.initialize(editor.getProject());
                            editor.getDisplay().setDisplaySelected(false);
                            editor.getMap().repaint();
                        }
                    });
                }
                else if(rule instanceof LinkDataRule)
                {
                    frame = new JInternalFrame("Edit data rule");
                     
                    frame.add(new LinkDataRulePanel((LinkDataRule)rule)
                    {
                        public void cancel()
                        {
                            frame.setVisible(false);
                        }
                        public void addRule(LinkRule rule)
                        {
                            rule.initialize(editor.getProject());
                            newRule(rule);
                        }

                        public void saveRule(LinkRule rule)
                        {
                            rule.initialize(editor.getProject());
                            editor.getDisplay().setDisplaySelected(false);
                            editor.getMap().repaint();
                        }
                    });
                }
                else
                {
                    return;
                }

                frame.pack();
                frame.setResizable(false);
                frame.setClosable(true);
                frame.setLocation(editor.getWidth()/2 - frame.getWidth()/2, editor.getHeight()/2 - frame.getHeight()/2);
                frame.setVisible(true);

                JLayeredPane toUse = JLayeredPane.getLayeredPaneAbove(editor.getPanel());
                toUse.add(frame);
                try
                {
                    frame.setSelected(true);
                }
                catch(Exception ex){}
            }
        });
        
        newFile.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Text files", "txt"));
                
                int val = chooser.showOpenDialog(editor);
                if(val == JFileChooser.APPROVE_OPTION)
                {
                    try
                    {
                        newRule(new LinkFileRule(chooser.getSelectedFile()));
                    }
                    catch(IOException ex)
                    {
                        GUI.handleException(ex);
                    }
                }
            }
        });
        
        newType.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                final JInternalFrame frame = new JInternalFrame("New type rule");

                frame.add(new LinkTypeRulePanel(editor.getProject())
                {
                    public void cancel()
                    {
                        frame.setVisible(false);
                    }
                    
                    public void addRule(LinkRule rule)
                    {
                        rule.initialize(editor.getProject());
                        newRule(rule);
                    }
                    
                    public void saveRule(LinkRule rule)
                    {
                        rule.initialize(editor.getProject());
                        editor.getDisplay().setDisplaySelected(false);
                        editor.getMap().repaint();
                    }
                });
    
                frame.pack();
                frame.setResizable(false);
                frame.setClosable(true);
                frame.setLocation(editor.getWidth()/2 - frame.getWidth()/2, editor.getHeight()/2 - frame.getHeight()/2);
                frame.setVisible(true);

                JLayeredPane toUse = JLayeredPane.getLayeredPaneAbove(editor.getPanel());
                toUse.add(frame);
                try
                {
                    frame.setSelected(true);
                }
                catch(Exception ex){}
            }
        });
        
        newData.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                final JInternalFrame frame = new JInternalFrame("New data rule");

                frame.add(new LinkDataRulePanel()
                {
                    public void cancel()
                    {
                        frame.setVisible(false);
                    }
                    
                    public void addRule(LinkRule rule)
                    {
                        newRule(rule);
                    }
                    
                    public void saveRule(LinkRule rule)
                    {
                        editor.getDisplay().setDisplaySelected(false);
                        editor.getMap().repaint();
                    }
                });
    
                frame.pack();
                frame.setResizable(false);
                frame.setClosable(true);
                frame.setLocation(editor.getWidth()/2 - frame.getWidth()/2, editor.getHeight()/2 - frame.getHeight()/2);
                frame.setVisible(true);

                JLayeredPane toUse = JLayeredPane.getLayeredPaneAbove(editor.getPanel());
                toUse.add(frame);
                try
                {
                    frame.setSelected(true);
                }
                catch(Exception ex){}
            }
        });
        
        
        remove.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                rules.remove(list.getSelectedIndex());
                editor.getDisplay().setDisplaySelected(false);
                refresh();
            }
        });
        
        up.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = list.getSelectedIndex();
                
                LinkRule rule = rules.remove(idx);
                rules.add(idx-1, rule);
                refresh();
                list.setSelectedIndex(idx-1);
            }
        });
        
        down.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = list.getSelectedIndex();
                
                LinkRule rule = rules.remove(idx);
                rules.add(idx+1, rule);
                refresh();
                list.setSelectedIndex(idx+1);
            }
        });
        
        
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                boolean enable = list.getSelectedIndex() >= 0;
                
                up.setEnabled(enable && list.getSelectedIndex() > 0);
                down.setEnabled(enable && list.getSelectedIndex() < rules.size()-1);
                
                if(enable)
                {
                     LinkRule rule = rules.get(list.getSelectedIndex());
                     
                     edit.setEnabled((rule instanceof LinkTypeRule) || (rule instanceof LinkDataRule));
                }
                else
                {
                    edit.setEnabled(false);
                }
                remove.setEnabled(enable);
            }
        });
        
        constrain(this, new JScrollPane(list), 0, 0, 3, 1);
        constrain(this, up, 0, 1, 1, 1);
        constrain(this, down, 1, 1, 1, 1);
        constrain(this, remove, 2, 1, 1, 1);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        constrain(p, newType, 0, 0, 1, 1);
        constrain(p, newData, 0, 1, 1, 1);
        constrain(p, newFile, 0, 2, 1, 1);
        constrain(p, edit, 0, 3, 1, 1);
        
        constrain(this, p, 3, 0, 1, 2);
        
        setMinimumSize(getPreferredSize());
    }
    
    public void newRule(LinkRule r)
    {
        rules.add(r);
        editor.getDisplay().setDisplaySelected(false);
        refresh();
        editor.getMap().repaint();
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
        LinkRule[] data = new LinkRule[rules.size()];
        
        for(int i = 0; i < data.length; i++)
        {
            data[i] = rules.get(i);
        }
        
        list.setSelectedIndex(-1);
        list.setListData(data);
        editor.repaint();
    }
}
