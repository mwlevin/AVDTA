/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.gui.GUI;
import static avdta.gui.GUI.getIcon;
import static avdta.gui.GUI.handleException;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import static avdta.gui.util.GraphicUtils.*;
import avdta.gui.util.ProjectChooser;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import avdta.project.Project;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 *
 * @author ml26893
 */
public class Editor extends JFrame
{
    public static final int PAN = 0;
    public static final int NODE = 1;
    public static final int LINK = 2;
    
    private MapViewer map;
    
    private Map<Integer, Node> nodes;
    private Map<Integer, Link> links;
    
    private Set<Node> selectedNodes;
    private Set<Link> selectedLinks;
    
    
    private JCheckBox linksSelect, nodesSelect, osmSelect;
    
    private Project project;
    
    private JLabel instructions;
    
    
    public Editor()
    {
        this(null);
    }
    public Editor(Project project)
    {
        setTitle(GUI.getTitleName());
        setIconImage(getIcon());
        
        selectedNodes = new HashSet<Node>();
        selectedLinks = new HashSet<Link>();
        
        
        nodes = new HashMap<Integer, Node>();
        links = new HashMap<Integer, Link>();
        
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        int size = (int)Math.min(width, height)-200;

        map = new MapViewer(size, size);
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
        JPanel mapPanel = new JPanel();
        mapPanel.add(map);
        mapPanel.setBorder(BorderFactory.createLineBorder(getForeground()));
        
        constrain(p, mapPanel, 1, 0, 1, 1);
        
        
        linksSelect = new JCheckBox("Links");
        nodesSelect = new JCheckBox("Nodes");
        osmSelect = new JCheckBox("OpenStreetMaps");
        
        linksSelect.setSelected(map.isDisplayLinks());
        nodesSelect.setSelected(map.isDisplayNodes());
        osmSelect.setSelected(map.isDisplayOSM());
        
        linksSelect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                map.setDisplayLinks(linksSelect.isSelected());
            }
        });
        
        nodesSelect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                map.setDisplayNodes(nodesSelect.isSelected());
            }
        });
        
        osmSelect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                map.setDisplayOSM(osmSelect.isSelected());
            }
        });
        
        
        JPanel layers = new JPanel();
        layers.setLayout(new GridBagLayout());
        constrain(layers, nodesSelect, 0, 0, 1, 1);
        constrain(layers, linksSelect, 0, 1, 1, 1);
        constrain(layers, osmSelect, 0, 2, 1, 1);
        
        layers.setBorder(BorderFactory.createTitledBorder("Layers"));
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        constrain(p2, layers, 0, 0, 1, 1);
        
        constrain(p, p2, 0, 0, 1, 2);
        
        instructions = new JLabel(" ");
        constrain(p, instructions, 1, 1, 1, 1, 0, 20, 0, 0);
        
        add(p);
        
        JMenuBar menu = new JMenuBar();
        JMenu me;
        JMenuItem mi;
        //if(project == null)
        {
            

            me = new JMenu("File");
            mi = new JMenuItem("New project");
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    newProject();
                }
            });
            me.add(mi);

            mi = new JMenuItem("Open project");
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    openProject();
                }
            });
            me.add(mi);
            
            mi = new JMenuItem("Save project");
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    saveProject();
                }
            });
            me.add(mi);
            
            mi = new JMenuItem("Close project");
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    closeProject();
                }
            });
            me.add(mi);
            
            me.addSeparator();

            mi = new JMenuItem("Exit");
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            });
            me.add(mi);

            menu.add(me);
            
        }
        
        me = new JMenu("View");
        
        mi = new JMenuItem("Refresh");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                refresh();
            }
        });
        me.add(mi);
        
        me.addSeparator();
        
        mi = new JMenuItem("Zoom in");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                map.zoomIn();
            }
        });
        me.add(mi);
        
        mi = new JMenuItem("Zoom out");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                map.zoomOut();
            }
        });
        me.add(mi);
        
        menu.add(me);
        
        me = new JMenu("Data");
        
        JMenu me2 = new JMenu("Nodes");
        
        mi = new JMenuItem("Find node");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Node node = findNode();
                
                if(node != null)
                {
                    map.center(node);
                }
            }
        });
        me2.add(mi);
        
        me2.addSeparator();
        
        mi = new JMenuItem("Add node");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        me2.add(mi);
        
        mi = new JMenuItem("Edit node");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        me2.add(mi);
        
        mi = new JMenuItem("Remove node");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

            }
        });
        me2.add(mi);
        
        me.add(me2);
        
        me2 = new JMenu("Links");
        
        mi = new JMenuItem("Find link");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                Link link = findLink();
                
                if(link != null)
                {
                    map.center(link);
                }
            }
        });
        me2.add(mi);
        
        me2.addSeparator();
        
        mi = new JMenuItem("Add link");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        me2.add(mi);
        
        mi = new JMenuItem("Edit link");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        me2.add(mi);
        
        mi = new JMenuItem("Remove link");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {

            }
        });
        me2.add(mi);
        
        me.add(me2);
        
        menu.add(me);
        
        menu.add(GUI.createHelpMenu());
        
        this.setJMenuBar(menu);

        
        pack();
        setResizable(false);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                System.exit(0);
            }
        });
        
        setLocationRelativeTo(null);

        setVisible(true);
        
        if(project != null)
        {
            openProject(project);
        }
        
        setMode(PAN);
    }
    
    public void setMode(int mode)
    {
        switch(mode)
        {
            case PAN:
                instructions.setText("Use right mouse button to move; use mouse wheel to zoom");
                break;
            case NODE:
                instructions.setText("Click on a node to select it.");
                break;
            case LINK:
                instructions.setText("Click on a link to select it.");
                break;
        }
    }
    
    public void saveProject()
    {
        try
        {
            if(project != null)
            {
                project.save();
            }
        }
        catch(IOException ex){}
        
    }
    public void newProject()
    {
        JFileChooser chooser = new ProjectChooser(new File(GUI.getDefaultDirectory()));
               
        int returnVal = chooser.showDialog(this, "Select folder");
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            File dir = chooser.getSelectedFile();
            
            String name = JOptionPane.showInputDialog(this, "What do you want to name this project? ", "Project name", 
                    JOptionPane.QUESTION_MESSAGE);
            
            if(name != null)
            {
                try
                {
                    DTAProject project = new DTAProject();
                    project.createProject(name, new File(dir.getCanonicalPath()+"/"+name));
                    openProject(project);
                }
                catch(IOException ex)
                {
                    handleException(ex);
                }
            }
        }
    }

    public void openProject()
    {
        JFileChooser chooser = new ProjectChooser(new File(GUI.getDefaultDirectory()), "DTA");
        
        int returnVal = chooser.showDialog(this, "Open project");
        
        if(returnVal == chooser.APPROVE_OPTION)
        {
            try
            {
                DTAProject project = new DTAProject(chooser.getSelectedFile());
                
                openProject(project);
            }
            catch(IOException ex)
            {
                JOptionPane.showMessageDialog(this, "The selected folder is not a DTA network", "Invalid network", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
    public void openProject(Project project)
    {
        this.project = project;
        
        clearSelected();
        
        
        refresh();
        map.recenter();
    }
    
    public void closeProject()
    {
        openProject(null);
    }
    
    public void refresh()
    {
        if(project != null)
        {
            Simulator sim = project.getSimulator();


            map.setNetwork(sim);

            map.repaint();

            nodes.clear();
            links.clear();

            for(Node n : sim.getNodes())
            {
                nodes.put(n.getId(), n);
            }

            for(Link l : sim.getLinks())
            {
                links.put(l.getId(), l);
            }
        }
        else
        {
            map.setNetwork(null);
            map.repaint();
            
            nodes.clear();
            links.clear();
        }
    }
    
    public Node findNode()
    {
        String input = JOptionPane.showInputDialog(this, "What is the node id?", "Node id", JOptionPane.QUESTION_MESSAGE);
        
        if(input != null)
        {
            try
            {
                int id = Integer.parseInt(input);
                
                return nodes.get(id);
            }
            catch(NumberFormatException ex)
            {
                return null;
            }
        }
        return null;
    }
    
    public Link findLink()
    {
        String input = JOptionPane.showInputDialog(this, "What is the link id?", "Link id", JOptionPane.QUESTION_MESSAGE);
        
        if(input != null)
        {
            try
            {
                int id = Integer.parseInt(input);
                
                return links.get(id);
            }
            catch(NumberFormatException ex)
            {
                return null;
            }
        }
        return null;
    }
    
    public void clearSelectedNodes()
    {
        for(Node n : selectedNodes)
        {
            n.setSelected(false);
        }
        
        selectedNodes.clear();
        
        map.repaint();
    }
    
    public void clearSelectedLinks()
    {
        for(Link l : selectedLinks)
        {
            l.setSelected(false);
        }
        
        selectedLinks.clear();
        
        map.repaint();
    }
    
    public void selectNode(Node n)
    {
        n.setSelected(true);
        selectedNodes.add(n);
        
        map.repaint();
    }
    
    public void selectLink(Link l)
    {
        l.setSelected(true);
        selectedLinks.add(l);
        
        map.repaint();
    }
    
    public void deselectNode(Node n)
    {
        n.setSelected(false);
        selectedNodes.remove(n);
        
        map.repaint();
    }
    
    public void deselectLink(Link l)
    {
        l.setSelected(false);
        selectedLinks.remove(l);
        
        map.repaint();
    }
    
    public void clearSelected()
    {
        clearSelectedNodes();
        clearSelectedLinks();
    }
}
