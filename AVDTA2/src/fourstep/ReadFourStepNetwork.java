/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fourstep;

import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import avdta.project.FourStepProject;
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
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);
        
        readIntersections(project);
        readPhases(project);
        
        readProductions(project);
        readAttractions(project);
        
        
        FourStepSimulator sim = new FourStepSimulator(project, nodes, links);

        
        
        sim.initialize();
        
        
        
        return sim;
    }
    
    public void readProductions(FourStepProject project) throws IOException
    {
        Scanner filein = new Scanner(project.getProductionsFile());
    }
    
    public static String getProductionsFileHeader()
    {
        return "node\tproductions";
    }
    
    public static String getAttractionsFileHeader()
    {
        return "node\tattractions";
    }
}
