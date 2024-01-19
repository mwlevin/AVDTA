/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.gui.editor.visual.rules.data;

import avdta.network.link.Link;
import avdta.network.type.Type;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This uses a file to provide input data
 * @author Michael
 */
public class LinkFileSource extends LinkDataSource
{
    private String name;
    private Map<Integer, Double> data;
    
    /**
     * Constructs the {@link LinkFileSource} from the specified file.
     * The data format is link id, data.
     * Header data will be ignored.
     * @param file the input file
     * @throws IOException if the file cannot be accessed
     */
    public LinkFileSource(File file) throws IOException
    {
        name = file.getName();
        data = new HashMap<Integer, Double>();
        
        Scanner filein = new Scanner(file);
        
        while(!filein.hasNextInt())
        {
            filein.nextLine();
        }
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            double val = filein.nextDouble();
            
            filein.nextLine();
            
            data.put(id, val);
        }
        filein.close();
    }
    
    /**
     * Returns the data specified by the file.
     * @param l the {@link Link}
     * @param t the time (s)
     * @return the data from the file
     */
    public double getData(Link l, int t)
    {
        if(data.containsKey(l.getId()))
        {
            return data.get(l.getId());
        }
        else
        {
            return 0;
        }
    }
    
    
    /**
     * Returns the name that appears in the user interface.
     * @return the name
     */
    public String getName()
    {
        return name;
    }
    
    /**
     * Returns a description that appears on mouseover in the user interface.
     * @return the description
     */
    public String getDescription()
    {
        return "File data from "+name;
    }
    
    
}
