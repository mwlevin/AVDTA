/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor;

import avdta.gui.editor.visual.DefaultDisplayManager;
import avdta.gui.editor.visual.DisplayManager;
import avdta.gui.GUI;
import static avdta.gui.GUI.getIcon;
import static avdta.gui.GUI.handleException;
import avdta.gui.editor.visual.RuleDisplay;
import avdta.gui.editor.visual.rules.LinkRule;
import avdta.gui.editor.visual.rules.NodeRule;
import avdta.gui.editor.visual.rules.editor.LinkRulePanel;
import avdta.gui.editor.visual.rules.editor.NodeRulePanel;
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
import avdta.network.link.SharedTransitCTMLink;
import avdta.network.link.TransitLane;
import avdta.network.node.Intersection;
import avdta.network.node.Location;
import avdta.network.node.Node;
import avdta.network.node.Phase;
import avdta.network.node.PhaseRecord;
import avdta.network.node.SignalRecord;
import avdta.network.node.Signalized;
import avdta.network.node.Turn;
import avdta.network.node.TurnRecord;
import avdta.project.DTAProject;
import avdta.project.Project;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

/**
 *
 * @author ml26893
 */
public class Editor extends JFrame implements MouseListener
{
    public static final int PAN = 0;
    public static final int NODE = 1;
    public static final int LINK = 2;
    public static final int POINT = 3;
    
    private int mode;
    
    private MapViewer map;
    
    private Map<Integer, Node> nodes;
    private Map<Integer, Link> links;
    
    private Set<Node> selectedNodes;
    private Set<Link> selectedLinks;
    
    
    private JCheckBox linksSelect, nodesSelect, osmSelect, centroidSelect;
    private JMenuItem save, close;
    
    private Project project;
    
    private JLabel instructions;
    
    private RuleDisplay display;
    
    private Set<SelectListener> listeners;
    
    
    private NodeRulePanel nodePanel;
    private LinkRulePanel linkPanel;
    
    

    
    
    
    private JPanel thisPanel;
   
    
    public static String getTitleName()
    {
        return GUI.getTitleName() + " Editor";
    }
    
    
    
    
    private JSlider timeSlider;
    
    public Editor()
    {
        this(null, true);
    }
    public Editor(Project project)
    {
        this(project, true);
    }
    public Editor(Project project, final boolean exitOnClose)
    {
        final Editor thisEditor = this;

        setTitle(getTitleName());
        setIconImage(getIcon());

        
        selectedNodes = new HashSet<Node>();
        selectedLinks = new HashSet<Link>();
        
        listeners = new HashSet<SelectListener>();
        
        
        nodes = new HashMap<Integer, Node>();
        links = new HashMap<Integer, Link>();
        
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        int size = (int)Math.min(width-400, height-200);

        display = new RuleDisplay();
        
        map = new MapViewer(display, size, size);
        
        map.addMouseListener(this);
        
        timeSlider = new JSlider(0, 3600, 0);
        timeSlider.setEnabled(false);
        
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        
         thisPanel = p;
        
        JPanel mapPanel = new JPanel();
        mapPanel.add(map);
        mapPanel.setBorder(BorderFactory.createLineBorder(getForeground()));
        
        constrain(p, mapPanel, 1, 0, 1, 1);
        
        
        linksSelect = new JCheckBox("Links");
        nodesSelect = new JCheckBox("Nodes");
        osmSelect = new JCheckBox("OpenStreetMaps");
        centroidSelect = new JCheckBox("Centroids");
        
        linksSelect.setSelected(display.isDisplayLinks());
        nodesSelect.setSelected(display.isDisplayNodes());
        osmSelect.setSelected(map.isDisplayOSM());
        centroidSelect.setSelected(display.isDisplayCentroids());
        
        linksSelect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                display.setDisplayLinks(linksSelect.isSelected());
                map.repaint();
            }
        });
        
        nodesSelect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                display.setDisplayNodes(nodesSelect.isSelected());
                map.repaint();
            }
        });
        
        centroidSelect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                display.setDisplayCentroids(centroidSelect.isSelected());
                map.repaint();
            }
        });
        
        osmSelect.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                map.setDisplayOSM(osmSelect.isSelected());
            }
        });
        
        timeSlider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent e)
            {
                map.setTime(timeSlider.getValue());
                map.repaint();
            }
        });
        
        JPanel layers = new JPanel();
        layers.setLayout(new GridBagLayout());
        constrain(layers, nodesSelect, 0, 0, 1, 1);
        constrain(layers, linksSelect, 0, 1, 1, 1);
        constrain(layers, osmSelect, 0, 3, 1, 1);
        constrain(layers, centroidSelect, 0, 2, 1, 1);
        
        layers.setBorder(BorderFactory.createTitledBorder("Layers"));
        
        JPanel p2 = new JPanel();
        p2.setLayout(new GridBagLayout());
        constrain(p2, layers, 0, 0, 1, 1);
        
        nodePanel = new NodeRulePanel(this, display.getNodeRules());
        linkPanel = new LinkRulePanel(this, display.getLinkRules());
        
        JPanel p3 = new JPanel();
        p3.setLayout(new GridBagLayout());
        p3.setBorder(BorderFactory.createTitledBorder("Visualization"));
        constrain(p3, nodePanel, 0, 0, 2, 1);
        constrain(p3, linkPanel, 0, 1, 2, 1);
        constrain(p3, new JLabel("Time: "), 0, 2, 1, 1);
        constrain(p3, timeSlider, 1, 2, 1, 1);
        constrain(p2, p3, 0, 1, 1, 1);
        
        constrain(p, p2, 0, 0, 1, 2);
        
        instructions = new JLabel(" ");
        constrain(p, instructions, 1, 1, 1, 1, 0, 20, 0, 0);
        
        add(p);
        
        JMenuBar menu = new JMenuBar();
        JMenu me;
        JMenuItem mi;
        
        me = new JMenu("File");
        
        if(project == null)
        {
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
        }
            
        mi = new JMenuItem("Save project");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                saveProject();
            }
        });
        me.add(mi);
        save = mi;
        save.setEnabled(false);
        
        if(project == null)
        {
            mi = new JMenuItem("Close project");
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    closeProject();
                }
            });
            me.add(mi);
            close = mi;
            close.setEnabled(false);
        }
        me.addSeparator();
        
        mi = new JMenuItem("Exit");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if(exitOnClose)
                {
                    exit();
                }
                else
                {
                    setVisible(false);
                }
            }
        });
        me.add(mi);
        
        

        menu.add(me);
        
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
        
        me.addSeparator();
        
        mi = new JMenuItem("Take screenshot");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                saveScreenshot();
            }
        });
        me.add(mi);
        
        me.addSeparator();
        
        mi = new JMenuItem("Open visualization");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                openVisualization();
            }
        });
        me.add(mi);
        
        mi = new JMenuItem("Save visualization");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                saveVisualization();
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
                addSelectListener(new SelectAdapter()
                {
                    public void pointSelected(Location loc)
                    {
                        removeSelectListener(this);
                        setMode(PAN);
                        
                        if(links == null)
                        {
                            return;
                        }
                        
                        final JInternalFrame frame = new JInternalFrame("Add node");
                        
                        EditNode edit = new EditNode(thisEditor)
                        {
                            public void cancel()
                            {
                                super.cancel();
                                frame.setVisible(false);
                                map.repaint();
                            }
                        };
                        edit.setLocation(loc);
                        
                        frame.add(edit);

                        frame.pack();
                        frame.setResizable(false);
                        frame.setClosable(true);
                        frame.setLocation(thisEditor.getWidth()/2 - frame.getWidth()/2, thisEditor.getHeight()/2 - frame.getHeight()/2);
                        frame.setVisible(true);
                        
                        JLayeredPane toUse = JLayeredPane.getLayeredPaneAbove(thisPanel);
                        toUse.add(frame);
                        try
                        {
                            frame.setSelected(true);
                        }
                        catch(Exception ex){}
                        
                        
                    }
                });
                setMode(POINT);
                setInstructions("Choose the location of the node.");
            }
        });
        me2.add(mi);
        
        mi = new JMenuItem("Edit node");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                
                addSelectListener(new SelectAdapter()
                {
                    public void nodeSelected(Node node)
                    {
                        removeSelectListener(this);
                        setMode(PAN);
                        
                        if(nodes == null)
                        {
                            return;
                        }
                        
                        final JInternalFrame frame = new JInternalFrame("Edit node");
                        
                        frame.add(new EditNode(thisEditor, node)
                        {
                            public void cancel()
                            {
                                super.cancel();
                                frame.setVisible(false);
                                map.repaint();
                            }
                        });
                        

                        frame.pack();
                        frame.setResizable(false);
                        frame.setClosable(true);
                        frame.setLocation(thisEditor.getWidth()/2 - frame.getWidth()/2, thisEditor.getHeight()/2 - frame.getHeight()/2);
                        frame.setVisible(true);
                        
                        JLayeredPane toUse = JLayeredPane.getLayeredPaneAbove(thisPanel);
                        toUse.add(frame);
                        try
                        {
                            frame.setSelected(true);
                        }
                        catch(Exception ex){}
                        
                        
                    }
                });
                setMode(NODE);
            }
        });
        me2.add(mi);
        
        mi = new JMenuItem("Remove node");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addSelectListener(new SelectAdapter()
                {
                    public void nodeSelected(Node n)
                    {
                        removeSelectListener(this);
                        setMode(PAN);
                        removeNode(n);
                        map.repaint();
                    }
                });
                setMode(NODE);
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
                addSelectListener(new TwoNodeSelectAdapter(thisEditor)
                {
                    public void firstNodeSelected(Node n)
                    {
                        setInstructions("Select the destination node.");
                    }
                    
                    public void nodesSelected(Node n1, Node n2)
                    {
                        removeSelectListener(this);
                        setMode(PAN);
                        
                        if(links == null)
                        {
                            return;
                        }
                        
                        final JInternalFrame frame = new JInternalFrame("Add link");
                        
                        
                        frame.add(new EditLink(thisEditor, n1, n2)
                        {
                            public void cancel()
                            {
                                super.cancel();
                                frame.setVisible(false);
                                map.repaint();
                            }
                        });

                        
                        frame.pack();
                        frame.setResizable(false);
                        frame.setClosable(true);
                        frame.setLocation(thisEditor.getWidth()/2 - frame.getWidth()/2, thisEditor.getHeight()/2 - frame.getHeight()/2);
                        frame.setVisible(true);
                        
                        JLayeredPane toUse = JLayeredPane.getLayeredPaneAbove(thisPanel);
                        toUse.add(frame);
                        try
                        {
                            frame.setSelected(true);
                        }
                        catch(Exception ex){}
                        
                        
                    }
                });
                setMode(NODE);
                setInstructions("Select the source node.");
            }
        });
        me2.add(mi);
        
        mi = new JMenuItem("Edit link");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addSelectListener(new SelectAdapter()
                {
                    public void linkSelected(Link[] links)
                    {
                        removeSelectListener(this);
                        setMode(PAN);
                        
                        if(links == null)
                        {
                            return;
                        }
                        
                        final JInternalFrame frame = new JInternalFrame("Edit link");
                        
                        if(links.length == 1)
                        {
                            frame.add(new EditLink(thisEditor, links[0])
                            {
                                public void cancel()
                                {
                                    super.cancel();
                                    frame.setVisible(false);
                                    map.repaint();
                                }
                            });
                        }
                        else
                        {
                            JTabbedPane tabs = new JTabbedPane();

                            for(Link link : links)
                            {
                                tabs.add(""+link.getId(), new EditLink(thisEditor, link)
                                {
                                    public boolean save()
                                    {
                                        super.save();
                                        return false;
                                    }
                                    public void cancel()
                                    {
                                        super.cancel();
                                        frame.setVisible(false);
                                    }
                                });
                            }

                            frame.add(tabs);
                        }
                        frame.pack();
                        frame.setResizable(false);
                        frame.setClosable(true);
                        frame.setLocation(thisEditor.getWidth()/2 - frame.getWidth()/2, thisEditor.getHeight()/2 - frame.getHeight()/2);
                        frame.setVisible(true);
                        
                        JLayeredPane toUse = JLayeredPane.getLayeredPaneAbove(thisPanel);
                        toUse.add(frame);
                        try
                        {
                            frame.setSelected(true);
                        }
                        catch(Exception ex){}
                        
                        
                    }
                });
                setMode(LINK);
            }
        });
        me2.add(mi);
        
        mi = new JMenuItem("Remove link");
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addSelectListener(new SelectAdapter()
                {
                    public void linkSelected(Link[] links)
                    {
                        removeSelectListener(this);
                        setMode(PAN);
                        
                        if(links == null)
                        {
                            return;
                        }
                        
                        if(links.length == 1)
                        {
                            removeLink(links[0]);
                        }
                        else
                        {
                            Link input = (Link)JOptionPane.showInputDialog(thisEditor, "Choose link to remove.", "Multiple links", JOptionPane.QUESTION_MESSAGE, null, links, links[0]);
                            
                            if(input != null)
                            {
                                removeLink(input);
                            }
                        }
                        map.repaint();
                    }
                });
                setMode(LINK);
            }
        });
        me2.add(mi);
        
        me.add(me2);
        
        menu.add(me);
        
        menu.add(GUI.createHelpMenu());
        
        this.setJMenuBar(menu);

        thisPanel.setMinimumSize(getPreferredSize());
        
        pack();
        setResizable(false);
        
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                if(exitOnClose)
                {
                    exit();
                }
                else
                {
                    setVisible(false);
                }
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
    
    public void exit()
    {
        setVisible(false);

        try
        {
            Thread.sleep(1*1000);
        }
        catch(InterruptedException ex)
        {

        }

        System.exit(0);
    }
    public void setMode(int mode)
    {
        this.mode = mode;
        switch(mode)
        {
            case PAN:
                setInstructions("Use right mouse button to move; use mouse wheel to zoom");
                map.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                break;
            case NODE:
                setInstructions("Click on a node to select it.");
                map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case LINK:
                setInstructions("Click on a link to select it.");
                map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            case POINT:
                setInstructions("Click on the map to select a point.");
                map.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
        }
    }
    
    public void setInstructions(String i)
    {
        instructions.setText(i);
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
    
    public void openVisualization()
    {
        JFileChooser chooser = new JFileChooser(GUI.getDefaultDirectory());
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Vizualization files", "viz"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        
        int returnVal = chooser.showOpenDialog(this);
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File file = chooser.getSelectedFile();
                
                display.open(chooser.getSelectedFile());
            }
            catch(IOException ex)
            {
                GUI.handleException(ex);
            }
        }
    }
    
    public void saveVisualization()
    {
        JFileChooser chooser = new JFileChooser(GUI.getDefaultDirectory());
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("Vizualization files", "viz"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        
        int returnVal = chooser.showSaveDialog(this);
        
        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File file = chooser.getSelectedFile();
            
                if(file.getName().indexOf('.') < 0)
                {
                    file = new File(file.getCanonicalPath()+".viz");
                }
                
                display.save(chooser.getSelectedFile());
            }
            catch(IOException ex)
            {
                GUI.handleException(ex);
            }
        }
    }
    
    private static final int screenshot_size = 1600;
    
    public void saveScreenshot()
    {

        
        JFileChooser chooser = new JFileChooser(GUI.getDefaultDirectory());
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG images", "png"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);

        int returnVal = chooser.showSaveDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION)
        {
            try
            {
                File file = chooser.getSelectedFile();

                if(file.getName().indexOf('.') < 0)
                {
                    file = new File(file.getCanonicalPath()+".png");
                }


                BufferedImage image = new BufferedImage(map.getWidth(), map.getHeight(), BufferedImage.TYPE_INT_ARGB);
                map.setZoomControlsVisible(false);
                Graphics g = image.getGraphics();
                map.paint(g);
                g.setColor(Color.black);
                g.drawRect(0, 0, image.getWidth(), image.getHeight());
                map.setZoomControlsVisible(true);
                
                ImageIO.write(image, "png", file);

            }
            catch(Exception ex)
            {
                GUI.handleException(ex);
            }
        }

        
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

        
        boolean enable = project != null;
        
        if(close != null)
        {
            close.setEnabled(enable);
        }
        
        save.setEnabled(enable);
        nodesSelect.setEnabled(enable);
        linksSelect.setEnabled(enable);
        centroidSelect.setEnabled(enable);
        nodePanel.setEnabled(enable);
        linkPanel.setEnabled(enable);
        timeSlider.setEnabled(enable);
        
        
        if(project != null)
        {
            setTitle(project.getName()+" - "+getTitleName());
            timeSlider.setValue(0);
            timeSlider.setMaximum(Simulator.duration);
            
        }
        else
        {
            setTitle(getTitleName());
        }
        
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
    
    public MapViewer getMap()
    {
        return map;
    }
    
    public JPanel getPanel()
    {
        return thisPanel;
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
    
    public void addSelectListener(SelectListener s)
    {
        listeners.add(s);
    }
    
    public void removeSelectListener(SelectListener s)
    {
        listeners.remove(s);
    }
    
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseClicked(MouseEvent e)
    {
        if(e.getButton() != MouseEvent.BUTTON1)
        {
            return;
        }
        switch(mode)
        {
            case PAN: 
                break;
            case NODE:
                Node n = findClosestNode(e.getX(), e.getY());
                
                if(n != null)
                {
                    for(SelectListener l : listeners)
                    {
                        l.nodeSelected(n);
                    }
                }
                break;
            case LINK:
                Link[] links = findClosestLink(e.getX(), e.getY());

                if(links != null)
                {
                    for(SelectListener listener : listeners)
                    {
                        listener.linkSelected(links);
                    }
                } 
                break;
            case POINT:
                ICoordinate coord = map.getPosition(e.getPoint());
                Location loc = new Location(coord);
                for(SelectListener liste :listeners)
                {
                    liste.pointSelected(loc);
                }
                break;
        }
        
    }
    
    private static final double epsilon = 0.5;
    
    protected Node findClosestNode(int x, int y)
    {
        Location loc = new Location(map.getPosition(new Point(x, y)));
        
        double min = Integer.MAX_VALUE;
        Node closest = null;
        
        TreeSet<Node> possible = new TreeSet<Node>();
        
        
        
        for(int id : nodes.keySet())
        {
            Node n = nodes.get(id);
            double dist = loc.distanceTo(n);
            
            if(dist < min)
            {
                min = dist;
                closest = n;
            }
        }
        
        for(int id : nodes.keySet())
        {
            Node n = nodes.get(id);
            double dist = loc.distanceTo(n);
            
            if((dist-min)/min < epsilon)
            {
                possible.add(n);
            }
        }

        

        
        if(possible.size() > 1)
        {
            Node output = (Node)JOptionPane.showInputDialog(this, "Choose node.", "Multiple nodes", JOptionPane.QUESTION_MESSAGE, null, possible.toArray(), possible.first());
            
            return output;
        }
        
        
        return closest;
    }
    
    protected Link[] findClosestLink(int x, int y)
    {
        Location loc = new Location(map.getPosition(new Point(x, y)));
        
        double min = Integer.MAX_VALUE;
        Link closest = null;
        
        for(int id : links.keySet())
        {
            Link l = links.get(id);
            
            if(l instanceof TransitLane)
            {
                continue;
            }
            
            double dist = l.distanceTo(loc);
            
            if(dist < min)
            {
                min = dist;
                closest = l;
            }
            
        }
        
        if(closest == null)
        {
            return null;
        }

        
        Set<Link> opposite_candidates = closest.getSource().getIncoming();
        
        for(Link l : opposite_candidates)
        {
            if(l.getSource() == closest.getDest() && !(l instanceof TransitLane))
            {
                return new Link[]{closest, l};
            }
        }
        
        return new Link[]{closest};
    }
    
    public void replaceNode(Node prev, Node newNode)
    {
        Set<Node> netNodes = project.getSimulator().getNodes();
        
        if(prev != null)
        {
           netNodes.remove(prev);
           nodes.remove(prev.getId());
        }
        
        netNodes.add(newNode);
        nodes.put(newNode.getId(), newNode);
    }
    
    public void saveLink(Link prev, Link newLink)
    {
        Set<Link> netLinks = project.getSimulator().getLinks();
        
        if(prev != null)
        {
           netLinks.remove(prev);
           links.remove(prev.getId());
        }
        
        netLinks.add(newLink);
        links.put(newLink.getId(), newLink);
        
    }
    
    public Node getNode(int id)
    {
        return nodes.get(id);
    }
    
    public Link getLink(int id)
    {
        return links.get(id);
    }
    
    public void removeNode(Node n)
    {
        for(Link l : n.getOutgoing())
        {
            removeLink(l);
        }
        
        for(Link l : n.getIncoming())
        {
            removeLink(l);
        }
        
        nodes.remove(n.getId());
        project.getSimulator().getNodes().remove(n);
    }
    
    public void removeLink(Link l)
    {
        links.remove(l.getId());
        project.getSimulator().getLinks().remove(l);
        
        if(l instanceof SharedTransitCTMLink)
        {
            Link temp = links.get(-l.getId());
            links.remove(-l.getId());
            project.getSimulator().getLinks().remove(temp);
        }
    }
    
    public void addVisualization(LinkRule rule)
    {
        display.getLinkRules().add(rule);
        map.repaint();
    }
    
    public void addVisualization(NodeRule rule)
    {
        display.getNodeRules().add(rule);
        map.repaint();
    }
}

