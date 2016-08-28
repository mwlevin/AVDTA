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
import avdta.network.node.PriorityTBR;
import avdta.network.node.StopSign;
import avdta.network.node.TBR;
import avdta.network.node.TrafficSignal;
import avdta.network.node.obj.BackPressureObj;
import avdta.network.node.obj.ObjFunction;
import avdta.network.node.obj.P0Obj;
import avdta.network.node.policy.AuctionPolicy;
import avdta.network.node.policy.FCFSPolicy;
import avdta.network.node.policy.IntersectionPolicy;
import avdta.network.node.policy.MCKSPriority;
import avdta.network.node.policy.MCKSTBR;

/**
 *
 * @author micha
 */
public class EditNode extends JPanel
{
    private static final String[] TYPES = new String[]{"Centroid", "Intersection"};
    
    private static final int CENTROID = 0;
    private static final int INTERSECTION = 1;
    
    private static final String[] CONTROLS = new String[]{"Signals", "Stop sign", "Reservations"};
    
    private static final int SIGNALS = 0;
    private static final int STOP_SIGN = 1;
    private static final int RESERVATIONS = 2;
    
    private static final String[] POLICIES = new String[]{"FCFS", "Auction", "Backpressure", "P0"};
    
    private static final int FCFS = 0;
    private static final int AUCTION = 1;
    private static final int BACKPRESSURE = 2;
    private static final int P0 = 3;
    
    private Location loc;
    private Editor editor;
    
    private JTextField id;
    private JComboBox type, control, policy;

    private Node prev;
    
    public EditNode(Editor editor)
    {
        id = new JTextField(6);
        
        type = new JComboBox(TYPES);
        control = new JComboBox(CONTROLS);
        policy = new JComboBox(POLICIES);
        
        control.setEnabled(false);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        constrain(p2, new JLabel("Id: "), 0, 0, 1, 1);
        constrain(p2, id, 1, 0, 1, 1);
        constrain(p2, new JLabel("Type: "), 0, 1, 1, 1);
        constrain(p2, type, 1, 1, 1, 1);
        constrain(p2, new JLabel("Control: "), 0, 2, 1, 1);
        constrain(p2, control, 1, 2, 1, 1);
        constrain(p2, new JLabel("Policy: "), 0, 3, 1, 1);
        constrain(p2, policy, 1, 3, 1, 1);
        
        constrain(p, p2, 0, 0, 2, 1);
        
        JButton save = new JButton("Save");
        
        save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                save();
            }
        });
        
        JButton cancel = new JButton("Cancel");
        
        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cancel();
            }
        });
        
        constrain(p, save, 0, 1, 1, 1);
        constrain(p, cancel, 1, 1, 1, 1);
                
              
        
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Node", p);

        policy.setEnabled(false);
        
        control.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    policy.setEnabled(control.getSelectedIndex() == RESERVATIONS);
                }
            }    
        });
        
        type.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    boolean enable = type.getSelectedIndex() != CENTROID;
                    policy.setEnabled(enable && control.getSelectedIndex() == RESERVATIONS);
                    control.setEnabled(enable);
                }
            }    
        });
        
        
        add(tabs);
    }
    
    public EditNode(Editor editor, Node node)
    {
        this(editor);
        this.editor = editor;
        this.prev = node;
        this.loc = node;
        
        id.setText(""+prev.getId());
        
        if(node instanceof Zone)
        {
            type.setSelectedIndex(CENTROID);
        }
        else
        {
            Intersection i = (Intersection)node;
            
            type.setSelectedIndex(INTERSECTION);
            
            if(i.getControl() instanceof TrafficSignal)
            {
                control.setSelectedIndex(SIGNALS);
            }
            else if(i.getControl() instanceof StopSign)
            {
                control.setSelectedIndex(STOP_SIGN);
            }
            else if(i.getControl() instanceof TBR)
            {
                control.setSelectedIndex(RESERVATIONS);
                
                if(i.getControl() instanceof PriorityTBR)
                {
                    IntersectionPolicy poly = ((PriorityTBR)i.getControl()).getPolicy();
                    
                    if(poly instanceof FCFSPolicy)
                    {
                        policy.setSelectedIndex(FCFS);
                    }
                    else if(poly instanceof AuctionPolicy)
                    {
                        policy.setSelectedIndex(AUCTION);
                    }
                    else if(poly instanceof MCKSPriority)
                    {
                        MCKSPriority mcks = (MCKSPriority)poly;
                        
                        ObjFunction obj = mcks.getObj();
                        
                        if(obj instanceof BackPressureObj)
                        {
                            policy.setSelectedIndex(BACKPRESSURE);
                        }
                        else if(obj instanceof P0Obj)
                        {
                            policy.setSelectedIndex(P0);
                        }
                    }
                }
            }
        }
    }

    public void setLocation(Location loc)
    {
        this.loc = loc;
    }
    
    public void save()
    {
        int id_ = 0;
        
        try
        {
            id_ = Integer.parseInt(id.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Id must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return;
        }
        
        if(id_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Id must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return;
        }
        
        if((prev == null || prev.getId() != id_) && editor.getNode(id_) != null)
        {
            JOptionPane.showMessageDialog(this, "Duplicate id", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return;
        }
        
        Node node = null;
        
        if(id_ != node.getId())
        {
            node = new Zone(id_, loc);
            editor.replaceNode(prev, node);
        }
        else
        {
            switch(type.getSelectedIndex())
            {
                case CENTROID:
                    if(prev instanceof Intersection)
                    {
                        node = new Zone(id_, loc);
                        editor.replaceNode(prev, node);
                    }
                    break;
                case INTERSECTION:
                    if(prev instanceof Zone)
                    {
                        node = new Intersection(id_, loc, null);
                        editor.replaceNode(prev, node);
                    }
                    break;
            }
        }
        
        saveNode(node);
    }
    public void cancel(){}
    
    public void saveNode(Node node)
    {
        if(node instanceof Intersection)
        {
            Intersection i = (Intersection)node;
            
            switch(control.getSelectedIndex())
            {
                case SIGNALS:
                    i.setControl(new TrafficSignal());
                    break;
                case STOP_SIGN:
                    i.setControl(new StopSign());
                    break;
                case RESERVATIONS:
                    switch(policy.getSelectedIndex())
                    {
                        case FCFS:
                            i.setControl(new PriorityTBR(new FCFSPolicy()));
                            break;
                        case AUCTION:
                            i.setControl(new PriorityTBR(new AuctionPolicy()));
                            break;
                        case BACKPRESSURE:
                            i.setControl(new MCKSTBR(new BackPressureObj()));
                            break;
                        case P0:
                            i.setControl(new MCKSTBR(new P0Obj()));
                            break;
                    }
                    break;
            }
        }
        
        Signalized signal = node.getSignal();
        
        if(signal != null)
        {
            saveSignal(signal);
        }
    }
    
    public void saveSignal(Signalized signal)
    {
        
    }
}