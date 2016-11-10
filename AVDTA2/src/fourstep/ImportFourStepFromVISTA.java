/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fourstep;

import java.io.File;
import java.io.IOException;
import avdta.project.FourStepProject;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 *
 * @author micha
 */
public class ImportFourStepFromVISTA 
{
    public ImportFourStepFromVISTA(FourStepProject project, File dynamic_od) throws IOException
    {
        convertDynamicOD(project, dynamic_od);
    }
    
    public void convertDynamicOD(FourStepProject project, File dynamic_od) throws IOException
    {
        Map<Integer, Double> productions = new HashMap<Integer, Double>();
        Map<Integer, Double> attractions = new HashMap<Integer, Double>();
        
        Scanner filein = new Scanner(dynamic_od);
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int o = filein.nextInt();
            int d = filein.nextInt();
            double dem = filein.nextDouble();
            int ast = filein.nextInt();
            
            if(productions.containsKey(o))
            {
                productions.put(o, productions.get(o)+dem);
            }
            else
            {
                productions.put(o, dem);
            }
            
            if(attractions.containsKey(d))
            {
                attractions.put(d, attractions.get(d)+dem);
            }
            else
            {
                attractions.put(d, dem);
            }
        }
        filein.close();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getProductionsFile()), true);
        fileout.println(ReadFourStepNetwork.getProductionsFileHeader());
        
        for(int o : productions.keySet())
        {
            fileout.println(o+"\t"+productions.get(o));
        }
        fileout.close();
        productions = null;
        
        fileout = new PrintStream(new FileOutputStream(project.getAttractionsFile()), true);
        fileout.println(ReadFourStepNetwork.getAttractionsFileHeader());
        
        for(int d : attractions.keySet())
        {
            fileout.println(d+"\t"+attractions.get(d));
        }
        fileout.close();
        
    }
}
