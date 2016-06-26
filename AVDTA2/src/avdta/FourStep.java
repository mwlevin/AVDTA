/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta;

import avdta.network.Results;
import avdta.network.Simulator;
import avdta.network.ReadNetwork;
import avdta.cost.TravelCost;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.link.BusLink;
import avdta.network.Path;
import avdta.network.link.WalkingLink;
import avdta.network.node.Zone;
import avdta.network.link.TransitLink;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.VehicleClass;
import avdta.vehicle.DriverType;
import avdta.vehicle.Bus;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author Michael
 */
public class FourStep 
{
    public static void main(String[] args) throws IOException
    {
        
        allAVs = false;
        
        String network = "coacongress2";
        
        
        test(network, true, false);
        test(network, true, true);
        
        
        allAVs = true;
        
        
        
        test(network, false, false);
        test(network, false, true);
    }
    public static void test(String network, boolean signals, boolean repositioning) throws IOException
    {
        Simulator sim2;
        
        if(signals)
        {
            sim2 = Simulator.readSignalsNetwork(network, Link.CTM); 
        }
        else
        {
            sim2 = Simulator.readTBRNetwork(network, Link.CTM); 
        }
        
        
        
        FourStep test2 = new FourStep(sim2, repositioning);
        
        String scenario = network+"/fourstep_"+(signals?"signals":"TBR")+(test2.allowRepositioning()?"_RP":"");
        
        File file = new File("results/"+scenario);
        file.mkdirs();
        
        
        sim2.setScenario(scenario);
        
        test2.readBuses(new File("data/"+network+"/bus_route_link.txt"), new File("data/"+network+"/bus_period.txt"), 
                new File("data/"+network+"/bus_frequency.txt"));
        
        test2.four_step(15, 50, new File("results/"+scenario+"/fourstep_log.txt"));
        test2.printODs(new File("results/"+scenario+"/od_data.txt"));
        test2.printLinks(new File("results/"+scenario+"/links.txt"));
        
        sim2 = null;
        test2 = null;



        
        
        
        
        
        
        
        
    }
    
    
    public static final double FUELCOST = 3.5;
    public static final double ENERGY_PER_GAL = 36.44;
    
    public static boolean allAVs = false;
    
    private Simulator sim;
    
    private Set<Zone> zones;
    
    private List<TransitLink> transitLinks;
    
    private List<PersonalVehicle> buses;
    
    private Map<Zone, Map<Zone, CostTuple[]>> costs;
    
    private double vot = 3.198; // $/hr
    private double TR_asc = -52.6, logit_dispersion = 0.0548, transitFee = 1;
    private double AV_asc = 0;
    
    
    // arrival time penalty values
    private double alpha = 1*vot, beta = 0.609375*vot, gamma = 2.376525*vot;
    
    private boolean allowRepos;
    
    public FourStep(Simulator sim, boolean reposTrips)
    {
        this.sim = sim;
        this.allowRepos = reposTrips;
        
        ReadNetwork input = new ReadNetwork();
        input.linkZones(sim);
        //input.createMissingZones(sim);
        input = null;
        
        
        zones = new HashSet<Zone>();
        
        for(Node n : sim.getNodes())
        {
            if(n instanceof Zone)
            {
                zones.add((Zone)n);
            }
        }
        
        sim.print_status = false;
        
        transitLinks = new ArrayList<TransitLink>();
        
        for(Link l : sim.getLinks())
        {
            transitLinks.add(new WalkingLink(l));
        }
        
        buses = new ArrayList<PersonalVehicle>();
        
        costs = new HashMap<Zone, Map<Zone, CostTuple[]>>();
        
        
        Random rand = new Random();
        
        for(Zone z : zones)
        {
           z.setPreferredArrivalTime(rand.nextGaussian() * Simulator.ast_duration + (Simulator.demand_asts - 2 )* Simulator.ast_duration);
           
           //System.out.println(z.getPreferredArrivalTime());
           z.setParkingFee(5);
        }
    }
    
    
    
    public boolean allowRepositioning()
    {
        return allowRepos;
    }
    
    public void printODs(File file) throws IOException
    {
        PrintWriter fileout = new PrintWriter(file);
        
        System.out.println("Printing costs..."+costs.size());
        fileout.println("Origin\tDest\tAST\tPK_cost\tRP_cost\tTR_cost\ttotal_trips\tTR_prop\tRP_prop\tDA_arr_time");
        
        for(Zone o : costs.keySet())
        {
            for(Zone d : costs.get(o).keySet())
            {
                for(int t = 0; t < Simulator.num_asts; t++)
                {
                    CostTuple temp2 = costs.get(o).get(d)[t];

                    if(temp2.total_flow > 0)
                    {
                        fileout.println(o+"\t"+d+"\t"+(t*Simulator.ast_duration)+"\t"+temp2.DA_cost+"\t"+temp2.AV_cost+"\t"+temp2.TR_cost+"\t"+temp2.total_flow+"\t"+temp2.TR_prop+"\t"+temp2.AV_prop+"\t"+temp2.DA_arr_time);
                        fileout.flush();
                    }
                }
            }
        }
        
        fileout.close();
    }
    
    public double getAvgLinkTT()
    {
        double output = 0;
        int count = 0;
        
        for(Link l : sim.getLinks())
        {
            if(!l.isCentroidConnector())
            {
                count++;
                output += l.getAvgTT(3600);
            }
        }
        
        return output/count;
    }
    
    public double getAvgLinkSpeed(int time)
    {
        double output = 0;
        int count = 0;
        
        for(Link l : sim.getLinks())
        {
            if(!l.isCentroidConnector())
            {
                count++;
                output += l.getAvgSpeed(time);
                
            }
        }
        
        return output/count;
    }
    
public void printLinks(File file) throws IOException
{
	PrintWriter fileout = new PrintWriter(file);

	fileout.print("link\tlength\tffspd\tcapacity");

	for(int t = 0; t < Simulator.demand_duration; t+=Simulator.ast_duration)
	{
		fileout.print("\t"+t);
	}
	fileout.println();
	fileout.flush();

	for(Link l : sim.getLinks())
	{
            if(l.isCentroidConnector())
            {
                continue;
            }
            
		fileout.print(l.getId()+"\t"+l.getLength()+"\t"+l.getFFSpeed()+"\t"+l.getCapacity());

		for(int t = 0; t < 10800; t += Simulator.ast_duration)
		{
			fileout.print("\t"+l.getAvgSpeed(t));
		}
		fileout.println();
		fileout.flush();
	}
	fileout.close();
}
    
    public void transit_dijkstras(Node o, int dep_time)
    {
        List<Node> nodes = sim.getNodes();
        
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
    
    public double trace(Node origin, Node dest)
    {
        double cost = 0;
        
        Node curr = dest;
        
        while(curr != origin)
        {
            cost += dest.label;
            curr = dest.transit_prev.getSource();
        }
        
        return cost;
    }
    
    public void readBuses(File bus_route_link, File bus_period, File bus_frequency) throws IOException
    {
        // map to {Path, ArrayList<BusLink>}
        Map<Integer, Object[]> routes = new HashMap<Integer, Object[]>();
        Map<Node, Map<Node, BusLink>> buslinks = new HashMap<Node, Map<Node, BusLink>>();
        

        Scanner filein = new Scanner(bus_route_link);
        
        int curr_route = -1;
        Path path = null;
        ArrayList<Boolean> stops = null;
        
        int num_stops = 0;
        
        while(filein.hasNext())
        {
            int route = filein.nextInt();
            int sequence = filein.nextInt();
            int link = filein.nextInt();
            boolean stop;
            
            if(filein.hasNextInt())
            {
                num_stops++;
                stop = true;
            }
            else
            {
                stop = false;
            }

            filein.nextLine();
            

            if(route != curr_route)
            {
                if(curr_route >= 0 && num_stops >= 2)
                {
                    routes.put(curr_route, new Object[]{path, createBusLinks(path, stops, buslinks)});

                }
                
                curr_route = route;
                path = new Path();
                stops = new ArrayList<Boolean>();
                num_stops = 0;
            }
            
            path.add(sim.getLink(link));
            stops.add(stop);
        }
        
        if(curr_route >= 0 && num_stops >= 2)
        {
            routes.put(curr_route, new Object[]{path, createBusLinks(path, stops, buslinks)});
        }
        
        filein.close();
        buslinks = null;
        
        Map<Integer, Integer[]> busPeriod = new HashMap<Integer, Integer[]>();
        
        
        
        
        filein = new Scanner(bus_period);
        
        while(filein.hasNext())
        {
            busPeriod.put(filein.nextInt(), new Integer[]{filein.nextInt(), filein.nextInt()});
        }
        
        filein.close();
        
        
        int bus_id = 9000001;
        
        filein = new Scanner(bus_frequency);
        
        while(filein.hasNext())
        {
            int route = filein.nextInt();
            int period = filein.nextInt();
            int freq = filein.nextInt();
            int offset = filein.nextInt();
            
            filein.nextLine();
            
            if(routes.containsKey(route))
            {
                Object[] temp = routes.get(route);
                path = (Path)temp[0];
                ArrayList<BusLink> links = (ArrayList<BusLink>)temp[1];
                
                Integer[] temp2 = busPeriod.get(period);

                int end = temp2[1];
                
                for(int t = temp2[0] + offset; t < end; t += freq)
                {
                    buses.add(new Bus(bus_id++, route, t, path, links));
                }
            }
        }
        
        filein.close();
    }
    
    private ArrayList<BusLink> createBusLinks(Path path, ArrayList<Boolean> stops, Map<Node, Map<Node, BusLink>> buslinks)
    {
        while(!stops.get(stops.size() -1 ))
        {
            stops.remove(stops.size() - 1);
            path.remove(path.size() - 1);
        }
        
        ArrayList<BusLink> output = new ArrayList<BusLink>();
        
        int curr_idx = 0;
        Node curr = null;
        
        while(!stops.get(curr_idx++));
        
        curr = path.get(curr_idx).getDest();
        
        for(int i = curr_idx; i < path.size(); i++)
        {
            if(stops.get(i))
            {
                Node next = path.get(i).getDest();
                
                BusLink temp;
                
                if(!buslinks.containsKey(curr))
                {
                    buslinks.put(curr, new HashMap<Node, BusLink>());
                }
                
                if(!buslinks.get(curr).containsKey(next))
                {
                    buslinks.get(curr).put(next, temp = new BusLink(curr, next));
                    transitLinks.add(temp);
                }
                else
                {
                    temp = buslinks.get(curr).get(next);
                }
                
                output.add(temp);
                
                curr = next;
            }
        }  
        
        return output;
    }
    
    public void four_step(int max_iter, int ta_iter, File output) throws IOException
    {
        
        // init bus costs
        
        sim.setVehicles(buses);
        sim.simulate();
        
        int iter = 0;
        
        PrintWriter fileout = new PrintWriter(output);
        
        fileout.print("Iter\tRMSE\tDA\tAV\tTR\tveh trips\tgap");
	
	
        for(int t = 0; t <= 7200; t += 900)
        {
                fileout.print("\t"+t);
        }
	fileout.println("\ttime");
        fileout.flush();
        
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
            
            List<PersonalVehicle> vehicles = discretize_trips();
            System.out.println(vehicles.size()+" DA trips");
            
            fileout.print(vehicles.size());
            
            addBuses(vehicles);
            
            
            
            sim.setVehicles(vehicles);
            
            System.out.print("Traffic assignment...");
            time = System.nanoTime();
            Results results = traffic_assign(ta_iter);
            System.out.println("done. "+String.format("%.1f", (System.nanoTime() - time)/1.0e9));
            
            System.out.println("ITERATION "+iter+"\t"+String.format("%.2f", results.getGapPercent()));
            
            fileout.print("\t"+results.getGapPercent());

		for(int t = 0; t <= 7200; t += 900)
		{
			fileout.print("\t"+getAvgLinkSpeed(t));
		}

            fileout.println("\t"+String.format("%.1f", (System.nanoTime() - time2)/1.0e9));
            fileout.flush();
        }
        
        fileout.close();
    }
    
    public static final int MAX_ITERATIONS = 10;
    public static final double MAX_ERROR = 0.0001;
        
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
                        for(int t = 0; t < Simulator.demand_asts; t++)
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
                        for(int t = 0; t < Simulator.demand_asts; t++)
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
                        for(int t = 0; t < Simulator.demand_asts; t++)
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
                for(int t = 0; t < Simulator.demand_asts; t++)
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
                
                sim.dijkstras(o, dtime, 1.0, DriverType.AV, TravelCost.ttCost);
                
                for(Zone d : zones)
                {
                    if(o == d)
                    {
                        continue;
                    }
                    
                    CostTuple temp2 = temp.get(d)[t];
                    
                    //Path path1 = sim.findPath(o, d, dtime, 1);
                    Path path1 = sim.trace(o, d);
                    
                    temp2.DA_arr_time = dtime + (int)Math.round(path1.getAvgTT(dtime));
                    
                    
                    
                    double pref = d.getPreferredArrivalTime();

                    if(path1.size() == 0)
                    {
                        temp2.DA_cost = Integer.MAX_VALUE;
                    }
                    else
                    {
                        temp2.DA_cost = arrivalTimePenalty(dtime + path1.getAvgTT(dtime), dtime, d.getPreferredArrivalTime()) + path1.getFuel(dtime)*FUELCOST;
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
                
                sim.dijkstras(o, dtime, 1.0, DriverType.AV, TravelCost.ttCost);
                
                for(Zone d : zones)
                {
                    if(o == d)
                    {
                        continue;
                    }
                    
                    CostTuple temp2 = temp.get(d)[t];
                    
                    Path path1 = sim.trace(o, d);

                    temp2.fuelcost = path1.getFuel(dtime)*FUELCOST;
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

                    for(int t = 0; t < Simulator.demand_asts; t++)
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
                
                for(int t = 0; t < Simulator.demand_asts; t++)
                {
                    CostTuple temp1 = costs.get(o).get(d)[t];
                    
                    temp1.DA_cost += d.getParkingCost();
                }
            }
        }
            
    }
    
    private double arrivalTimePenalty(double etime, int dtime, double pref)
    {
        double tt = etime - dtime;
        
        return alpha * tt / 3600.0 + beta * Math.max(0, pref - etime) / 3600.0 + gamma * Math.max(0, etime - pref) / 3600.0;
    }
    
    // return total transit flow
    public double[] mode_choice(double step)
    {
        System.out.println("step "+step);
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
                
                for(int t = 0; t < Simulator.demand_asts; t++)
                {
                    CostTuple temp2 = costs.get(o).get(d)[t];
                    
                    total += temp2.total_flow;
                    
                    // AV vs DA

                    double AV_util = Math.exp(logit_dispersion * (AV_asc - temp2.AV_cost));
                    double TR_util = Math.exp(logit_dispersion * (TR_asc - temp2.TR_cost));
                    double DA_util = Math.exp(logit_dispersion * (-temp2.DA_cost));
                    
                    
                    if(temp2.AV_cost < 0 || temp2.TR_cost < 0 || temp2.DA_cost < 0)
                    {
                        System.out.println(o+" "+d+" "+temp2.DA_cost+" "+temp2.AV_cost+" "+temp2.TR_cost);
                    }

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
        
        System.out.println("Total: "+total);

        return output;
    }
    
    public void addBuses(List<PersonalVehicle> vehicles)
    {
        for(PersonalVehicle b : buses)
        {
            vehicles.add(b);
        }
    }
    
    public List<PersonalVehicle> discretize_trips()
    {
        int id = 1;
        
        List<PersonalVehicle> output = new ArrayList<PersonalVehicle>();
        

        for(Zone o : costs.keySet())
        {
            for(Zone d : costs.get(o).keySet())
            {
                for(int t = 0; t < Simulator.num_asts; t++)
                {
                    int num_veh = 0;
                    double flow = costs.get(o).get(d)[t].HV_flow;
                    
                    num_veh = (int)Math.floor(flow);
                    
                    if(num_veh < 0)
                    {
                        throw new RuntimeException("Negative flow");
                    }
                    
                    flow -= num_veh;
                    
                    if(flow > Math.random())
                    {
                        num_veh++;
                    }
                    
                    
                    
                    int dtime_interval = Simulator.ast_duration / (num_veh+1);
                    
                    for(int i = 0; i < num_veh; i++)
                    {
                        int dtime = (i+1)*dtime_interval + t * Simulator.ast_duration;
                        
                        output.add(new PersonalVehicle(id++, o, d, dtime, VehicleClass.icv, allAVs?DriverType.AV : DriverType.HV));
                    }
                    
                    num_veh = 0;
                    flow = costs.get(o).get(d)[t].AV_flow;
                    
                    num_veh = (int)Math.floor(flow);
                    
                    if(num_veh < 0)
                    {
                        throw new RuntimeException("Negative flow");
                    }
                    
                    flow -= num_veh;
                    
                    if(flow > Math.random())
                    {
                        num_veh++;
                    }
                    
                    
                    
                    dtime_interval = Simulator.ast_duration / (num_veh+1);
                    
                    for(int i = 0; i < num_veh; i++)
                    {
                        int dtime = (i+1)*dtime_interval + t * Simulator.ast_duration;
                        
                        output.add(new PersonalVehicle(id++, o, d, dtime, VehicleClass.icv, DriverType.AV));
                    }
                }
            }
        }
   
        
        return output;
    }
    
    public Results traffic_assign(int max_iter) throws IOException
    {
        Simulator.print_status=true;
        System.out.println();
        return sim.msa(max_iter);
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
