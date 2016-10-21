/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.panels;

import avdta.gui.panels.DTAPane;
import avdta.dta.Assignment;
import avdta.dta.DTASimulator;
import avdta.dta.MSAAssignment;
import avdta.gui.GUI;
import avdta.project.DTAProject;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import static avdta.gui.util.GraphicUtils.*;
import avdta.network.PathList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
/**
 *
 * @author micha
 */
public class AssignmentPane extends GUIPanel
{
    private DTAProject project;
    private DTAPane parent;
    
    private JList list;
    private JButton loadAssignment;
    private JButton clearAssignments;
    
    private ArrayList<Assignment> assignments;
    
    private Assignment mostRecent;
    
    public AssignmentPane(DTAPane parent_)
    {
        super(parent_);
        this.parent = parent_;
        
        list = new JList();
        list.setListData(new String[]{});
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellWidth(300);
        list.setFixedCellHeight(15);
        list.setVisibleRowCount(6);
        
        list.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                if(list.getSelectedIndex() >= 0)
                {
                    parent.showAssignment(assignments.get(list.getSelectedIndex()));
                    loadAssignment.setEnabled(true);
                }
                else
                {
                    parent.showAssignment(null);
                    loadAssignment.setEnabled(false);
                }
            }
        });
        
        clearAssignments = new JButton("Clear all");
        clearAssignments.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                project.deleteAssignments();
                try
                {
                    listAssignments();
                }
                catch(IOException ex)
                {
                    GUI.handleException(ex);
                }
            }
        });
        
        loadAssignment = new JButton("Load assignment");
        
        loadAssignment.addActionListener(new ActionListener()
        {
           public void actionPerformed(ActionEvent e)
           {
               String name = list.getSelectedValue().toString();
               
               try
               {
                    loadAssignment(name);
               }
               catch(IOException ex)
               {
                   GUI.handleException(ex);
               }
           }
        });
        
        setLayout(new GridBagLayout());
        JScrollPane scroll = new JScrollPane(list);
        
        constrain(this, new JScrollPane(list), 0, 0, 1, 1);
        constrain(this, loadAssignment, 0, 1, 1, 1);
        constrain(this, clearAssignments, 0, 2, 1, 1);
        
        assignments = new ArrayList<Assignment>();
        
        reset();
        setEnabled(false);
        
    }
    
    public Assignment getMostRecentAssignment()
    {
        return mostRecent;
    }
    
    public void loadAssignment(String name) throws IOException
    {
        parentSetEnabled(false);
        
        Assignment assign = readAssignment(name);
        
        DTASimulator sim = project.getSimulator();
        
        PathList paths = new PathList(sim, assign.getPathsFile());
        assign.readFromFile(project, sim.getVehicles(), paths);
        parent.loadAssignment(assign);
        
        JOptionPane.showMessageDialog(this, "Loaded assignment "+name, "Complete", JOptionPane.INFORMATION_MESSAGE);
        list.setSelectedIndex(-1);
        
        parentSetEnabled(true);
    }
    
    public Assignment readAssignment(String name) throws IOException
    {
        String folder = project.getAssignmentsFolder()+"/"+name;
        
        File indicator = new File(folder+"/msa.dat");
        Assignment assign;
        
        File file = new File(folder); 
        if(indicator.exists())
        {
            assign = new MSAAssignment(file);
        }
        else
        {
            assign = new Assignment(file);
        }
        
        return assign;
    }
    
    
    public void listAssignments() throws IOException
    {
        File dir = new File(project.getAssignmentsFolder());
        
        assignments.clear();
        mostRecent = null;
        
        long lastModified = Long.MAX_VALUE;
        mostRecent = null;
        
        if(dir.exists())
        {
            for(File f : dir.listFiles())
            {
                
                try
                {
                    Assignment assign = readAssignment(f.getName());
                    assignments.add(assign);

                    if(assign.getTime() < lastModified)
                    {
                        lastModified = assign.getTime();
                        mostRecent = assign;
                    }
                }
                catch(IOException ex)
                {
                    delete(f);
                }
            }
        }
        
        
        Collections.sort(assignments);
        
        String[] names = new String[assignments.size()];
        
        for(int i = 0; i < assignments.size(); i++)
        {
            names[i] = assignments.get(i).getName();
        }
        
        list.setListData(names);
    }
    
    public void delete(File folder) throws IOException
    {
        for(File f : folder.listFiles())
        {
            f.delete();
        }
        folder.delete();
    }
    
    public void setProject(DTAProject project)
    {
        this.project = project;
        
        reset();
        
        if(project != null)
        {
            setEnabled(true);
        }
        else
        {
            setEnabled(false);
        }
    }
    
    public void reset()
    {
        if(project != null)
        {
            try
            {
                listAssignments();
            }
            catch(IOException ex)
            {
                GUI.handleException(ex);
            }
            
        }
        else
        {
            list.setListData(new String[]{});
            list.setSelectedIndex(-1);
            
        }
    }
    
    
    public void setEnabled(boolean e)
    {
        list.setEnabled(e);
        loadAssignment.setEnabled(e && list.getSelectedIndex() > 0);
        clearAssignments.setEnabled(e);
        super.setEnabled(e);
    }
}
