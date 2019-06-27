/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.demand.StaticODTable;
import avdta.dta.Assignment;
import avdta.dta.DTASimulator;
import avdta.network.Path;
import avdta.network.PathList;
import avdta.network.Simulator;
import avdta.network.link.Link;
import avdta.network.node.Intersection;
import avdta.network.node.IntersectionControl;
import avdta.network.node.MPTurn;
import avdta.network.node.MaxPressure;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import avdta.project.DemandProject;
import avdta.project.Project;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author mlevin
 */
public class MPSimulator extends DTASimulator
{
    public MPSimulator(DTAProject project)
    {
        super(project);
    }
    
    public MPSimulator(DTAProject project, Set<Node> nodes, Set<Link> links)
    {
        super(project, nodes, links);
    }
    
    public void loadTurningProportionsMP(Assignment assign) throws IOException
    {
        Scanner filein = new Scanner(new File(assign.getResultsDirectory()+"/turning_proportions.txt"));
        
        Map<Integer, Link> linksmap = createLinkIdsMap();
        
        while(filein.hasNextInt())
        {
            int i_id = filein.nextInt();
            int j_id = filein.nextInt();
            double p_ij = filein.nextDouble();
            
            Link i = linksmap.get(i_id);
            Link j = linksmap.get(j_id);
            
            Node node = i.getDest();
            MaxPressure control = (MaxPressure)((Intersection)node).getControl();
            for(MPTurn t : control.getTurns())
            {
                if(t.i == i && t.j == j)
                {
                    t.setTurningProportion(p_ij);
                    break;
                }
            }
        }
        filein.close();
    }
    
    /**
     * This method calculates turning proportions for nodes when max-pressure is used.
     */
    public void calculateTurningProportionsMP(Assignment assign) throws IOException
    {
        PathList paths = new PathList(this, assign.getPathsFile());
        
        StaticODTable table = new StaticODTable((DemandProject)getProject());
        
        for(Path p : paths)
        {
            p.flow = p.proportion * table.getTrips(p.getOrigin(), p.getDest());
            
        }
        
        table = null;
        
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(assign.getResultsDirectory()+"/turning_proportions.txt")), true);
        
        for(Node n : nodes)
        {
            if(n instanceof Intersection)
            {
                IntersectionControl c = ((Intersection)n).getControl();
                
                if(!(c instanceof MaxPressure))
                {
                    continue;
                }
                
                MaxPressure control = (MaxPressure)c;
                
                for(MPTurn turn : control.getTurns())
                {
                    for(Path p : paths)
                    {
                        int idx = p.indexOf(turn.i);
                        if(idx >= 0)
                        {
                            turn.denom += p.flow;
                            
                            if(p.get(idx+1) == turn.j)
                            {
                                turn.num += p.flow;
                            }
                        }
                    }
                    
                    if(turn.denom > 0)
                    {
                        turn.setTurningProportion(turn.num / turn.denom);
                    }
                    else
                    {
                        turn.setTurningProportion(0);
                    }
                    
                    fileout.println(turn.i.getId()+"\t"+turn.j.getId()+"\t"+turn.getTurningProportion());
                }
            }
        }
        fileout.close();
    }
}
