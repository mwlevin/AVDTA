/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;

/**
 *
 * @author ut
 */

// convert VISTA output - vehicle_path, vehicle_path_time into format read by simulator
public class ConvertVISTAoutput 
{
    
    public static void convert(String network) throws IOException
    {
        convert(network, new File(network+"_vehicle_path.txt"), new File(network+"_vehicle_path_time.txt"));
    }
    
    public static void convert(String network, File vehicle_path, File vehicle_path_time) throws IOException
    {
        File dir = new File("save/"+network);
        dir.mkdirs();

        PrintStream path_out = new PrintStream(new FileOutputStream(new File("save/"+network+"/path.dat")), true);
        
        Scanner path_in = new Scanner(vehicle_path);
        
        int max_id = 0;
        
        while(path_in.hasNextInt())
        {
            int id = path_in.nextInt();
            
            max_id = (int)Math.max(id, max_id);
            
            path_in.nextLine();
        }
        
        path_in.close();
        
        path_out.println(max_id + 1);
        
        path_in = new Scanner(vehicle_path);
        
        while(path_in.hasNextInt())
        {
            int id = path_in.nextInt();
            
            path_in.next();
            path_in.next();
            path_in.next();
            
            int size = path_in.nextInt();
            
            path_in.next();
            path_in.next();
            path_in.next();
            
            String rem = path_in.nextLine().trim();
            rem = rem.substring(1, rem.length()-1);
            
            String[] split = rem.split(",");
            
            path_out.print(id+"\t"+size);
            
            for(String x : split)
            {
                path_out.print("\t"+Integer.parseInt(x));
            }
            
            path_out.println();
        }
        
        path_in.close();
        path_out.close();
        
        Scanner veh_in = new Scanner(vehicle_path_time);
        
        PrintStream veh_out = new PrintStream(new FileOutputStream(new File("save/"+network+"/vehicles.dat")), true);
        
        while(veh_in.hasNextInt())
        {
            int id = veh_in.nextInt();
            veh_in.next();
            
            int dtime = veh_in.nextInt();
            veh_in.next();
            veh_in.next();
            
            int path = veh_in.nextInt();
            
            veh_in.nextLine();
            
            veh_out.println(id+"\t"+dtime+"\t"+path+"\tAV\tICV\t1");
        }
        
        veh_out.close();
        
    }
}
