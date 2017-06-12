/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.duer;

import avdta.dta.DTAResults;
import avdta.dta.DTASimulator;
import avdta.network.Path;
import avdta.network.Simulator;
import static avdta.network.Simulator.rand;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.project.DUERProject;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.Vehicle;
import avdta.vehicle.route.Hyperpath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ml26893
 */
public class DUERSimulator extends DTASimulator
{
    private Map<Incident, Map<Link, Double>> avgTT; // store average travel times per incident state
    private static final double rationality_bound = 0.5;
    
    private Set<Incident> incidents;
    
    private Incident activeIncident;
    private Set<IncidentEffect> null_effects;
    
    private Map<Link, List<State>> allStates;
    
    /**
     * Constructs this {@link DTASimulator} empty with the given project.
     * @param project the project
     */
    public DUERSimulator(DTAProject project)
    {
        super(project);
        
        avgTT = new HashMap<Incident, Map<Link, Double>>();
        incidents = new HashSet<Incident>();
        null_effects = new HashSet<IncidentEffect>();
        activeIncident = Incident.UNKNOWN;
        
        initializeTT();
        createStates();
    }
    
    public DUERSimulator(DUERProject project, Set<Node> nodes, Set<Link> links, Set<Incident> incidents)
    {
        super(project, nodes, links);
        avgTT = new HashMap<Incident, Map<Link, Double>>();
        this.incidents = incidents;
        null_effects = new HashSet<IncidentEffect>();
        activeIncident = Incident.UNKNOWN;
        
        initializeTT();
        createStates();
    }
    

    // this method is used with vms_test network
    public void test()
    {
        Map<Integer, Link> linksMap = createLinkIdsMap();
        Map<Integer, Incident> incidentsMap = createIncidentIdsMap();
        
        Incident inc = incidentsMap.get(2);
        Incident none = incidentsMap.get(1);
        Link AB = linksMap.get(12);
        Link AC = linksMap.get(13);
        Link BD = linksMap.get(24);
        Link CD = linksMap.get(34);
        Link DE = linksMap.get(46);
        Link DEtop1 = linksMap.get(45);
        Link DEtop2 = linksMap.get(56);
        
        avgTT.get(Incident.UNKNOWN).put(AB, 5.0);
        avgTT.get(none).put(AB, 5.0);
        avgTT.get(inc).put(AB, 5.0);
        
        avgTT.get(Incident.UNKNOWN).put(AC, 4.0);
        avgTT.get(none).put(AC, 4.0);
        avgTT.get(inc).put(AC, 4.0);
        
        avgTT.get(Incident.UNKNOWN).put(CD, 5.0);
        avgTT.get(none).put(CD, 5.0);
        avgTT.get(inc).put(CD, 5.0);
        
        avgTT.get(Incident.UNKNOWN).put(BD, 5.0);
        avgTT.get(none).put(BD, 5.0);
        avgTT.get(inc).put(BD, 5.0);
        
        avgTT.get(Incident.UNKNOWN).put(DE, 10.0);
        avgTT.get(none).put(DE, 10.0);
        avgTT.get(inc).put(DE, 60.0);
        
        avgTT.get(Incident.UNKNOWN).put(DEtop1, 15.0);
        avgTT.get(none).put(DEtop1, 15.0);
        avgTT.get(inc).put(DEtop1, 15.0);
        
        avgTT.get(Incident.UNKNOWN).put(DEtop2, 15.0);
        avgTT.get(none).put(DEtop2, 15.0);
        avgTT.get(inc).put(DEtop2, 15.0);
        
    }
    
    public void printStates()
    {
        for(Link l : allStates.keySet())
        {
            for(State s : allStates.get(l))
            {
                System.out.println(s.getLink()+"\t"+s.getIncident()+"\t"+s.J+"\t"+s.mu);
            }
        }
    }
    
    private void initializeTT()
    {
        Set<Link> links = getLinks();
        for(Incident i : incidents)
        {
            Map<Link, Double> temp = new HashMap<>();
            avgTT.put(i, temp);
            
            for(Link l : links)
            {
                temp.put(l, l.getFFTime());
            }
        }
    }
        
    private void createStates()
    {
        allStates = new HashMap<>();
        
        for(Link l : links)
        {
            Set<Link> U = null;
            
            List<State> temp = new ArrayList<>();
            allStates.put(l, temp);
            for(Incident i : incidents)
            {
                State state = new State(l, i);
                if(U == null)
                {
                    U = state.getActionSpace(DriverType.AV);
                }
                state.U = U;
                temp.add(state);
            }
        }
    }
    
    public boolean isObservable(Link l, Incident i)
    {
        if(i == Incident.UNKNOWN)
        {
            return false;
        }
        
        for(IncidentEffect effect : i.getEffects())
        {
            if(effect.getLink() == l)
            {
                return true;
            }
        }
        
        double normalTT = getAvgTT(l, Incident.UNKNOWN);
        double incidentTT = getAvgTT(l, i);
        
        return incidentTT > (1 + rationality_bound) * normalTT;
    }
    
    public double getAvgTT(Link l, Incident i)
    {
        if(i == Incident.UNKNOWN)
        {
            i = Incident.NONE;
        }
        
        return avgTT.get(i).get(l);
    }
    
    public Set<Incident> getIncidents()
    {
        return incidents;
    }
    
    public void activate(Incident i)
    {
        activeIncident = i;
        for(IncidentEffect e : i.getEffects())
        {
            Link link = e.getLink();
            
            null_effects.add(new IncidentEffect(link, link.getNumLanes(), link.getCapacityPerLane()));
            link.setNumLanes(e.getLanesOpen());
            link.setCapacityPerLane(e.getCapacityPerLane());
        }
    }
    
    public void deactivate(Incident i)
    {
        activeIncident = Incident.NONE;
        for(IncidentEffect e : null_effects)
        {
            Link link = e.getLink();
            link.setNumLanes(e.getLanesOpen());
            link.setCapacityPerLane(e.getCapacityPerLane());
        }
    }
    
    public Incident getIncident()
    {
        return activeIncident;
    }
    
    public void simulate() throws IOException
    {
        for(Incident i : incidents)
        {
            // activate incident
            activate(i);
            super.simulate();

            // store link travel times
            Map<Link, Double> tt = new HashMap<Link, Double>();
            for(Link l : getLinks())
            {
                double avg = 0;
                
                for(int ast = 0; ast < Simulator.num_asts; ast++)
                {
                    avg += l.getAvgTT(ast * Simulator.ast_duration);
                }
                
                avg /= Simulator.num_asts;
                
                tt.put(l, avg);
            }
            
            avgTT.put(i, tt);
            
            deactivate(i);
        }
    }
    
    public Map<Integer, Incident> createIncidentIdsMap()
    {
        Map<Integer, Incident> output = new HashMap<>();
        
        for(Incident i : incidents)
        {
            output.put(i.getId(), i);
        }
        return output;
    }
    /**
     * Generates new paths and loads 1/stepsize vehicles onto the new paths.
     * This method also compares minimum travel times with experienced travel times to calculate the gap.
     * @param stepsize the proportion of vehicles to move onto new paths.
     * @return the results from simulating the previous assignment
     * @throws IOException if a file cannot be accessed
     */
    public DTAResults pathgen(double stepsize) throws IOException
    {
        int error_count = 0;

        // map dest to new hyperpaths
        Map<Node, Hyperpath[][]> newpaths = new HashMap<Node, Hyperpath[][]>();

        double tstt = 0;
        double min = 0;
        int exiting = 0;

        int count = 0;
        int moved_count = 0;

        for(Vehicle x : vehicles)
        {
            
            
            PersonalVehicle v = (PersonalVehicle)x;
            
            Node o = v.getOrigin();
            Node d = v.getDest();
            int ast = v.getAST();
            int dep_time = v.getDepTime();


            if(v.getExitTime() < Simulator.duration)
            {
                exiting++;
            }


            if(x.isTransit())
            {
                continue;
            }
            
            Hyperpath[][] newPath; 
            
            if(newpaths.containsKey(d))
            {
                newPath = newpaths.get(d);
            }
            else
            {
                newpaths.put(d, newPath = new Hyperpath[Simulator.num_asts][]);
            }
            
            int drivertype = v.getDriver().typeIndex();
            
            if(newPath[ast] == null)
            {
                newPath[ast] = new Hyperpath[DriverType.num_types];
            }
            
            if(newPath[ast][drivertype] == null)
            {
                newPath[ast][drivertype] = osp(d, v.getDriver());
            }

            

            if(v.getRouteChoice() != null)
            {
                tstt += ((Hyperpath)v.getRouteChoice()).getAvgCost(v.getOrigin(), dep_time);
            }
            
            min += newPath[ast][v.getDriver().typeIndex()].getAvgCost(v.getOrigin(), dep_time);


            
            
            // move vehicle random chance
            if(v.getRouteChoice() == null || rand.nextDouble() < stepsize)
            {

                try
                {
                    v.setRouteChoice(newPath[ast][drivertype]);
                    moved_count++;
                }
                catch(Exception ex)
                {
                    out.println("Path unable: "+o+" "+d);
                    for(Link l : d.getIncoming())
                    {
                        out.println((l instanceof CentroidConnector)+" "+l.getSource());
                    }
                    error_count++;
                }
            }
            

            count ++;
        }

        
        if(error_count > 0)
        {
            System.err.println("Unable: "+error_count);
        }

        simulate();
        

        if(tstt == 0)
        {
            tstt = min;
        }


        return new DTAResults(min, tstt, vehicles.size(), exiting);

    }
    
    public static final double error_bound = 1.0e-4;
    
    // for simplicity, ignore driver type
    public Hyperpath osp(Node dest, DriverType driver)
    {
        // initialize
        for(Link l: allStates.keySet())
        {
            for(State s : allStates.get(l))
            {
                s.J = Integer.MAX_VALUE;
                s.mu = null;
            }
        }
        
        double error = 0.0;
        
        int iter = 0;
        do
        {
            iter++;
            error = vi_iter(dest);
            
            System.out.println(iter+"\t"+error);
        }
        while(error >= error_bound);
        
        // calculate best policy
        vi_iter(dest);
        
        Hyperpath output = new Hyperpath();
        
        for(Link l : allStates.keySet())
        {
            for(State s : allStates.get(l))
            {
                output.setNextLink(s.getLink(), s.getIncident(), s.mu);
            }
        }
        
        // origins
        for(Node n : getNodes())
        {
            if(n.isZone() && n.getOutgoing().size() > 0)
            {
                Link min = null;
                double best = Integer.MAX_VALUE;
                
                for(Link l : n.getOutgoing())
                {
                    State nullI = null;
                    // find null incident state
                    for(State s : allStates.get(l))
                    {
                        if(s.getIncident() == Incident.UNKNOWN)
                        {
                            nullI = s;
                            break;
                        }
                    }
                    
                    if(nullI.J < best)
                    {
                        best = nullI.J;
                        min = l;
                    }
                }
                // store in hyperpath
                output.setNextLink(n, best, min);
            }
        }
        
        return output;
    }
    
    private double vi_iter(Node dest)
    {
        double error = 0.0;
        for(Link link : allStates.keySet())
        {
            for(State s : allStates.get(link))
            {
                Incident inc = s.getIncident();

                double newJ = Integer.MAX_VALUE;
                double g = avgTT.get(inc).get(link);

                if(link.getDest() == dest)
                {
                    newJ = avgTT.get(inc).get(link);
                    s.mu = null;
                }
                else
                {
                    Link bestMu = null;

                    // look through outgoing links
                    for(Link u : s.U)
                    {
                        double expJ = 0.0;

                        // calculate transition
                        if(inc != Incident.UNKNOWN)
                        {
                            for(State sp : allStates.get(u))
                            {
                                if(sp.getIncident() == inc)
                                {
                                    expJ = sp.J;
                                    break;
                                }
                            }
                        }
                        else
                        {
                            State nullIncident = null;
                            double totalProb = 0.0;

                            for(State sp : allStates.get(u))
                            {
                                Incident ip = sp.getIncident();

                                if(ip == Incident.UNKNOWN)
                                {
                                    nullIncident = sp;
                                    continue;
                                }

                                double prob;

                                if(isObservable(u, ip))
                                {
                                    prob = ip.getProbabilityOn();
                                }
                                else
                                {
                                    prob = link.getDest().getVMS().getProbOfInformation(ip) * ip.getProbabilityOn();
                                }

                                totalProb += prob;

                                expJ += sp.J * prob;
                                
                                
                            }
                            
                            

                            // calculate null incident probability separately
                            expJ += nullIncident.J * (1 - totalProb);

                        }

                        double temp = g + expJ;
                        

                        if(temp < newJ)
                        {
                            newJ = temp;
                            bestMu = u;
                        }
                    }

                    s.mu = bestMu;
                }

                error = Math.max(error, Math.abs(newJ - s.J));
                s.J = newJ;
            }
        }
        return error;
    }
}
