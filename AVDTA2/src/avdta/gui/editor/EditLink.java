/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.network.link.Link;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static avdta.gui.util.GraphicUtils.*;
import avdta.network.link.CACCLTMLink;
import avdta.network.link.CTMLink;
import avdta.network.link.CentroidConnector;
import avdta.network.link.DLRCTMLink;
import avdta.network.link.LTMLink;
import avdta.network.link.SharedTransitCTMLink;
import avdta.network.link.TransitLane;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author micha
 */
public class EditLink extends JPanel
{
    private static final Integer[] LANE_OPTIONS = new Integer[]{1, 2, 3, 4, 5, 6};
    private static final String[] FLOW_MODELS = new String[]{"Centroid", "CTM", "LTM", "Shared transit CTM", "DLR CTM", "CACC LTM"};
    
    private static final int CTM = 1;
    private static final int LTM = 2;
    private static final int CENTROID = 0;
    private static final int SHARED_TRANSIT_CTM = 3;
    private static final int DLR_CTM = 4;
    private static final int CACC_LTM = 5;
    
    
    private Editor editor;
    private Link prev;
    
    private JTextField id, source, dest, capacity, ffspd, wavespd, length;
    private JComboBox numLanes, flowModel;
    
    public EditLink(Editor editor)
    {
        this(editor, null);
    }
    
    public EditLink(Editor editor, Node n1, Node n2)
    {
        this(editor, null);
        source.setText(""+n1.getId());
        dest.setText(""+n2.getId());
    }
    public EditLink(Editor editor, Link prev)
    {
        this.prev = prev;
        this.editor = editor;
        
        setLayout(new GridBagLayout());
        
        id = new JTextField(6);
        source = new JTextField(6);
        dest = new JTextField(6);
        
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Location"));
        p.setLayout(new GridBagLayout());
        constrain(p, new JLabel("Id: "), 0, 0, 1, 1);
        constrain(p, id, 1, 0, 1, 1);
        constrain(p, new JLabel("Source: "), 0, 1, 1, 1);
        constrain(p, source, 1, 1, 1, 1);
        constrain(p, new JLabel("Destination: "), 0, 2, 1, 1);
        constrain(p, dest, 1, 2, 1, 1);
        
        constrain(this, p, 0, 0, 1, 1);
        
        capacity = new JTextField(6);
        ffspd = new JTextField(6);
        wavespd = new JTextField(6);
        length = new JTextField(6);
        
        numLanes = new JComboBox(LANE_OPTIONS);
        flowModel = new JComboBox(FLOW_MODELS);
        
        p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Characteristics"));
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JLabel("Length: "), 0, 0, 1, 1);
        constrain(p, length, 1, 0, 1, 1);
        constrain(p, new JLabel("ft"), 2, 0, 1, 1);
        constrain(p, new JLabel("Lanes: "), 0, 1, 1, 1);
        constrain(p, numLanes, 1, 1, 2, 1);
        
        constrain(this, p, 1, 0, 1, 1);
        
        p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Flow model"));
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JLabel("Capacity: "), 0, 1, 1, 1);
        constrain(p, capacity, 1, 1, 1, 1);
        constrain(p, new JLabel("vph per lane"), 2, 1, 1, 1);
        constrain(p, new JLabel("Free flow speed: "), 0, 2, 1, 1);
        constrain(p, ffspd, 1, 2, 1, 1);
        constrain(p, new JLabel("mph"), 2, 2, 1, 1);
        constrain(p, new JLabel("Congested wave speed: "), 0, 3, 1, 1);
        constrain(p, wavespd, 1, 3, 1, 1);
        constrain(p, new JLabel("mph"), 2, 3, 1, 1);
        constrain(p, new JLabel("Flow model: "), 0, 0, 1, 1);
        constrain(p, flowModel, 1, 0, 2, 1);
        
        constrain(this, p, 0, 1, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        JButton save = new JButton("Save");
        
        JButton cancel = new JButton("Cancel");
        
        constrain(p, save, 0, 0, 1, 1);
        constrain(p, cancel, 1, 0, 1, 1);
        
        constrain(this, p, 0, 2, 1, 1);
        
        save.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                save();
            }
        });
        
        cancel.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                cancel();
            }
        });
        
        loadLink(prev);
    }
    
    public void cancel()
    {
        
    }
        
    
    public void loadLink(Link prev)
    {
        if(prev == null)
        {
            numLanes.setSelectedIndex(1);
            source.setText("");
            dest.setText("");
            id.setText("");
            ffspd.setText("");
            wavespd.setText("");
            flowModel.setSelectedIndex(0);
            length.setText("");
            capacity.setText("");
        }
        else
        {
            id.setText(""+prev.getId());
            numLanes.setSelectedIndex(prev.getNumLanes()-1);
            capacity.setText(String.format("%.2f", prev.getCapacityPerLane()));
            source.setText(""+prev.getSource().getId());
            dest.setText(""+prev.getDest().getId());
            ffspd.setText(String.format("%.2f", prev.getFFSpeed()));
            wavespd.setText(String.format("%.2f", prev.getWaveSpeed()));
            length.setText(String.format("%.1f", prev.getLength()*5280));
            
            if(prev.isCentroidConnector())
            {
                flowModel.setSelectedIndex(CENTROID);
            }
            else if(prev instanceof SharedTransitCTMLink)
            {
                flowModel.setSelectedIndex(SHARED_TRANSIT_CTM);
            }
            else if(prev instanceof DLRCTMLink)
            {
                flowModel.setSelectedIndex(DLR_CTM);
            }
            else if(prev instanceof CTMLink)
            {
                flowModel.setSelectedIndex(CTM);
            }
            else if(prev instanceof CACCLTMLink)
            {
                flowModel.setSelectedIndex(CACC_LTM);
            }
            else if(prev instanceof LTMLink)
            {
                flowModel.setSelectedIndex(LTM);
            }
        }
    }
    
    public void save()
    {
        int source_id = 0;
        int dest_id = 0;
        int id_ = 0;
        double capacity_ = 0;
        double wavespd_ = 0;
        double ffspd_ = 0;
        double length_ = 0;
        
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
        
        if((prev == null || prev.getId() != id_) && editor.getLink(id_) != null)
        {
            JOptionPane.showMessageDialog(this, "Duplicate id", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return;
        }
        
        try
        {
            dest_id = Integer.parseInt(dest.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Destination must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            dest.requestFocus();
            return;
        }
        
        try
        {
            source_id = Integer.parseInt(source.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Source must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            source.requestFocus();
            return;
        }
        
        if(source_id <= 0)
        {
            JOptionPane.showMessageDialog(this, "Source must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            source.requestFocus();
            return;
        }
        
        try
        {
            wavespd_ = Double.parseDouble(wavespd.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Congested wave speed must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            wavespd.requestFocus();
            return;
        }
        
        if(wavespd_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Congested wave speed must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            wavespd.requestFocus();
            return;
        }
        
        try
        {
            capacity_ = Double.parseDouble(capacity.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            capacity.requestFocus();
            return;
        }
        
        if(capacity_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            capacity.requestFocus();
            return;
        }
        
        try
        {
            ffspd_ = Double.parseDouble(ffspd.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Free flow speed must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            ffspd.requestFocus();
            return;
        }
        
        if(ffspd_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Free flow speed must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            ffspd.requestFocus();
            return;
        }
        
        try
        {
            length_ = Double.parseDouble(length.getText().trim())/5280;
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Length must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            length.requestFocus();
            return;
        }
        
        if(length_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Length must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
            length.requestFocus();
            return;
        }
        
        Node source_ = editor.getNode(source_id);
        Node dest_ = editor.getNode(dest_id);
        
        if(source_ == null)
        {
            JOptionPane.showMessageDialog(this, "Node "+source_id+" not found", "Error", JOptionPane.ERROR_MESSAGE);
            source.requestFocus();
            return;
        }
        
        if(dest_ == null)
        {
            JOptionPane.showMessageDialog(this, "Node "+dest_id+" not found", "Error", JOptionPane.ERROR_MESSAGE);
            dest.requestFocus();
            return;
        }
        
        int numLanes_ = numLanes.getSelectedIndex()+1;
        
        double jamd = 5280.0/Vehicle.vehicle_length;
        
        Link newLink = null;
        
        switch(flowModel.getSelectedIndex())
        {
            case CTM:
                newLink = new CTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_);
                break;
            case LTM:
                newLink = new LTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_);
                break;
            case CENTROID:
                newLink = new CentroidConnector(id_, source_, dest_);
                break;
            case SHARED_TRANSIT_CTM:
                TransitLane lane = new TransitLane(-id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_);
                newLink = new SharedTransitCTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_-1, lane);
                break;
            case DLR_CTM:
                newLink = new DLRCTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_);
                break;
            case CACC_LTM:
                newLink = new CACCLTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Could not find flow model", "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }
        
        saveLink(prev, newLink);
    }
    
    public void saveLink(Link prev, Link newLink)
    {
        editor.saveLink(prev, newLink);
    }
}
