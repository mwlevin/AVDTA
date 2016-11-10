/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.fourstep;

import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.Network;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.network.ReadNetwork;
import avdta.project.FourStepProject;
import avdta.project.Project;
import avdta.vehicle.DriverType;
import avdta.vehicle.Vehicle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author micha
 */
public class ReadFourStepNetwork extends ReadDTANetwork
{
    /**
     * Constructs the {@link ReadFourStepNetwork}
     */
    public ReadFourStepNetwork()
    {
        
    }
    
    
    /**
     * Constructs a {@link FourStepSimulator} for the given {@link FourStepProject}. 
     * This calls {@link ReadNetwork#readNodes(avdta.project.Project)} and {@link ReadNetwork#readLinks(avdta.project.Project)} and initializes the simulator ({@link Simulator#initialize()}).
     * This also reads transit ({@link ReadDTANetwork#readTransit(avdta.project.TransitProject)} and personal vehicles ({@link ReadDTANetwork#readVehicles(avdta.project.DTAProject)}).
     * @param project the {@link FourStepProject}
     * @return the created {@link FourStepSimulator}
     * @throws IOException if a file cannot be accessed
     */
    public FourStepSimulator readNetwork(FourStepProject project) throws IOException
    {
        readOptions(project);
        readFourStepOptions(project);
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        readZones(project);
        
        
        FourStepSimulator sim = new FourStepSimulator(project, nodes, links);

        readTransit(project);
        
        sim.initialize();
        
        
        
        return sim;
    }
    
    public void readZones(FourStepProject project) throws IOException
    {
        Scanner filein = new Scanner(project.getZonesFile());
        
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            double p = filein.nextDouble();
            double a = filein.nextDouble();
            double pat = filein.nextDouble();
            double parkFee = filein.nextDouble();
            
            if(nodesmap.containsKey(id))
            {
                Node node = nodesmap.get(id);
                
                if(node instanceof Zone)
                {
                    Zone origin = (Zone)node;
                    origin.setProductions(p);
                    
                    Zone dest = origin.getLinkedZone();
                    dest.setAttractions(a);
                    dest.setPreferredArrivalTime(pat);
                    dest.setParkingFee(parkFee);
                }
                else
                {
                    throw new RuntimeException("Node "+id+" is not a zone.");
                }
            }
            else
            {
                throw new RuntimeException("Zone "+id+" not found from productions.");
            }
        }
        
        filein.close();
    }
    
    
    
    /**
     * Reads the project options from the options file.
     * @param project the project
     */
    public void readFourStepOptions(FourStepProject project)
    {
        try
        {
            Scanner filein = new Scanner(project.getFourStepOptionsFile());
            
            filein.nextLine();
            
            while(filein.hasNext())
            {
                String key = filein.next().toLowerCase();
                String val = filein.next().toLowerCase();
                
                project.setOption(key, val);
                
                
            }
            filein.close();
        }
        catch(IOException ex)
        {
            ex.printStackTrace(System.err);
        }
    }
    
    
    
    public static String getZonesFileHeader()
    {
        return "node\tproductions\tattractions\tarrival_time\tparking_fee";
    }
    
}
