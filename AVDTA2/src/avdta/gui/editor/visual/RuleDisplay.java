/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual;

import avdta.gui.GUI;
import avdta.gui.editor.visual.rules.LinkRule;
import avdta.gui.editor.visual.rules.NodeRule;
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
    
    public RuleDisplay()
    {
        nodeRules = new ArrayList<NodeRule>();
        linkRules = new ArrayList<LinkRule>();

    }
    
    public List<NodeRule> getNodeRules()
    {
        return nodeRules;
    }
    
    public List<LinkRule> getLinkRules()
    {
        return linkRules;
    }
    
    
    public Color getColor(Link l)
    {
        if(l == cachedLink)
        {
            return cachedLinkRule.getColor(l);
        }
        else
        {
            cachedLinkRule = findLinkRule(l);
            
            if(cachedLink != null)
            {
                cachedLink = l;
                return cachedLinkRule.getColor(l);
            }
            else
            {
                cachedLink = null;
                return super.getColor(l);
            }  
        }
        
    }
    
    public int getWidth(Link l)
    {
        if(l == cachedLink)
        {
            return cachedLinkRule.getWidth(l);
        }
        else
        {
            cachedLinkRule = findLinkRule(l);
            
            if(cachedLink != null)
            {
                cachedLink = l;
                return cachedLinkRule.getWidth(l);
            }
            else
            {
                cachedLink = null;
                return super.getWidth(l);
            }  
        }
    }
    
    public Color getColor(Node n)
    {
        if(n == cachedNode)
        {
            return cachedNodeRule.getColor(n);
        }
        else
        {
            cachedNodeRule = findNodeRule(n);
            
            if(cachedNode != null)
            {
                cachedNode = n;
                return cachedNodeRule.getColor(n);
            }
            else
            {
                cachedNode = null;
                return super.getColor(n);
            }  
        }
    }
    
    public Color getBackColor(Node n)
    {
        if(n == cachedNode)
        {
            return cachedNodeRule.getBackColor(n);
        }
        else
        {
            cachedNodeRule = findNodeRule(n);
            
            if(cachedNode != null)
            {
                cachedNode = n;
                return cachedNodeRule.getBackColor(n);
            }
            else
            {
                cachedNode = null;
                return super.getBackColor(n);
            }  
        }
    }
    
    public int getRadius(Node n)
    {
        if(n == cachedNode)
        {
            return cachedNodeRule.getRadius(n);
        }
        else
        {
            cachedNodeRule = findNodeRule(n);
            
            if(cachedNode != null)
            {
                cachedNode = n;
                return cachedNodeRule.getRadius(n);
            }
            else
            {
                cachedNode = null;
                return super.getRadius(n);
            }  
        }
    }
    
    protected NodeRule findNodeRule(Node n)
    {
        for(NodeRule rule : nodeRules)
        {
            if(rule.matches(n))
            {
                return rule;
            }
        }
        
        return null;
    }
    
    protected LinkRule findLinkRule(Link l)
    {
        for(LinkRule rule : linkRules)
        {
            if(rule.matches(l))
            {
                return rule;
            }
        }
        
        return null;
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
