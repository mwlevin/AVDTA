/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.demand.AST;
import avdta.demand.DemandProfile;
import avdta.demand.ReadDemandNetwork;
import avdta.demand.StaticODTable;
import avdta.dta.Assignment;
import avdta.dta.DTASimulator;
import avdta.dta.ReadDTANetwork;
import avdta.network.Path;
import avdta.network.PathList;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import avdta.vehicle.Vehicle;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author mlevin
 */
public class MaxPressureTest 
{
    public static MPSimulator createMPSimulator(DTAProject project, double vph, int duration) throws IOException
    {
        ReadDTANetwork read = new ReadDTANetwork();
        DemandProfile profile = new DemandProfile();
        profile.add(new AST(1, 0, duration, 1.0));
        profile.save(project);
        
        StaticODTable staticOd = new StaticODTable(project);
        read.createDynamicOD(project, duration / 3600.0 * vph / staticOd.getTotal());
        
        read.prepareDemand(project);
        
        
        
        
        read.readOptions(project);
        Set<Node> nodes = read.readNodes(project);
        Set<Link> links = read.readLinks(project);
        
        read.readIntersections(project);
        read.readPhases(project);
        
        
        MPSimulator sim = new MPSimulator(project, nodes, links);

        read.readVehicles(project, sim);
        
        sim.initialize();

                
                
        Assignment assign = new Assignment("mp_proportion", project);
        sim.setPaths(assign);
        
        PathList paths = sim.getPaths();
        
        for(Vehicle v : sim.getVehicles())
        {
            Path path = paths.randomPath(v.getOrigin(), v.getDest());
            
            if(path == null)
            {
                path = paths.addPath(sim.findPath(v.getOrigin(), v.getDest()));
                path.proportion = 1;
            }
            v.setPath(path);
        }
        
        sim.calculateTurningProportionsMP(assign);
        
        
        Simulator.duration = duration;
        
        return sim;
    }
}
