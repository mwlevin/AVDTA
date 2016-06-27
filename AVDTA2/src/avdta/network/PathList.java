/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.network;

import avdta.network.link.Link;
import avdta.network.node.Node;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class PathList
{
    private Map<Node, Map<Node, Map<Integer, List<Path>>>> paths;
    
    public PathList()
    {
        paths = new HashMap<Node, Map<Node, Map<Integer, List<Path>>>>();
    }
    
    
    // return new path, or old path if new path is a duplicate
    public Path addPath(Path p)
    {
        Map<Node, Map<Integer, List<Path>>> temp;
        
        Node origin = p.getOrigin();
        if(paths.containsKey(origin))
        {
            temp = paths.get(origin);
        }
        else
        {
            paths.put(origin, temp = new HashMap<Node, Map<Integer, List<Path>>>());
        }
        
        Map<Integer, List<Path>> temp2;
        
        Node dest = p.getDest();
        
        if(temp.containsKey(dest))
        {
            temp2 = temp.get(dest);
        }
        else
        {
            temp.put(dest, temp2 = new HashMap<Integer, List<Path>>());
        }
        
        List<Path> temp3;
        
        int hash = p.hashCode();
        
        if(temp2.containsKey(hash))
        {
            temp3 = temp2.get(hash);
            
            for(Path p2 : temp3)
            {
                if(p.equals(p2))
                {
                    return p2;
                }
            }
            
            temp3.add(p);
            p.setId();
            return p;
        }
        else
        {
            temp2.put(hash, temp3 = new ArrayList<Path>());
            temp3.add(p);
            p.setId();
            return p;
        }
    }
    
    public void writeToFile(File file) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(file), true);
        
        for(Node o : paths.keySet())
        {
            Map<Node, Map<Integer, List<Path>>> temp = paths.get(o);
            
            for(Node d : temp.keySet())
            {
                Map<Integer, List<Path>> temp2 = temp.get(d);
                
                for(int h : temp2.keySet())
                {
                    for(Path p : temp2.get(h))
                    {
                        fileout.print(p.getId()+"\t"+p.size());
                        
                        for(Link l : p)
                        {
                            fileout.print("\t"+l.getId());
                        }
                        
                        fileout.println();
                    }
                }
            }
        }
        
        fileout.close();
    }
    
    public void readFromFile(Network network, File file) throws IOException
    {
        paths.clear();
        
        Scanner filein = new Scanner(file);
        
        Map<Integer, Link> links = network.createLinkIdsMap();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            
            Path p = new Path(id);
            int size = filein.nextInt();
            
            for(int i = 0; i < size; i++)
            {
                p.add(links.get(filein.nextInt()));
                
                addUnrestricted(p);
            }
        }
    }
    
    private void addUnrestricted(Path p)
    {
        Map<Node, Map<Integer, List<Path>>> temp;
        
        Node origin = p.getOrigin();
        if(paths.containsKey(origin))
        {
            temp = paths.get(origin);
        }
        else
        {
            paths.put(origin, temp = new HashMap<Node, Map<Integer, List<Path>>>());
        }
        
        Map<Integer, List<Path>> temp2;
        
        Node dest = p.getDest();
        
        if(temp.containsKey(dest))
        {
            temp2 = temp.get(dest);
        }
        else
        {
            temp.put(dest, temp2 = new HashMap<Integer, List<Path>>());
        }
        
        List<Path> temp3;
        
        int hash = p.hashCode();
        
        if(temp2.containsKey(hash))
        {
            temp3 = temp2.get(hash);
            temp3.add(p);
        }
        else
        {
            temp2.put(hash, temp3 = new ArrayList<Path>());
            temp3.add(p);
        }
    }
    
    public Map<Integer, Path> createPathIdsMap()
    {
        Map<Integer, Path> output = new HashMap<Integer, Path>();
        
        for(Node o : paths.keySet())
        {
            Map<Node, Map<Integer, List<Path>>> temp = paths.get(o);
            
            for(Node d : temp.keySet())
            {
                Map<Integer, List<Path>> temp2 = temp.get(d);
                
                for(int h : temp2.keySet())
                {
                    for(Path p : temp2.get(h))
                    {
                        output.put(p.getId(), p);
                    }
                }
            }
        }
        
        return output;
    }
}
