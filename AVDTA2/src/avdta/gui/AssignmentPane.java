/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui;

import avdta.dta.Assignment;
import avdta.dta.DTASimulator;
import avdta.dta.MSAAssignment;
import avdta.project.DTAProject;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import static avdta.gui.GraphicUtils.*;
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
public class AssignmentPane extends JPanel
{
    private DTAPane parent;
    
    private DTAProject project;
    
    private JList list;
    private JButton loadAssignment;
    private JButton clearAssignments;
    
    private ArrayList<Assignment> assignments;
    
    public AssignmentPane(DTAPane parent_)
    {
        this.parent = parent_;
        
        list = new JList();
        list.setListData(new String[]{});
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
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
        disable();
        
    }
    
    public void loadAssignment(String name) throws IOException
    {
        parent.disable();
        
        Assignment assign = readAssignment(name);
        
        DTASimulator sim = project.getSimulator();
        
        PathList paths = new PathList(sim, project.getPathsFile());
        assign.readFromFile(sim.getVehicles(), paths, 
                new File(project.getAssignmentsFolder()+"/"+name+"/vehicles.dat"));
        parent.loadAssignment(assign);
        
        JOptionPane.showMessageDialog(this, "Loaded assignment "+name, "Complete", JOptionPane.INFORMATION_MESSAGE);
        list.setSelectedIndex(-1);
        
        parent.enable();
    }
    
    public Assignment readAssignment(String name) throws IOException
    {
        String folder = project.getAssignmentsFolder()+"/"+name;
        
        File indicator = new File(folder+"/msa.dat");
        Assignment assign;
        
        File file = new File(folder+"/vehicles.dat"); 
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
        
        for(File f : dir.listFiles())
        {
            assignments.add(readAssignment(f.getName()));
        }
        
        Collections.sort(assignments);
        
        String[] names = new String[assignments.size()];
        
        for(int i = 0; i < assignments.size(); i++)
        {
            names[i] = assignments.get(i).getName();
        }
        
        list.setListData(names);
    }
    
    public void setProject(DTAProject project)
    {
        this.project = project;
        
        reset();
        
        if(project != null)
        {
            enable();
        }
        else
        {
            disable();
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
    
    public void disable()
    {
        list.setEnabled(false);
        loadAssignment.setEnabled(false);
        clearAssignments.setEnabled(false);
    }
    
    public void enable()
    {
        list.setEnabled(true);
        loadAssignment.setEnabled(list.getSelectedIndex() > 0);
        clearAssignments.setEnabled(true);
    }
}
