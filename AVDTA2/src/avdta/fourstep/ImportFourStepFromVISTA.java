/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.fourstep;

import avdta.demand.DemandProfile;
import avdta.demand.ReadDemandNetwork;
import java.io.File;
import java.io.IOException;
import avdta.project.FourStepProject;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
        ReadDemandNetwork read = new ReadDemandNetwork();
        
        DemandProfile profile = read.readDemandProfile(project);
        
        
        Map<Integer, Double[]> productions = new HashMap<Integer, Double[]>();
        
        int max_time = 0;
        int ast_duration = Integer.parseInt(project.getOption("ast-duration"));
        
        Scanner filein = new Scanner(dynamic_od);
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int o = filein.nextInt();
            int d = filein.nextInt();
            double dem = filein.nextDouble();
            int ast = filein.nextInt();
            
            max_time = (int)Math.max(max_time, profile.get(ast).getEnd());
            
            if(productions.containsKey(o))
            {
                Double[] temp = productions.get(o);
                temp[0] += dem;
            }
            else
            {
                productions.put(o, new Double[]{dem, 0.0});
            }
            
            if(productions.containsKey(d))
            {
                Double[] temp = productions.get(d);
                temp[1] += dem;
            }
            else
            {
                productions.put(o, new Double[]{0.0, dem});
            }
        }
        filein.close();
        
        project.setOption("demand-asts", ""+(int)Math.ceil(max_time / ast_duration));
        project.writeOptions();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getZonesFile()), true);
        fileout.println(ReadFourStepNetwork.getZonesFileHeader());
        
        Random rand = new Random();
        
        for(int o : productions.keySet())
        {
            Double[] temp = productions.get(o);
            double pat = rand.nextGaussian() * 7200;
            fileout.println(o+"\t"+temp[0]+"\t"+temp[1]+"\t"+pat+"\t"+5);
        }
        fileout.close();
    }
}
