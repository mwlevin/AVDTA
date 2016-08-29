/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.node.Node;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class NodeFileRule extends NodeRule
{
    private Map<Integer, Tuple> data;
    private String name;

    public NodeFileRule(File file) throws IOException
    {
        name = file.getName();
        data = new HashMap<Integer, Tuple>();
        
        Scanner filein = new Scanner(name);
        if(!filein.hasNextInt())
        {
            filein.nextLine();
        }
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int width = filein.nextInt();
            int r = filein.nextInt();
            int g = filein.nextInt();
            int b = filein.nextInt();
            
            data.put(id, new Tuple(width, new Color(r, g, b)));
        }
        filein.close();
    }
    
    public String getName()
    {
        return name;
    }
    
    public boolean matches(Node n, int t)
    {
        return data.containsKey(n.getId());
    }
    
    public int getRadius(Node n, int t)
    {
        return data.get(n.getId()).width;
    }
    
    public Color getColor(Node n, int t)
    {
        return Color.black;
    }
    
    public Color getBackColor(Node n, int t)
    {
        return data.get(n.getId()).color;
    }

    static class Tuple
    {
        public int width;
        public Color color;
        
        public Tuple(int width, Color color)
        {
            this.width = width;
            this.color = color;
        }
    }
}
