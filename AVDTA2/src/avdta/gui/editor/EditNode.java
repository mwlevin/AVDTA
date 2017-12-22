/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.node.Intersection;
import avdta.network.node.Location;
import javax.swing.JPanel;
import avdta.network.node.Zone;
import avdta.network.node.Node;
import avdta.network.node.Signalized;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import static avdta.gui.util.GraphicUtils.*;
import avdta.network.ReadNetwork;
import avdta.network.node.Connector;
import avdta.network.node.Diverge;
import avdta.network.node.Merge;
import avdta.network.node.PhasedTBR;
import avdta.network.node.PriorityTBR;
import avdta.network.node.StopSign;
import avdta.network.node.TBR;
import avdta.network.node.TrafficSignal;
import avdta.network.node.obj.BackPressureObj;
import avdta.network.node.obj.MaxPressureObj;
import avdta.network.node.obj.ObjFunction;
import avdta.network.node.obj.P0Obj;
import avdta.network.node.policy.AuctionPolicy;
import avdta.network.node.policy.FCFSPolicy;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.network.node.policy.MCKSPriority;
import avdta.network.node.policy.MCKSTBR;
import avdta.network.node.policy.SignalWeightedTBR;
import avdta.network.node.policy.TransitFirst;
import avdta.network.type.Type;
import avdta.util.Util;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author micha
 */
public class EditNode extends JPanel
{
    public static final Type[] TYPES = new Type[]{ReadNetwork.CENTROID, ReadNetwork.INTERSECTION};
    

    
    public static final Type[] CONTROLS = ReadNetwork.NODE_OPTIONS;
    

    
    public static final Type[] POLICIES = ReadNetwork.NODE_EXT_OPTIONS;
    

    private Location loc;
    private Editor editor;
    
    private JTextField id, lat, lon;
    private JComboBox type, control, policy;
    private JButton editSignal, save;

    private Node prev;
    
    private JCheckBox selected;
    
    
    public EditNode(Editor editor_)
    {
        this.editor = editor_;
        id = new JTextField(6);
        
        type = new JComboBox(TYPES);
        control = new JComboBox(CONTROLS);
        policy = new JComboBox(POLICIES);
        
        lat = new JTextField(8);
        lon = new JTextField(8);
        
        control.setEnabled(false);
        
        editSignal = new JButton("Edit signal");
        
        selected = new JCheckBox("Selected");
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        constrain(p2, new JLabel("Id: "), 0, 0, 1, 1);
        constrain(p2, id, 1, 0, 1, 1);
        constrain(p2, new JLabel("Type: "), 0, 1, 1, 1);
        constrain(p2, type, 1, 1, 1, 1);
        constrain(p, p2, 0, 0, 1, 1);
        
        p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Location"));
        constrain(p2, new JLabel("Latitude: "), 0, 0, 1, 1);
        constrain(p2, lat, 1, 0, 1, 1);
        constrain(p2, new JLabel("Longitude: "), 0, 1, 1, 1);
        constrain(p2, lon, 1, 1, 1, 1);
        
        constrain(p, p2, 0, 1, 1, 1);
                
        
        p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        p2.setBorder(BorderFactory.createTitledBorder("Intersection control"));
        constrain(p2, new JLabel("Control: "), 0, 0, 1, 1);
        constrain(p2, control, 1, 0, 1, 1);
        constrain(p2, new JLabel("Policy: "), 0, 1, 1, 1);
        constrain(p2, policy, 1, 1, 1, 1);
        constrain(p2, editSignal, 0, 2, 2, 1);
        
        constrain(p, p2, 1, 0, 1, 2);
        
        save = new JButton("Save");
        save.setEnabled(false);
        
        JButton cancel = new JButton("Cancel");
        
        p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        constrain(p2, save, 0, 0, 1, 1);
        constrain(p2, cancel, 1, 0, 1, 1);
        constrain(p2, selected, 2, 0, 1, 1);
        
        selected.setEnabled(false);
        
        constrain(p, p2, 0, 2, 2, 1);
        
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
        
        selected.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                if(prev != null)
                {
                    prev.setSelected(selected.isSelected());
                    editor.repaintMap();
                }
            }
        });
        
        id.getDocument().addDocumentListener(changeListener);
        
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
        
        
        
        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cancel();
            }
        });
        
        editSignal.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                editSignal();
            }
        });
        
        editSignal.setEnabled(false);
        
        
                
              
        

        policy.setEnabled(false);
        
        control.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e) 
            {
                if (e.getStateChange() == ItemEvent.SELECTED) 
                {
                    policy.setEnabled(control.getSelectedItem() == ReadNetwork.RESERVATION);
                }
                save.setEnabled(true);
                
                checkEditSignal();
            }    
        });
        
        type.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e) 
            {
                if (e.getStateChange() == ItemEvent.SELECTED) 
                {
                    boolean enable = type.getSelectedItem() != ReadNetwork.CENTROID;
                    policy.setEnabled(enable && control.getSelectedItem() == ReadNetwork.RESERVATION);
                    control.setEnabled(enable);
                }
                save.setEnabled(true);
            }    
        });
        
        policy.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                checkEditSignal();
            }
        });
        
        
        add(p);
        
        setMinimumSize(getPreferredSize());
    }
    
    
    public EditNode(Editor editor_, Node node)
    {
        this(editor_);
        
        this.prev = node;
        this.loc = node;
        
        id.setText(""+prev.getId());
        lat.setText(""+prev.getLat());
        lon.setText(""+prev.getLon());
        
        if(node instanceof Zone)
        {
            type.setSelectedIndex(Util.indexOf(TYPES, ReadNetwork.CENTROID));
        }
        else
        {
            Intersection i = (Intersection)node;
            
            type.setSelectedIndex(Util.indexOf(TYPES, ReadNetwork.INTERSECTION));
            
            control.setSelectedIndex(Util.indexOf(ReadNetwork.NODE_OPTIONS, i.getType().getBase()));
            
            if(i.getControl() instanceof TBR)
            {
                policy.setSelectedIndex(Util.indexOf(ReadNetwork.NODE_EXT_OPTIONS, i.getType()));
            }
        }
        

        selected.setSelected(prev.isSelected());
      
        selected.setEnabled(true);
        
        
        save.setEnabled(false);
        checkEditSignal();
    }
    
    public void checkEditSignal()
    {
        editSignal.setEnabled(control.getSelectedItem() == ReadNetwork.SIGNAL || 
                        (control.getSelectedItem() == ReadNetwork.RESERVATION && 
                                (policy.getSelectedItem() == ReadNetwork.WEIGHTED || policy.getSelectedItem() == ReadNetwork.PHASED)));
    }
    
    public void editSignal()
    {
        if(save.isEnabled())
        {
            int result = JOptionPane.showConfirmDialog(this, "Save changes?", "Unsaved changes", JOptionPane.YES_NO_OPTION);
            
            if(result == JOptionPane.YES_OPTION)
            {
                if(save())
                {
                    cancel();
                }
                else
                {
                    return;
                }
            }
            else
            {
                return;
            }
        }
        
        final JInternalFrame frame = new JInternalFrame("Edit signal");
                        
        frame.add(new EditSignal(prev.getSignal(), prev)
        {

            public void cancel()
            {
                super.cancel();
                frame.setVisible(false);
            }
        });

        cancel();
        
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

    public void setLocation(Location loc)
    {
        this.loc = loc;
        lat.setText(""+loc.getLat());
        lon.setText(""+loc.getLon());
    }
    
    public boolean save()
    {
        int id_ = 0;
        
        double lat_ = 0;
        double lon_ = 0;
        
        try
        {
            id_ = Integer.parseInt(id.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Id must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return false;
        }
        
        if(id_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Id must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return false;
        }
        
        
        
        try
        {
            lon_ = Double.parseDouble(lon.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Longitude must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            lon.requestFocus();
            return false;
        }
        
        if(lon_ < -180 || lon_ > 180)
        {
            JOptionPane.showMessageDialog(this, "Longitude must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            lon.requestFocus();
            return false;
        }
        
        try
        {
            lat_ = Double.parseDouble(lat.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Latitude must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            lat.requestFocus();
            return false;
        }
        
        if(lat_ < -90 || lat_ > 90)
        {
            JOptionPane.showMessageDialog(this, "Latitude must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            lat.requestFocus();
            return false;
        }
        
        if((prev == null || prev.getId() != id_) && editor.getNode(id_) != null)
        {
            JOptionPane.showMessageDialog(this, "Duplicate id", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return false;
        }
        
        Node node = prev;
        
        if(id_ != node.getId())
        {
            node = new Zone(id_, loc);
            editor.replaceNode(prev, node);
        }
        else
        {
            if(type.getSelectedItem() == ReadNetwork.CENTROID)
            {
                if(prev instanceof Intersection)
                {
                    node = new Zone(id_, loc);
                    editor.replaceNode(prev, node);
                }
            }
            else if(type.getSelectedItem() == ReadNetwork.INTERSECTION)
            {
                if(prev instanceof Zone)
                {
                    node = new Intersection(id_, loc, null);
                    editor.replaceNode(prev, node);
                }
            }
            
        }
        
        saveNode(node);
        cancel();
        return true;
    }
    public void cancel(){}
    
    public void saveNode(Node node)
    {
        if(node instanceof Intersection)
        {
            Intersection i = (Intersection)node;
            
            
            Type newType = (Type)control.getSelectedItem();
            if(newType == ReadNetwork.STOPSIGN)
            {
                i.setControl(new StopSign());
            } 
            else if(newType == ReadNetwork.DIVERGE || newType == ReadNetwork.MERGE || newType == ReadNetwork.CONNECTOR)
            {
                if(i.getIncoming().size() == 1)
                {
                    if(i.getOutgoing().size() == 1)
                    {
                        i.setControl(new Connector());
                    }
                    else
                    {
                        i.setControl(new Diverge());
                    }
                }
                else if(i.getOutgoing().size() == 1)
                {
                    i.setControl(new Merge());
                }
            }
            else if(newType == ReadNetwork.SIGNAL)
            {
                i.setControl(new TrafficSignal());
            }
            else if(newType == ReadNetwork.RESERVATION)
            {
                Type p = (Type)policy.getSelectedItem();
                
                if(p == ReadNetwork.FCFS)
                {
                    i.setControl(new PriorityTBR(new FCFSPolicy()));
                }
                else if(p == ReadNetwork.AUCTION)
                {
                    i.setControl(new PriorityTBR(new AuctionPolicy()));
                }
                else if(p == ReadNetwork.PRESSURE)
                {
                    i.setControl(new MCKSTBR(new BackPressureObj()));
                }
                else if(p == ReadNetwork.P0)
                {
                    i.setControl(new MCKSTBR(new P0Obj()));
                }
                else if(p == ReadNetwork.MAX_PRESSURE)
                {
                    i.setControl(new MCKSTBR(new MaxPressureObj()));
                }
                else if(p == ReadNetwork.PHASED)
                {
                    i.setControl(new PhasedTBR());
                }
                else if(p == ReadNetwork.WEIGHTED)
                {
                    i.setControl(new SignalWeightedTBR());
                }
                else if(p == ReadNetwork.TRANSIT_FIRST)
                {
                    i.setControl(new PriorityTBR(new TransitFirst(new FCFSPolicy())));
                }
            }
            else
            {
                JOptionPane.showMessageDialog(this, "Could not find node type", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        prev = node;
    }

}
