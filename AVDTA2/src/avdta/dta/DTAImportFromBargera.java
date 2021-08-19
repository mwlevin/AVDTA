/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.demand.ReadDemandNetwork;
import avdta.demand.StaticODRecord;
import avdta.network.ReadNetwork;
import avdta.project.DTAProject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import avdta.network.node.NodeRecord;
import avdta.network.link.LinkRecord;

/**
 *
 * @author micha
 */
public class DTAImportFromBargera 
{
    private Set<Integer> nodesCreated;
    private PrintStream nodes;
    
    public DTAImportFromBargera(String name, File net, File trips, String length_units, String time_units) throws IOException
    {
        DTAProject project = new DTAProject();
        project.createProject(name, new File("projects/"+name));
        
        nodes = new PrintStream(new FileOutputStream(project.getNodesFile()), true);
        PrintStream links = new PrintStream(new FileOutputStream(project.getLinksFile()), true);
        
        nodes.println(ReadNetwork.getNodesFileHeader());
        links.println(ReadNetwork.getLinksFileHeader());
        
        Scanner filein = new Scanner(net);
        
        int firstThruNode = 1;
        
        String line;
        
        while( (line = filein.nextLine()).indexOf("<END OF METADATA>") < 0)
        {
            if(line.indexOf("<FIRST THRU NODE>") >= 0)
            {
                firstThruNode = Integer.parseInt(line.substring(line.indexOf(">")+1).trim());
            }
        }
        
        while(!filein.hasNextInt())
        {
            filein.nextLine();
        }
        
        nodesCreated = new HashSet<>();
        
        
        int linkid = 1;
        
        while(filein.hasNextInt())
        {
            int origin = filein.nextInt();
            int dest = filein.nextInt();
            
            createNode(origin, origin >= firstThruNode);
            createNode(dest, dest >= firstThruNode);
            
            
            
            double capacity = filein.nextDouble();
            double length = filein.nextDouble();
            double fftime = filein.nextDouble();
            
            filein.nextLine();
            
            int type;
            
            if(origin < firstThruNode || dest < firstThruNode)
            {
                type = 1000;
            }
            else
            {
                type = 100;
            }
            
            if(length_units.equalsIgnoreCase("ft"))
            {
                length /= 5280;
            }
            
            if(time_units.equalsIgnoreCase("min"))
            {
                fftime /= 60;
            }
            
            double ffspeed = length / fftime;
            
            LinkRecord record = new LinkRecord(linkid++, type, origin, dest, length, ffspeed, ffspeed/2, capacity, 1);
            links.println(record);
        }
        
        filein.close();
        
        nodes.close();
        links.close();
        
        filein = new Scanner(trips);
        
        PrintStream staticod = new PrintStream(new FileOutputStream(project.getStaticODFile()), true);
        staticod.println(ReadDemandNetwork.getStaticODFileHeader());
        
        
        while( (line = filein.nextLine()).indexOf("<END OF METADATA>") < 0);
        
        int origin = 0;
        
        int demandid = 1;
        
        while(filein.hasNext())
        {
            if(filein.hasNextInt())
            {
                int dest = filein.nextInt();
                filein.next(); // :
                String next = filein.next();
                
                if(next.charAt(next.length()-1) == ';')
                {
                    next = next.substring(0, next.length()-1);
                }
                else
                {
                    filein.next();
                }
                double dem = Double.parseDouble(next);
                
                if(dem > 0)
                {
                    StaticODRecord record = new StaticODRecord(demandid++, 111, origin, dest, dem);
                    staticod.println(record);
                }
            }
            else
            {
                String next = filein.next();

                if(next.equals("Origin"))
                {
                    origin = filein.nextInt();
                }
            }
            
        }
        
        staticod.close();
    }
    
    private boolean createNode(int id, boolean thru)
    {
        if(!nodesCreated.contains(id))
        {
            NodeRecord record = new NodeRecord(id, thru? 100:1000, 0, 0, 0);
            nodes.println(record);
            nodesCreated.add(id);
            return true;
        }
        else
        {
            return false;
        }
    }
}
