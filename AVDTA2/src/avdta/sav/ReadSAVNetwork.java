/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.sav;

import avdta.dta.DTASimulator;
import static avdta.dta.ReadDTANetwork.getDemandFileHeader;
import avdta.dta.VehicleRecord;
import avdta.demand.AST;
import avdta.demand.DemandProfile;
import avdta.demand.ReadDemandNetwork;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Location;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.sav.SAVTraveler;
import avdta.sav.SAVSimulator;
import avdta.sav.SAVDest;
import avdta.sav.SAVOrigin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import avdta.project.SAVProject;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.TreeSet;

/**
 * This contains methods to read a SAV network, fleet, and demand from file.
 * To access, call {@link ReadSAVNetwork#readNetwork(avdta.project.SAVProject)}.
 * @author Michael
 */
public class ReadSAVNetwork extends ReadDemandNetwork
{
    public static final int TAXI = 200;
    
    public static final int TRAVELER = 600;
    

    /**
     * Creates a new {@link ReadSAVNetwork}
     */
    public ReadSAVNetwork()
    {
   
    }
    
    /**
     * Reads a {@link SAVSimulator} from the files specified by the project.
     * This replaces zones with {@link SAVOrigin} and {@link SAVDest} as appropriate.
     * This also links origin and destination zones.
     * Finally, it creates travelers and the SAV fleet.
     * @param project the project
     * @return the associated simulator
     * @throws IOException if a file is not found
     */
    public SAVSimulator readNetwork(SAVProject project) throws IOException
    {
        readOptions(project);
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);

        
        readIntersections(project);
        readPhases(project);
        
        
        SAVSimulator sim = new SAVSimulator(project, nodes, links);

        sim.initialize();
        
        
        sim.setTravelers(readTravelers(project));
        readFleet(project, sim);
        
        return sim;
    }
    
    /**
     * Creates zones for the specified id and location.
     * The destination zone uses -id.
     * @param id the id of the zone
     * @param loc the location
     */
    public Zone[] createZones(int id, Location loc)
    {
        Zone origin = new SAVOrigin(id, loc);
        Zone dest = new SAVDest(-id, loc);
        
        origin.setLinkedZone(dest);
        dest.setLinkedZone(origin);
        
        return new Zone[]{origin, dest};
    }
    
    /**
     * This creates a list of travelers from the demand file.
     * @param project the project
     * @return a list of travelers
     * @throws IOException if a file is not found
     */
    public List<SAVTraveler> readTravelers(SAVProject project) throws IOException
    {
        List<SAVTraveler> travelers = new ArrayList<SAVTraveler>();
        
        Scanner filein = new Scanner(project.getDemandFile());
        
        filein.nextLine();
        
        
        while(filein.hasNext())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            int origin_id = filein.nextInt();
            int dest_id = -filein.nextInt();
            int dtime = filein.nextInt();
            double vot = filein.nextDouble();
            filein.nextLine();
         
            if(type == TRAVELER)
            {
                try
                {
                    SAVOrigin origin = (SAVOrigin)nodesmap.get(origin_id);
                    SAVDest dest = (SAVDest)nodesmap.get(dest_id);

                    origin.addProductions(1);
                    dest.addAttractions(1);

                    travelers.add(new SAVTraveler(id, origin, dest, dtime, vot));
                }
                catch(Exception ex)
                {
                    System.out.println(origin_id+" "+dest_id);
                    ex.printStackTrace(System.err);
                    System.exit(1);
                }
                

                
                
            }
        }
        
        filein.close();

        return travelers;
        
    }
    

    
    
    
    

    
  
    /**
     * Returns the header for the fleet file.
     * @return the header for the fleet file
     */
    public static String getFleetFileHeader()
    {
        return "id\torigin\tcapacity";
    }
    
    /**
     * This creates a fleet of the specified size, distributing them among centroids proportional to productions.
     * The output is in the project fleet file.
     * @param project the project
     * @param total the fleet size
     * @throws IOException if a file cannot be accessed
     */
    public void createFleet(SAVProject project, int total) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getFleetFile()), true);
        
        fileout.println(getFleetFileHeader());
        
        int total_productions = project.getSimulator().getNumTrips();
        
        int new_id = 1;
        
        for(Node n : project.getSimulator().getNodes())
        {
            if(!(n instanceof Zone))
            {
                continue;
            }
            
            Zone z = (Zone)n;
            

            if(z instanceof SAVOrigin)
            {
                
                SAVOrigin o = (SAVOrigin)z;
                
                int num = (int)Math.round((double)total * o.getProductions() / total_productions);
                

                for(int i = 0; i < num; i++)
                {
                    fileout.println((new_id++)+"\t"+z.getId()+"\t"+4);
                }
            }
        }
        fileout.close();
        
    }
    
    /**
     * This creates a fleet of the specified size, distributing them among centroids proportional to productions.
     * The output is in the project fleet file.
     * @param project the project
     * @param total the fleet size
     * @throws IOException if a file cannot be accessed
     */
    public void createFleetEq(SAVProject project, int total) throws IOException
    {
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getFleetFile()), true);
        
        fileout.println(getFleetFileHeader());

        
        int new_id = 1;
        
        int count = 0;

        
        for(Node n : project.getSimulator().getNodes())
        {
            if((n instanceof Zone) && n.getId() > 0)
            {
                count++;
            }
            
        }
        
        System.out.println(count);

        for(Node n : project.getSimulator().getNodes())
        {
            if(!(n instanceof Zone))
            {
                continue;
            }
            
            Zone z = (Zone)n;
            

            if(z instanceof SAVOrigin)
            {
                
                SAVOrigin o = (SAVOrigin)z;
                
                int num = total / count;
                

                for(int i = 0; i < num; i++)
                {
                    fileout.println((new_id++)+"\t"+z.getId()+"\t"+4);
                }
            }
        }
        fileout.close();
        
    }
    
    /**
     * Read the fleet from the fleet file.
     * @param project the project
     * @param sim the {@link SAVSimulator} (its construction is in progress)
     * @throws IOException if a file cannot be accessed
     */
    public void readFleet(SAVProject project, SAVSimulator sim) throws IOException
    {
        Scanner filein = new Scanner(project.getFleetFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int origin_id = filein.nextInt();
            int capacity = filein.nextInt();
            
            Taxi taxi = new Taxi(id, (SAVOrigin)nodesmap.get(origin_id), capacity);
            

            sim.addTaxi(taxi);
        }
        
        filein.close();
    }
    
    /**
     * This goes through the static_od, dynamic_od, and demand files, and changes the type to {@link ReadSAVNetwork#TRAVELER}.
     * Then, this calls {@link ReadDemandNetwork#prepareDemand(avdta.project.DemandProject, double)} on the modified trip table.
     * @param project the project
     * @throws IOException if a file cannot be accessed
     */
    public void setTripsToTravelers(SAVProject project) throws IOException
    {
        ReadSAVNetwork read = new ReadSAVNetwork();
        Map<Integer, Double> proportion = new HashMap<Integer, Double>();
        
        proportion.put(TRAVELER, 1.0);
        read.changeDynamicType(project, proportion);
        read.changeStaticType(project, proportion);
        read.prepareDemand(project, 1.0);
    }
}
