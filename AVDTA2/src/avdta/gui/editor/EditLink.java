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
import avdta.network.link.SplitCTMLink;
import avdta.network.link.TransitLane;
import avdta.network.node.Location;
import avdta.network.node.Node;
import avdta.vehicle.Vehicle;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author micha
 */
public class EditLink extends JPanel implements SelectListener
{
    public static final Integer[] LANE_OPTIONS = new Integer[]{1, 2, 3, 4, 5, 6};
    public static final String[] FLOW_MODELS = new String[]{"Centroid", "CTM", "LTM", "Shared transit CTM", "Split transit CTM", "DLR CTM", "CACC LTM"};
    
    public static final int CTM = 1;
    public static final int LTM = 2;
    public static final int CENTROID = 0;
    public static final int SHARED_TRANSIT_CTM = 3;
    public static final int SPLIT_TRANSIT_CTM = 4;
    public static final int DLR_CTM = 5;
    public static final int CACC_LTM = 6;
    
    
    private Editor editor;
    private Link prev;
    
    private JTextField id, source, dest, capacity, ffspd, wavespd, length;
    private JComboBox numLanes, flowModel;
    private JButton save;
    
    private JTextField lat, lon;
    private JList coordsList;
    private JButton up, down, remove, add, saveCoord;
    private List<Location> coords;
    
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
    public EditLink(Editor editor_, Link prev)
    {
        this.prev = prev;
        this.editor = editor_;
        
        setLayout(new GridBagLayout());
        
        id = new JTextField(6);
        source = new JTextField(6);
        dest = new JTextField(6);
        
        lat = new JTextField(8);
        lon = new JTextField(8);
        coordsList = new JList();
        coordsList.setListData(new String[]{});
        coordsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coordsList.setFixedCellWidth(150);
        coordsList.setFixedCellHeight(20);
        coordsList.setVisibleRowCount(4);
        
        up = new JButton("↑");
        down = new JButton("↓");
        remove = new JButton("Remove");
        add = new JButton("Add coordinate");
        saveCoord = new JButton("Save coordinate");
        
        coords = new ArrayList<Location>();
        
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder("Id"));
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
        p.setBorder(BorderFactory.createTitledBorder("Location"));
        p.setLayout(new GridBagLayout());
        
        constrain(p, new JScrollPane(coordsList), 0, 0, 3, 1);
        constrain(p, up, 0, 1, 1, 1);
        constrain(p, down, 1, 1, 1, 1);
        constrain(p, remove, 2, 1, 1, 1);
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        
        constrain(p2, add, 0, 0, 2, 1);
        constrain(p2, new JLabel("Latitude: "), 0, 1, 1, 1);
        constrain(p2, lat, 1, 1, 1, 1);
        constrain(p2, new JLabel("Longitude: "), 0, 2, 1, 1);
        constrain(p2, lon, 1, 2, 1, 1);
        constrain(p2, saveCoord, 0, 3, 2, 1);
        
        constrain(p, p2, 3, 0, 1, 2);
        
        constrain(this, p, 0, 1, 2, 1);
        
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
        
        constrain(this, p, 0, 2, 2, 1);
        
        p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        save = new JButton("Save");
        save.setEnabled(false);
        
        JButton cancel = new JButton("Cancel");
        
        constrain(p, save, 0, 0, 1, 1);
        constrain(p, cancel, 1, 0, 1, 1);
        
        constrain(this, p, 0, 3, 1, 1);
        
        
        saveCoord.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                double lat_ = 0;
                double lon_ = 0;
                
                try
                {
                    lon_ = Double.parseDouble(lon.getText().trim());
                }
                catch(NumberFormatException ex)
                {
                    JOptionPane.showMessageDialog(editor, "Longitude must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                    lon.requestFocus();
                    return;
                }

                if(lon_ < -180 || lon_ > 180)
                {
                    JOptionPane.showMessageDialog(editor, "Longitude must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                    lon.requestFocus();
                    return;
                }

                try
                {
                    lat_ = Double.parseDouble(lat.getText().trim());
                }
                catch(NumberFormatException ex)
                {
                    JOptionPane.showMessageDialog(editor, "Latitude must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                    lat.requestFocus();
                    return;
                }

                if(lat_ < -90 || lat_ > 90)
                {
                    JOptionPane.showMessageDialog(editor, "Latitude must be a number", "Error", JOptionPane.ERROR_MESSAGE);
                    lat.requestFocus();
                    return;
                }
        
                Location loc;
                if(coordsList.getSelectedIndex() >= 0)
                {
                    loc = (Location)coordsList.getSelectedValue();
                    
                }
                else
                {
                    loc = new Location();
                    coords.add(loc);
                }
                
                loc.setLat(lat_);
                loc.setLon(lon_);
                lat.setText("");
                lon.setText("");
                refreshCoords();
            }
        });
        
        coordsList.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                int idx = coordsList.getSelectedIndex();
                up.setEnabled(idx > 0);
                down.setEnabled(idx < coords.size()-1 && idx >= 0);
                remove.setEnabled(idx >= 0);
                
                if(idx >= 0)
                {
                    Location loc = (Location)coordsList.getSelectedValue();
                    lat.setText(""+loc.getLat());
                    lon.setText(""+loc.getLon());
                }
                else
                {
                    lat.setText("");
                    lon.setText("");
                }
            }
        });
        
        remove.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                coords.remove(coordsList.getSelectedIndex());
                refreshCoords();
            }
        });
        
        add.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                coordsList.clearSelection();
                editor.setMode(Editor.POINT);
            }
        });
        editor.addSelectListener(this);
        
        up.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = coordsList.getSelectedIndex();
                
                Location loc = coords.remove(idx);
                coords.add(idx-1, loc);
                refreshCoords();
                coordsList.setSelectedIndex(idx-1);
            }
        });
        down.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                int idx = coordsList.getSelectedIndex();
                
                Location loc = coords.remove(idx);
                coords.add(idx+1, loc);
                refreshCoords();
                coordsList.setSelectedIndex(idx+1);
            }
        });
        up.setEnabled(false);
        down.setEnabled(false);
        remove.setEnabled(false);
        
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
        id.getDocument().addDocumentListener(changeListener);
        dest.getDocument().addDocumentListener(changeListener);
        source.getDocument().addDocumentListener(changeListener);
        capacity.getDocument().addDocumentListener(changeListener);
        ffspd.getDocument().addDocumentListener(changeListener);
        wavespd.getDocument().addDocumentListener(changeListener);
        length.getDocument().addDocumentListener(changeListener);
        
        numLanes.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                save.setEnabled(true);
            }
        });
        
        flowModel.addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent e)
            {
                boolean enable = flowModel.getSelectedIndex() != CENTROID;
                capacity.setEnabled(enable);
                ffspd.setEnabled(enable);
                wavespd.setEnabled(enable);
                length.setEnabled(enable);
                numLanes.setEnabled(enable);
                
                save.setEnabled(true);
            }
        });
        
        editor.addSelectListener(this);
        
        setMinimumSize(getPreferredSize());
        
        loadLink(prev);
    }
    
    public void setPoint(Location loc)
    {
        lat.setText(String.format("%.7f", loc.getLat()));
        lon.setText(String.format("%.7f", loc.getLon()));
    }
    
    public void cancel()
    {
        editor.removeSelectListener(this);
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
            coordsList.setListData(new String[]{});
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
            
            Location[] c = prev.getCoordinates();
            coords.clear();

            for(int i = 1; i < c.length-1; i++)
            {
                coords.add(c[i]);
            }
            
            refreshCoords();
            
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
        
        save.setEnabled(false);
    }
    
    public void refreshCoords()
    {
        coordsList.setListData(coords.toArray());
    }

    public boolean save()
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
            return false;
        }
        
        if(id_ <= 0)
        {
            JOptionPane.showMessageDialog(this, "Id must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return false;
        }
        
        if((prev == null || prev.getId() != id_) && editor.getLink(id_) != null)
        {
            JOptionPane.showMessageDialog(this, "Duplicate id", "Error", JOptionPane.ERROR_MESSAGE);
            id.requestFocus();
            return false;
        }
        
        try
        {
            dest_id = Integer.parseInt(dest.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Destination must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            dest.requestFocus();
            return false;
        }
        
        try
        {
            source_id = Integer.parseInt(source.getText().trim());
        }
        catch(NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(this, "Source must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            source.requestFocus();
            return false;
        }
        
        if(source_id <= 0)
        {
            JOptionPane.showMessageDialog(this, "Source must be a positive integer", "Error", JOptionPane.ERROR_MESSAGE);
            source.requestFocus();
            return false;
        }
        
        if(flowModel.getSelectedIndex() != CENTROID)
        {
            try
            {
                wavespd_ = Double.parseDouble(wavespd.getText().trim());
            }
            catch(NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(this, "Congested wave speed must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                wavespd.requestFocus();
                return false;
            }

            if(wavespd_ <= 0)
            {
                JOptionPane.showMessageDialog(this, "Congested wave speed must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                wavespd.requestFocus();
                return false;
            }
        
            try
            {
                capacity_ = Double.parseDouble(capacity.getText().trim());
            }
            catch(NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(this, "Capacity must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                capacity.requestFocus();
                return false;
            }
        
            if(capacity_ <= 0)
            {
                JOptionPane.showMessageDialog(this, "Capacity must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                capacity.requestFocus();
                return false;
            }

            try
            {
                ffspd_ = Double.parseDouble(ffspd.getText().trim());
            }
            catch(NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(this, "Free flow speed must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                ffspd.requestFocus();
                return false;
            }

            if(ffspd_ <= 0)
            {
                JOptionPane.showMessageDialog(this, "Free flow speed must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                ffspd.requestFocus();
                return false;
            }
        
            try
            {
                length_ = Double.parseDouble(length.getText().trim())/5280;
            }
            catch(NumberFormatException ex)
            {
                JOptionPane.showMessageDialog(this, "Length must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                length.requestFocus();
                return false;
            }

            if(length_ <= 0)
            {
                JOptionPane.showMessageDialog(this, "Length must be a positive number", "Error", JOptionPane.ERROR_MESSAGE);
                length.requestFocus();
                return false;
            }
        }
        
        Node source_ = editor.getNode(source_id);
        Node dest_ = editor.getNode(dest_id);
        
        if(source_ == null)
        {
            JOptionPane.showMessageDialog(this, "Node "+source_id+" not found", "Error", JOptionPane.ERROR_MESSAGE);
            source.requestFocus();
            return false;
        }
        
        if(dest_ == null)
        {
            JOptionPane.showMessageDialog(this, "Node "+dest_id+" not found", "Error", JOptionPane.ERROR_MESSAGE);
            dest.requestFocus();
            return false;
        }
        
        int numLanes_ = numLanes.getSelectedIndex()+1;
        
        double jamd = 5280.0/Vehicle.vehicle_length;
        
        Link newLink = null;
        TransitLane lane;
        
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
                lane = new TransitLane(-id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_);
                newLink = new SharedTransitCTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_-1, lane);
                break;
            case SPLIT_TRANSIT_CTM:
                lane = new TransitLane(-id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_);
                newLink = new SplitCTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_-1, lane);
                break;
            case DLR_CTM:
                newLink = new DLRCTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_);
                break;
            case CACC_LTM:
                newLink = new CACCLTMLink(id_, source_, dest_, capacity_, ffspd_, wavespd_, jamd, length_, numLanes_);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Could not find flow model", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
        }
        
        Location[] newCoords = new Location[coords.size()+2];
        newCoords[0] = newLink.getSource();
        newCoords[newCoords.length-1] = newLink.getDest();
        
        for(int i = 0; i < coords.size(); i++)
        {
            newCoords[i+1] = coords.get(i);
        }
        
        newLink.setCoordinates(newCoords);
        
        saveLink(prev, newLink);
        cancel();
        return true;
    }
    
    public void saveLink(Link prev, Link newLink)
    {
        editor.saveLink(prev, newLink);
    }
    
    public void nodeSelected(Node n){}
    public void linkSelected(Link[] l){}
    public void pointSelected(Location loc)
    {
        editor.setMode(Editor.PAN);
        setPoint(loc);
    }
}
