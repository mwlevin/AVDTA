/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.network.Path;
import avdta.network.PathList;
import avdta.network.link.Link;
import avdta.project.DTAProject;
import avdta.vehicle.Vehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import avdta.demand.DemandImportFromVISTA;

/**
 * This class allows the import of an assignment from VISTA, using the vehicle_path and vehicle_path_time tables from VISTA.
 * To import demand, see {@link DemandImportFromVISTA}.
 * @author Michael
 */
public class DTAImportFromVISTA 
{
    /**
     * Calls {@link DTAImportFromVISTA#DTAImportFromVISTA(avdta.project.DTAProject, java.io.File, java.io.File)} with a {@code vehicle_path.txt} file and a {@code vehicle_path_time.txt} file from the specified directory.
     * @param project the project
     * @param directory the directory to look for files
     * @throws IOException if a file cannot be accessed
     */
    public DTAImportFromVISTA(DTAProject project, String directory) throws IOException
    {
        this(project, new File(directory+"/vehicle_path.txt"), new File(directory+"/vehicle_path_time.txt"));
    }
    
    /**
     * Constructs the {@link DTAImportFromVISTA} with the specified project, vehicle_path, and vehicle_path_time table from VISTA.
     * This method takes the VISTA assignment and creates a corresponding assignment for the specified project.
     * Note that vehicle and link ids must match. 
     * Importing the network and demand from VISTA will result in matching ids.
     * 
     * Also note that new path ids may not match VISTA path ids. 
     * However, they will be unique, and corrected for each vehicle.
     * 
     * @param project the project
     * @param vehicle_path the file containing the VISTA vehicle_path table
     * @param vehicle_path_time the file containing the VISTA vehicle_path_time table
     * @throws IOException if a file cannot be accessed
     */
    public DTAImportFromVISTA(DTAProject project, File vehicle_path, File vehicle_path_time) throws IOException
    {
        DTASimulator sim = project.getSimulator();
        
        Map<Integer, Path> pathmap = new HashMap<Integer, Path>();
        
        PathList paths = sim.getPaths();
        
        Map<Integer, Link> linksmap = sim.createLinkIdsMap();
        
        // read in all paths
        
        Scanner filein = new Scanner(vehicle_path);
        
        // remove header data
        while(!filein.hasNextInt())
        {
            filein.nextLine();
        }
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            filein.nextDouble();
            
            int size = filein.nextInt();
            filein.nextDouble();
            filein.nextDouble();
            filein.nextInt();
            
            String line = filein.nextLine();
            line = line.substring(line.indexOf('{')+1, line.indexOf('}'));
            
            String[] split = line.split(",");
            
            Path path = new Path();
            
            for(String x : split)
            {
                int link = Integer.parseInt(x.trim());
                path.add(linksmap.get(link));
            }
            
            pathmap.put(id, paths.addPath(path));
        }
        filein.close();
        
        
        
        // free up memory
        linksmap = null;
        
        
        
        // write sim.vat
        DTAResults results = new DTAResults();
        Assignment assign = new Assignment(project, results);
        
        paths.writeToFile(assign);
        paths = null;
        
        
        PrintStream fileout = new PrintStream(new FileOutputStream(assign.getSimVatFile()), true);
        
        Map<Integer, Vehicle> vehmap = sim.createVehicleIdsMap();
        
        filein = new Scanner(vehicle_path_time);
        
        double tstt = 0;
        int num_veh = 0;
        int exiting = 0;
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int vista_type = filein.nextInt();
            

            int dtime = filein.nextInt();
            filein.next();
            
            int etime;
            if(filein.hasNextInt())
            {
                etime = filein.nextInt();
            }
            else
            {
                etime = -1;
                filein.next();
            }
            
            filein.nextInt();
            int pathid = filein.nextInt();
            
            
            tstt += (etime - dtime);
            
            String line = filein.nextLine();
            
            // ignore buses
            if(vista_type >= 500)
            {
                continue;
            }
            
            num_veh++;
            if(etime > 0)
            {
                exiting++;
            }
            
            line = line.substring(line.indexOf('{')+1, line.indexOf('}'));
            
            String[] split = line.split(",");
            
            int[] arrTimes = new int[split.length];
            
            for(int i = 0; i < split.length; i++)
            {
                arrTimes[i] = Integer.parseInt(split[i].trim());
            }
            
            Vehicle v = vehmap.get(id);
            
            if(v == null)
            {
                continue;
            }
            v.setExitTime(etime);
            
            Path path = pathmap.get(pathid);
            v.setPath(path);
            
            
            // write sim.vat
            fileout.println(v.getType()+" "+v.getId()+" "+dtime+".00 "+etime+".00");
            
            fileout.print(path.size());
            
            fileout.println();

            for(int i = 0; i < path.size(); i++)
            {
                fileout.print(" "+path.get(i).getId()+" "+arrTimes[i]+".00");
            }
            fileout.println();
        }
        filein.close();
        fileout.close();
        
        results.setTSTT(tstt);
        results.setMinTT(tstt);
        results.setExiting(exiting);
        results.setTrips(num_veh);
        
        assign.writeToFile(sim.getVehicles(), project);
    }
}
