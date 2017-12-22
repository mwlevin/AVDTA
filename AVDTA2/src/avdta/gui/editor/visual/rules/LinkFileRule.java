/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules;

import avdta.network.link.Link;
import avdta.network.type.Type;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This displays {@link Link}s based on information in a file.
 * This class can be used to create an external visualization source.
 * @author Michael
 */
public class LinkFileRule extends LinkRule
{
    private Map<Integer, Tuple> data;
    private String name;

    /**
     * Reads in data for the {@link LinkFileRule}.
     * The file format is node id, width, red, green, blue.
     * Header data may be included and will be ignored.
     * @param file the file
     * @throws IOException if the file cannot be accessed
     */
    public LinkFileRule(File file) throws IOException
    {
        name = file.getName();
        data = new HashMap<Integer, Tuple>();
        
        Scanner filein = new Scanner(file);
        while(!filein.hasNextInt())
        {
            filein.nextLine();
        }
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int width = (int)Math.max(0, Math.round(filein.nextDouble()));
            int r = (int)Math.min(255, Math.max(0, Math.round(filein.nextDouble())));
            int g = (int)Math.min(255, Math.max(0, Math.round(filein.nextDouble())));
            int b = (int)Math.min(255, Math.max(0, Math.round(filein.nextDouble())));
            
            data.put(id, new Tuple(width, new Color(r, g, b)));
            
            filein.nextLine();
        }
        filein.close();
    }
    
    /**
     * Returns the input file name.
     * @return the input file name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns whether this {@link LinkRule} applies to the given {@link Link} at the specified time
     * @param l the {@link Link}
     * @param t the time (s)
     * @return if the {@link Link} id was found in the file.
     */
    public boolean matches(Link l, int t)
    {
        return data.containsKey(l.getId());
    }
    
    /**
     * Returns the width for the given {@link Link} at the given time specified by the file.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return the width (px)
     */
    public int getWidth(Link l, int t)
    {
        return data.get(l.getId()).width;
    }
    
    /**
     * Returns the color for the given {@link Link} at the given time specified by the file.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return the border color
     */
    public Color getColor(Link l, int t)
    {
        return data.get(l.getId()).color;
    }

    private static class Tuple
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
