/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import avdta.project.DUERProject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class ReadDUERNetwork extends ReadDTANetwork
{
    private Map<Integer, Incident> incidentsmap;
    
    public ReadDUERNetwork()
    {
        incidentsmap = new HashMap<Integer, Incident>();
    }
    
    /**
     * Constructs a {@link DTASimulator} for the given {@link DTAProject}. 
     * This calls {@link ReadNetwork#readNodes(avdta.project.Project)} and {@link ReadNetwork#readLinks(avdta.project.Project)} and initializes the simulator ({@link Simulator#initialize()}).
     * This also reads transit ({@link ReadDTANetwork#readTransit(avdta.project.TransitProject)} and personal vehicles ({@link ReadDTANetwork#readVehicles(avdta.project.DTAProject)}).
     * @param project the {@link DTAProject}
     * @return the created {@link DTASimulator}
     * @throws IOException if a file cannot be accessed
     */
    public DUERSimulator readNetwork(DUERProject project) throws IOException
    {
        readOptions(project);
        Set<Node> nodes = readNodes(project);
        Set<Link> links = readLinks(project);
        Set<Incident> incidents = readIncidents(project);
        
        readVMS(project);
        
        readIntersections(project);
        readPhases(project);
        
        
        DUERSimulator sim = new DUERSimulator(project, nodes, links, incidents);

        readVehicles(project, sim);
        
        sim.initialize();
        
        
        
        return sim;
    }
    
    public void readVMS(DUERProject project) throws IOException
    {
        Scanner filein = new Scanner(project.getVMSFile());
        filein.nextLine();
        
        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            int type = filein.nextInt();
            String line = filein.nextLine();
            
            Map<Incident, Double> information = new HashMap<Incident, Double>();
            while(line.indexOf('(') >= 0)
            {
                String effect = line.substring(line.indexOf('(')+1, line.indexOf(')'));
                line = line.substring(line.indexOf(')')+1);
                
                effect = effect.replaceAll(",", " ");
                Scanner chopper = new Scanner(effect);
                
                int incidentId = chopper.nextInt();
                double prob = chopper.nextDouble();
                
                information.put(incidentsmap.get(incidentId), prob);
            }
            
            nodesmap.get(id).setVMS(new VMS(information));
        }
        filein.close();
    }
    
    public Set<Incident> readIncidents(DUERProject project) throws IOException
    {
        Set<Incident> output = new HashSet<Incident>();
        
        Scanner filein = new Scanner(project.getIncidentsFile());
        
        filein.nextLine();
        

        while(filein.hasNextInt())
        {
            int id = filein.nextInt();
            double pOn = filein.nextDouble();
            double pOff = filein.nextDouble();
            

            String line = filein.nextLine().trim();
            
            List<IncidentEffect> effects = new ArrayList<IncidentEffect>();
            
            while(line.indexOf('(') >= 0)
            {
                String effect = line.substring(line.indexOf('(')+1, line.indexOf(')'));
                line = line.substring(line.indexOf(')')+1);
                
                effect = effect.replaceAll(",", " ");
                Scanner chopper = new Scanner(effect);
                
                int linkid = chopper.nextInt();
                int lanesOpen = chopper.nextInt();
                double capPerLane = chopper.nextDouble();
                
                Link link = linksmap.get(linkid);
                
                effects.add(new IncidentEffect(link, lanesOpen, capPerLane));
            }
            
            Incident incident = new Incident(id, pOn, pOff, effects);
            output.add(incident);
            incidentsmap.put(id, incident);
        }
        

        output.add(Incident.UNKNOWN);

        return output;
    }
    
    public static String getIncidentsFileHeader()
    {
        return "id\tprob_on\tprob_off\teffects";
    }
    
    public static String getVMSFileHeader()
    {
        return "node id\ttype\tinformation";
    }
}
