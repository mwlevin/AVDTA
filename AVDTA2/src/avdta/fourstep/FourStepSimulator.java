/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.fourstep;

import avdta.demand.AST;
import avdta.demand.DemandProfile;
import avdta.demand.DynamicODTable;
import avdta.demand.ReadDemandNetwork;
import avdta.dta.Assignment;
import avdta.dta.DTAResults;
import avdta.dta.DTASimulator;
import avdta.network.Path;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.cost.TravelCost;
import avdta.network.link.Link;
import avdta.network.link.transit.TransitLink;
import avdta.network.link.transit.WalkingLink;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.project.FourStepProject;
import avdta.util.FileTransfer;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
public class FourStepSimulator extends DTASimulator
{
    private double TR_asc, logit_dispersion, transitFee, AV_asc, fuel_cost;
    
    // arrival time penalty values
    private double alpha, beta, gamma;
    
    private boolean allowRepos;
    private boolean allAVs;
    
    private Set<TransitLink> transitLinks;
    
    private Map<Zone, Map<Zone, CostTuple[]>> costs;
    private Set<Zone> zones;
    
    private int demand_asts;
    
    private FourStepAssignment assign;
    
    
    public FourStepSimulator(FourStepProject project)
    {
        super(project);
        
        setOptions(project);
        
        costs = new HashMap<Zone, Map<Zone, CostTuple[]>>();
    }
    
    public FourStepSimulator(FourStepProject project, Set<Node> nodes, Set<Link> links)
    {
        super(project, nodes, links);
        
        setOptions(project);
        
        costs = new HashMap<Zone, Map<Zone, CostTuple[]>>();
    }
    
    
    public Assignment createAssignment(int start_iter)
    {
        return assign;
    }
    
    /**
     * Updates the {@link Node}s of this network.
     * Calls {@link DTASimulator#setNodes(java.util.Set)}.
     * @param nodes the new set of {@link Node}s
     */
    public void setNodes(Set<Node> nodes)
    {
        super.setNodes(nodes);
        
        zones = new HashSet<Zone>();
        
        for(Node n : nodes)
        {
            if(n instanceof Zone)
            {
                zones.add((Zone)n);
            }
        }
    }
    
    /**
     * Updates the set of {@link Link}s.
     * Calls {@link DTASimulator#setLinks(java.util.Set)}.
     * @param links the new set of {@link Link}s
     */
    public void setLinks(Set<Link> links)
    {
        super.setLinks(links);
        
        transitLinks = new HashSet<TransitLink>();
        
        for(Link l : super.getLinks())
        {
            transitLinks.add(new WalkingLink(l));
        }
    }
    
    public void setOptions(FourStepProject project)
    {
        alpha = Double.parseDouble(project.getFourStepOption("arrival-time-alpha"));
        beta = Double.parseDouble(project.getFourStepOption("arrival-time-beta"));
        gamma = Double.parseDouble(project.getFourStepOption("arrival-time-gamma"));
        transitFee = Double.parseDouble(project.getFourStepOption("transit-fee"));
        TR_asc = Double.parseDouble(project.getFourStepOption("transit-asc"));
        AV_asc = Double.parseDouble(project.getFourStepOption("reposition-asc"));
        logit_dispersion = Double.parseDouble(project.getFourStepOption("logit-dispersion"));
        allowRepos = project.getFourStepOption("allow-repositioning").equalsIgnoreCase("true");
        allAVs = project.getFourStepOption("all-avs").equalsIgnoreCase("true");
        fuel_cost = Double.parseDouble(project.getFourStepOption("fuel-cost"));
        demand_asts = Integer.parseInt(project.getFourStepOption("demand-asts"));
    }
    
    
    /**
     * Indicates whether repositioning is allowed or not.
     * @return whether repositioning is allowed or not
     */
    public boolean allowRepositioning()
    {
        return allowRepos;
    }
    
    
    
    /**
     * Updates the alpha value for arrival time penalty.
     * Alpha is the penalty for travel time.
     * @param alpha the new alpha value ($/hr)
     */
    public void setAlpha(double alpha)
    {
        this.alpha = alpha;
    }
    
    /**
     * Updates the gamma value for arrival time penalty.
     * Gamma is the penalty for late arrival time.
     * @param gamma the new gamma value ($/hr)
     */
    public void setGamma(double gamma)
    {
        this.gamma = gamma;
    }
    
    /**
     * Updates the beta value for arrival time penalty.
     * Beta is the penalty for early arrival time.
     * @param beta the new beta value ($/hr)
     */
    public void setBeta(double beta)
    {
        this.beta = beta;
    }
    
    /**
     * Returns the alpha value for arrival time penalty.
     * Alpha is the penalty for travel time.
     * @return the alpha value ($/hr)
     */
    public double getAlpha()
    {
        return alpha;
    }
    
    /**
     * Returns the beta value for arrival time penalty.
     * Beta is the penalty for early arrival time.
     * @return the beta value ($/hr)
     */
    public double getBeta()
    {
        return beta;
    }
    
    /**
     * Returns the gamma value for arrival time penalty.
     * Gamma is the penalty for late arrival time.
     * @return the gamma value ($/hr)
     */
    public double getGamma()
    {
        return gamma;
    }
    
    /**
     * Returns the project associated with this simulator
     * @return the project associated with this simulator
     */
    public FourStepProject getProject()
    {
        return (FourStepProject)super.getProject();
    }
    
    
    public DTAResults four_step(int max_iter, int ta_iter, File output) throws IOException
    {
        return four_step(max_iter, ta_iter, 0, output);
    }
    
    public DTAResults four_step(int max_iter, int ta_iter, double ta_min_gap, File output) throws IOException
    {
        return four_step(max_iter, ta_iter, 0, ta_min_gap, output);
    }
    public DTAResults four_step(int max_iter, int ta_iter, int pd_iter, double ta_min_gap, File output) throws IOException
    {
        assign = new FourStepAssignment(getProject(), null, 1, 1);
        
        int AV_type = ReadNetwork.DA_VEHICLE + ReadNetwork.AV + ReadNetwork.ICV;
        int HV_type = ReadNetwork.DA_VEHICLE + ReadNetwork.HV + ReadNetwork.ICV;
        int type = allAVs? AV_type : HV_type;
        
        
        
        FourStepProject project = getProject();
        ReadFourStepNetwork read = new ReadFourStepNetwork();
        read.setNodesMap(createNodeIdsMap());
        
        // write demand profile
        DemandProfile profile = new DemandProfile();
        
        for(int t = 0; t < demand_asts; t++)
        {
            profile.put(t+1, new AST(t+1, 1, ast_duration*t, ast_duration));
        }
        
        profile.save(project);
        
        
        // init bus costs
        // clear demand file, prepare demand, read vehicles
        DynamicODTable odtable = new DynamicODTable();
        odtable.printDynamicOD(project);
        odtable.printStaticOD(project);
        read.prepareDemand(project, 0);
        
        
        read.readVehicles(project, this);
        
        simulate();
        
        int iter = 0;
        
        PrintStream fileout = new PrintStream(new FileOutputStream(output), true);
        
        fileout.println("Iter\tRMSE\tDA\tAV\tTR\tveh trips\tgap\ttime");
	
        DTAResults results = null;
        
        while(++iter <= max_iter)
        {
            long time2 = System.nanoTime();
            
            System.out.print("Calculating costs...");
            long time = System.nanoTime();
            initCosts();
            System.out.println("done "+String.format("%.1f", (System.nanoTime() - time)/1.0e9)+" s");
            
            System.out.print("Trip distribution...");
            time = System.nanoTime();
            double rmse = trip_distribution(1.0/iter);
            System.out.println("done. "+String.format("%.1f", (System.nanoTime() - time)/1.0e9)+" s");
            
            
            System.out.print("Mode choice...");
            time = System.nanoTime();
            double[] mode_flow = mode_choice(1.0/iter);
            System.out.println("done. "+String.format("%.1f", (System.nanoTime() - time)/1.0e9)+" s ");
            System.out.printf("DA: %.0f\nAV: %.0f\nTR: %.0f\n",mode_flow[0], mode_flow[1], mode_flow[2]);
            
            fileout.print(iter+"\t"+rmse+"\t"+String.format("%.0f\t%.0f\t%.0f", mode_flow[0], mode_flow[1], mode_flow[2])+"\t");
            
            // write dynamic OD table and prepare demand
            for(Zone o : costs.keySet())
            {
                for(Zone d : costs.get(o).keySet())
                {
                    for(int t = 0; t < Simulator.num_asts; t++)
                    {
                        odtable.addDemand(o, d, t, type, costs.get(o).get(d)[t].HV_flow);
                        odtable.addDemand(o, d, t, AV_type, costs.get(o).get(d)[t].AV_flow);
                    }
                }
            }
                    
            odtable.printDynamicOD(project);
            odtable.printStaticOD(project);
            createDemandProfile(project, odtable);
            
            read.prepareDemand(project, 1);
            
            read.readVehicles(project, this);
            
            System.out.println(vehicles.size()+" DA trips");
            fileout.print(vehicles.size());
            
            
            
            
            System.out.print("Traffic assignment...");
            time = System.nanoTime();
            results = traffic_assign(pd_iter, ta_iter, ta_min_gap);
            assign.setFourStepIter(iter);
            
            System.out.println("done. "+String.format("%.1f", (System.nanoTime() - time)/1.0e9));
            
            System.out.println("ITERATION "+iter+"\t"+String.format("%.2f", results.getGapPercent()));
            
            fileout.print("\t"+results.getGapPercent());


            fileout.println("\t"+String.format("%.1f", (System.nanoTime() - time2)/1.0e9));
        }
        
        fileout.close();
        
        return results;
    }
    
    /**
     * Saves the assignment in the given {@link Assignment} object.
     * This calls {@link Assignment#writeToFile(java.util.List, avdta.project.DTAProject)}.
     * @param assign the {@link Assignment} wrapper object
     * @throws IOException if a file cannot be accessed
     */
    public void saveAssignment(Assignment assign) throws IOException
    {        
        super.saveAssignment(assign);
        
        FourStepAssignment matrix = (FourStepAssignment)assign;
        FourStepProject project = getProject();
        
        FileTransfer.copy(project.getStaticODFile(), matrix.getStaticODFile());
        FileTransfer.copy(project.getZonesFile(), matrix.getZonesFile());
        FileTransfer.copy(project.getDynamicODFile(), matrix.getDynamicODFile());
        FileTransfer.copy(project.getDemandProfileFile(), matrix.getDemandProfileFile());
    }
    
    
    public static final int MAX_ITERATIONS = 10;
    public static final double MAX_ERROR = 0.0001;
    
    
    public void createDemandProfile(FourStepProject project, DynamicODTable table) throws IOException
    {
        int max_ast = table.getMaxAST();
        
        PrintStream fileout = new PrintStream(new FileOutputStream(project.getDemandProfileFile()), true);
        fileout.println(ReadDemandNetwork.getDemandProfileFileHeader());
        
        int ast_duration = Integer.parseInt(project.getOption("ast-duration"));
        
        for(int ast = 0; ast <= max_ast; ast++)
        {
            fileout.println(ast+"\t"+1+"\t"+(ast*ast_duration)+"\t"+ast_duration);
        }
    }
    
    public double trip_distribution(double step)
    {
        
        
        // 1. balance productions and attractions
        // initialize adjustment factors to 1
        for(Zone z : zones)
        {
            z.mu = 1;
        }
        
        double error = 0;
        int iteration = 1;

        do
        {
            error = 0;
            
            // a. calibrate origin weighting factors to ensure productions are consistent
            for(Zone r : zones)
            {
                double c = 0.0;
                
                for(Zone s : zones)
                {
                    if(r != s && r.getProductions() > 0)
                    {
                        for(int t = 0; t < demand_asts; t++)
                        {
                            c += s.mu * s.getAttractions() * friction(costs.get(r).get(s)[t].min_cost);
                            
                        }
                    }
                }
                
                r.C = c;
            }
            
            // b. generate empty trip table
            for(Zone r : zones)
            {
                if(r.C == 0)
                {
                    continue;
                }
                
                for(Zone s : zones)
                {
                    if(r != s)
                    {
                        for(int t = 0; t < demand_asts; t++)
                        {
                            CostTuple temp2 = costs.get(r).get(s)[t];
                            
                            temp2.total_flow_star = s.mu * r.getProductions() * s.getAttractions() * friction(temp2.min_cost) / r.C;
                            
                        }
                    }
                }
            }
            
            // c. change adjustment factors so attractions are consistent
            for(Zone s : zones)
            {
                double destSum = 0.0;
                
                for(Zone r : zones)
                {
                    if(r != s)
                    {
                        for(int t = 0; t < demand_asts; t++)
                        {
                            destSum += costs.get(r).get(s)[t].total_flow_star;
                        }
                    }
                }
                
                error = Math.max(error, Math.abs(destSum - s.getAttractions()) );
                
                if(destSum == 0)
                {
                    if(s.getAttractions() == 0)
                    {
                        s.mu = 1;
                    }
                    else
                    {
                        System.out.println("Warning: 0 demand despite positive attractions.");
                        s.mu = 1;
                    }
                }
                else
                {
                    s.mu *= s.getAttractions() / destSum;
                }
            }
            
        }
        while(iteration++ <= MAX_ITERATIONS && error >= MAX_ERROR);
        
        double output = 0;
        int count = 0;
        
        double total = 0;
        double total2 = 0;
        
        for(Zone r : costs.keySet())
        {
            for(Zone s : costs.get(r).keySet())
            {
                for(int t = 0; t < demand_asts; t++)
                {
                    CostTuple temp2 = costs.get(r).get(s)[t];
                    
                    double new_flow = temp2.total_flow_star * step + temp2.total_flow * (1 - step);

                    
                    output += (new_flow - temp2.total_flow) * (new_flow - temp2.total_flow);
                    temp2.total_flow = new_flow;

                    
                    count++;
                }
            }
        }
        
        return Math.sqrt(output / count);
    }
    
    private double friction(double cost)
    {
        return 1.0/cost;
    }
    
    public void initCosts()
    {

        for(Zone o : zones)
        {
            Map<Zone, CostTuple[]> temp;
            
            if(!costs.containsKey(o))
            {
                costs.put(o, temp = new HashMap<Zone, CostTuple[]>());
            }
            else
            {
                temp = costs.get(o);
            }
            
            for(int t = 0; t < Simulator.num_asts; t++)
            {
                int dtime = (int)((t+0.5)*Simulator.ast_duration);
                
                transit_dijkstras(o, dtime);
                
                for(Zone d : zones)
                {
                    if(o == d)
                    {
                        continue;
                    }

                    CostTuple[] temp2;

                    if(!temp.containsKey(d))
                    {
                        temp.put(d, temp2 = new CostTuple[Simulator.num_asts]);
                        
                        for(int i = 0; i < temp2.length; i++)
                        {
                            temp2[i] = new CostTuple();
                        }
                    }
                    else
                    {
                        temp2 = temp.get(d);
                    }
                    
                    if(d.label < 0)
                    {
                        System.out.println(d.label);
                    }

                    temp2[t].TR_cost = arrivalTimePenalty(d.label, dtime, d.getPreferredArrivalTime()) + transitFee;
                    
                }
                
                dijkstras(o, null, dtime, 1.0, DriverType.AV, TravelCost.ttCost);
                
                for(Zone d : zones)
                {
                    if(o == d)
                    {
                        continue;
                    }
                    
                    CostTuple temp2 = temp.get(d)[t];
                    
                    //Path path1 = sim.findPath(o, d, dtime, 1);
                    Path path1 = trace(o, d);
                    
                    temp2.DA_arr_time = dtime + (int)Math.round(path1.getAvgTT(dtime));
                    
                    
                    
                    double pref = d.getPreferredArrivalTime();

                    if(path1.size() == 0)
                    {
                        temp2.DA_cost = Integer.MAX_VALUE;
                    }
                    else
                    {
                        temp2.DA_cost = arrivalTimePenalty(dtime + path1.getAvgTT(dtime), dtime, d.getPreferredArrivalTime()) + path1.getFuel(dtime)*fuel_cost;
                    }
                    
                    temp2.AV_cost = Integer.MAX_VALUE;
                    
                    temp2.min_cost = Math.min(temp2.DA_cost, Math.min(temp2.TR_cost, temp2.AV_cost));
                    

                    temp2.AV_flow = 0.0;
                    temp2.HV_flow = 0.0;
                    temp2.TR_flow = 0.0;
                    temp2.total_flow_star = 0.0;
                    
                    
                    if(path1.size() == 0)
                    {
                        temp2.DA_cost = Integer.MAX_VALUE;
                    }
                    
                }
                
                dijkstras(o, null, dtime, 1.0, DriverType.AV, TravelCost.ttCost);
                
                for(Zone d : zones)
                {
                    if(o == d)
                    {
                        continue;
                    }
                    
                    CostTuple temp2 = temp.get(d)[t];
                    
                    Path path1 = trace(o, d);

                    temp2.fuelcost = path1.getFuel(dtime)*fuel_cost;
                }
            }
        }
        
        if(allowRepos)
        {
            for(Zone o : zones)
            {
                for(Zone d : zones)
                {
                    if(o == d)
                    {
                        continue;
                    }

                    for(int t = 0; t < demand_asts; t++)
                    {
                        CostTuple temp1 = costs.get(o).get(d)[t];
                        CostTuple temp2 = costs.get(d.getLinkedZone()).get(o.getLinkedZone())[Simulator.ast(temp1.DA_arr_time)];


                        temp1.AV_cost = temp1.DA_cost + temp2.fuelcost;

                        if(temp1.DA_cost == Integer.MAX_VALUE || temp2.DA_cost == Integer.MAX_VALUE)
                        {
                            temp1.AV_cost = Integer.MAX_VALUE;
                        }

                    }
                }
            }
        }

        for(Zone o : zones)
        {
            for(Zone d : zones)
            {
                if(o == d)
                {
                    continue;
                }
                
                for(int t = 0; t < demand_asts; t++)
                {
                    CostTuple temp1 = costs.get(o).get(d)[t];
                    
                    temp1.DA_cost += d.getParkingCost();
                }
            }
        }
            
    }
    
    public double[] mode_choice(double step)
    {
        double[] output = new double[3];
        
        double total = 0;
        
        for(Zone o : zones)
        {
            for(Zone d : zones)
            {
                if(o == d)
                {
                    continue;
                }
                
                for(int t = 0; t < demand_asts; t++)
                {
                    CostTuple temp2 = costs.get(o).get(d)[t];
                    
                    total += temp2.total_flow;
                    
                    // AV vs DA

                    double AV_util = Math.exp(logit_dispersion * (AV_asc - temp2.AV_cost));
                    double TR_util = Math.exp(logit_dispersion * (TR_asc - temp2.TR_cost));
                    double DA_util = Math.exp(logit_dispersion * (-temp2.DA_cost));
                    
                    /*
                    if(temp2.AV_cost < 0 || temp2.TR_cost < 0 || temp2.DA_cost < 0)
                    {
                        System.out.println(o+" "+d+" "+temp2.DA_cost+" "+temp2.AV_cost+" "+temp2.TR_cost);
                    }
                    */
                    double denom;
                    
                    if(allowRepos)
                    {
                        denom = TR_util + Math.max(AV_util, DA_util);
                    }
                    else
                    {
                        denom = TR_util + DA_util;
                    }

                    double TR_prop_star;

                    if(denom != 0)
                    {
                        TR_prop_star = TR_util / denom;
                    }
                    else
                    {
                        TR_prop_star = 0;
                    }

                    double denom2 = AV_util + DA_util;

                    
                    double AV_prop_star;

                    if(allowRepos && denom2 != 0)
                    {
                        AV_prop_star = (AV_util / denom2);
                    }
                    else
                    {
                        AV_prop_star = 0;
                    }

                    double TR_prop = TR_prop_star * step + temp2.TR_prop * (1- step);
                    double AV_prop = AV_prop_star * step + temp2.AV_prop * (1 - step);

                    double new_TR_flow = temp2.total_flow * TR_prop;
                    double remaining_flow = temp2.total_flow * (1 - TR_prop);


                    double new_AV_flow = remaining_flow * AV_prop;
                    double new_DA_flow = remaining_flow * (1 - AV_prop);

                    
                    temp2.AV_flow += new_AV_flow;
                    temp2.HV_flow += new_DA_flow;

                    // round trip
                    costs.get(d.getLinkedZone()).get(o.getLinkedZone())[Simulator.ast(temp2.DA_arr_time)].AV_flow += new_AV_flow;

                    temp2.TR_prop = TR_prop;
                    temp2.AV_prop = AV_prop;

 
                    temp2.TR_flow += new_TR_flow;

                    if(temp2.total_flow > 0)
                    {
                        output[2] += new_TR_flow;
                        output[1] += new_AV_flow;
                        output[0] += new_DA_flow;
                    }
                    
                }
            }
        }


        return output;
    }
    
    private double arrivalTimePenalty(double etime, int dtime, double pref)
    {
        double tt = etime - dtime;
        
        return alpha * tt / 3600.0 + beta * Math.max(0, pref - etime) / 3600.0 + gamma * Math.max(0, etime - pref) / 3600.0;
    }
    
    public void transit_dijkstras(Node o, int dep_time)
    {
        Set<Node> nodes = getNodes();
        
        for(Node n : nodes)
        {
            n.label = Integer.MAX_VALUE;
            n.transit_prev = null;
        }
        
        o.label = dep_time;
        
        Set<Node> Q = new HashSet<Node>();
        
        Q.add(o);
        
        
        while(!Q.isEmpty())
        {
            double min = Integer.MAX_VALUE;
            Node u = null;
            
            for(Node n : Q)
            {
                if(n.label < min)
                {
                    u = n;
                    min = n.label;
                }
            }
            
            Q.remove(u);
            
            for(TransitLink l : u.getTransitOut())
            {
                Node v = l.getDest();
                
                double temp = u.label;
                
                
                temp += l.getTT((int)temp);
                
                if(temp < v.label)
                {
                    v.label = temp;
                    v.transit_prev = l;
                    
                    if(!(v instanceof Zone))
                    {
                        Q.add(v);
                    }
                }
            }
            
        }
    }
    
    public DTAResults traffic_assign(int pd, int max_iter, double min_gap) throws IOException
    {
        if(pd > 0)
        {
            partial_demand(pd);
            return msa_cont(pd+1, max_iter, min_gap);
        }
        else
        {
            return msa(max_iter, min_gap);
        }
        
    }
    
    static class CostTuple
    {

        public double DA_cost;
        public double TR_cost;
        public double AV_cost;
        public int DA_arr_time;
        public double min_cost;
        public double total_flow;
        public double TR_flow;
        public double TR_prop;
        public double AV_prop;
        public double total_flow_star;
        public double fuelcost;
        
        public double AV_flow;
        public double HV_flow;
    }
}
