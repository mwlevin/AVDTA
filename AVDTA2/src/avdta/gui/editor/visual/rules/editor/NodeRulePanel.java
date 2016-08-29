/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.editor;

import avdta.gui.GUI;
import avdta.gui.editor.Editor;
import avdta.gui.editor.visual.rules.LinkDataRule;
import avdta.gui.editor.visual.rules.LinkRule;
import avdta.gui.editor.visual.rules.LinkTypeRule;
import avdta.gui.editor.visual.rules.NodeDataRule;
import avdta.gui.editor.visual.rules.NodeFileRule;
import avdta.gui.editor.visual.rules.NodeRule;
import avdta.gui.editor.visual.rules.NodeTypeRule;
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
public class NodeRulePanel extends JPanel
{
    private JList list;
    
    private JButton up, down, newType, newData, newFile, edit, remove;
    
    private List<NodeRule> rules;
    private Editor editor;
    
    public NodeRulePanel(Editor editor_, List<NodeRule> rules_)
    {
        this.editor = editor_;
        this.rules = rules_;
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createTitledBorder("Nodes"));
        
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
                list.setSelectedIndex(idx-1);
            }
        });
        
        down.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = list.getSelectedIndex();
                
                NodeRule rule = rules.remove(idx);
                rules.add(idx+1, rule);
                refresh();
                list.setSelectedIndex(idx+1);
            }
        });
        
        edit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                final JInternalFrame frame;
                
                NodeRule rule = rules.get(list.getSelectedIndex());
                
                if(rule instanceof NodeTypeRule)
                {
                    frame = new JInternalFrame("Edit type rule");
                     
                    frame.add(new NodeTypeRulePanel((NodeTypeRule)rule)
                    {
                        public void cancel()
                        {
                            super.cancel();
                            frame.setVisible(false);
                        }
                        public void addRule(NodeTypeRule rule)
                        {
                            newRule(rule);
                        }

                        public void saveRule()
                        {
                            editor.getMap().repaint();
                        }
                    });
                }
                else if(rule instanceof NodeDataRule)
                {
                    frame = new JInternalFrame("Edit data rule");
                     
                    frame.add(new NodeDataRulePanel((NodeDataRule)rule)
                    {
                        public void cancel()
                        {
                            super.cancel();
                            frame.setVisible(false);
                        }
                        public void addRule(NodeDataRule rule)
                        {
                            newRule(rule);
                        }

                        public void saveRule()
                        {
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
                        newRule(new NodeFileRule(chooser.getSelectedFile()));
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

                frame.add(new NodeTypeRulePanel()
                {
                    public void cancel()
                    {
                        super.cancel();
                        frame.setVisible(false);
                    }
                    
                    public void addRule(NodeTypeRule rule)
                    {
                        newRule(rule);
                    }
                    
                    public void saveRule()
                    {
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

                frame.add(new NodeDataRulePanel()
                {
                    public void cancel()
                    {
                        super.cancel();
                        frame.setVisible(false);
                    }
                    
                    public void addRule(NodeDataRule rule)
                    {
                        newRule(rule);
                    }
                    
                    public void saveRule()
                    {
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
        editor.repaint();
    }
    
    public void newRule(NodeRule r)
    {
        rules.add(r);
        refresh();
        editor.getMap().repaint();
    }
}
