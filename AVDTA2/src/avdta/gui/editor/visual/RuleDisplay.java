/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual;

import avdta.gui.GUI;
import avdta.gui.editor.visual.rules.LinkRule;
import avdta.gui.editor.visual.rules.NodeRule;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Node;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author micha
 */
public class RuleDisplay extends DefaultDisplayManager
{
    private List<NodeRule> nodeRules;
    private List<LinkRule> linkRules;
    
    private Link cachedLink;
    private Node cachedNode;
    private NodeRule cachedNodeRule;
    private LinkRule cachedLinkRule;
    private int cachedNodeT;
    private int cachedLinkT;
    
    private LinkRule defaultLinkRule;
    private NodeRule defaultNodeRule;
    
    public RuleDisplay()
    {
        nodeRules = new ArrayList<NodeRule>();
        linkRules = new ArrayList<LinkRule>();
        
        defaultLinkRule = new LinkRule()
        {
            public String getName()
            {
                return "default";
            }
            
            public int getWidth(Link l, int t)
            {
                return 3;
            }
            
            public Color getColor(Link l, int t)
            {
                return Color.black;
            }
            
            public boolean matches(Link l, int t)
            {
                return true;
            }
        };

        defaultNodeRule = new NodeRule()
        {
            public String getName()
            {
                return "default";
            }
            
            public boolean matches(Node n, int t)
            {
                return true;
            }
            
            public int getRadius(Node n, int t)
            {
                return 0;
            }
            
            public Color getColor(Node n, int t)
            {
                return Color.black;
            }
            
            public Color getBackColor(Node n, int t)
            {
                return Color.yellow;
            }
        };
    }
    

    public List<NodeRule> getNodeRules()
    {
        return nodeRules;
    }
    
    public List<LinkRule> getLinkRules()
    {
        return linkRules;
    }
    
    public boolean hasSpecialDisplay(Link l, int t)
    {
        return findLinkRule(l, t) != defaultLinkRule;
    }
    
    public Color getColor(Link l, int t)
    {
        return findLinkRule(l, t).getColor(l, t);
    }
    
    public int getWidth(Link l, int t)
    {
        return findLinkRule(l, t).getWidth(l, t);
    }
    
    public Color getColor(Node n, int t)
    {
        return findNodeRule(n, t).getColor(n, t);
    }
    
    public Color getBackColor(Node n, int t)
    {
        return findNodeRule(n, t).getBackColor(n, t);
    }
    
    public int getRadius(Node n, int t)
    {
        return findNodeRule(n, t).getRadius(n, t);
    }
    
    protected NodeRule findNodeRule(Node n, int t)
    {
        if(n == cachedNode && t == cachedNodeT)
        {
            return cachedNodeRule;
        }
        
        cachedNode = n;
        cachedNodeT = t;
        
        for(NodeRule rule : nodeRules)
        {
            if(rule.matches(n, t))
            {
                cachedNodeRule = rule;
                return rule;
            }
        }
        
        cachedNodeRule = defaultNodeRule;
        return defaultNodeRule;
    }
    
    protected LinkRule findLinkRule(Link l, int t)
    {
        if(l == cachedLink && t == cachedLinkT)
        {
            return cachedLinkRule;
        }
        
        cachedLink = l;
        cachedLinkT = t;
        
        for(LinkRule rule : linkRules)
        {
            if(rule.matches(l, t))
            {
                cachedLinkRule = rule;
                return rule;
            }
        }

        cachedLinkRule = defaultLinkRule;
        return defaultLinkRule;
    }
    
    public void save(File file) throws IOException
    {
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        
        out.writeObject(new Integer(nodeRules.size()));
        
        for(NodeRule n : nodeRules)
        {
            out.writeObject(n);
        }
        
        out.writeObject(new Integer(linkRules.size()));
        
        for(LinkRule l : linkRules)
        {
            out.writeObject(l);
        }

        out.close();
    }
    
    public void open(File file) throws IOException
    {
        try
        {
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));

            int size = (Integer)in.readObject();
            
            nodeRules.clear();
            
            for(int i = 0; i < size; i++)
            {
                nodeRules.add((NodeRule)in.readObject());
            }
            
            size = (Integer)in.readObject();
            
            linkRules.clear();
            
            for(int i = 0; i < size; i++)
            {
                linkRules.add((LinkRule)in.readObject());
            }
        }
        catch(ClassNotFoundException ex)
        {
            GUI.handleException(ex);
        }
    }
}
