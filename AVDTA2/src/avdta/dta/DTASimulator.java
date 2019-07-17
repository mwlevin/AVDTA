/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.demand.DemandProfile;
import avdta.demand.DynamicODTable;
import avdta.demand.ReadDemandNetwork;
import avdta.network.Path;
import avdta.network.PathList;
import avdta.network.ReadNetwork;
import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.network.node.Zone;
import avdta.project.DTAProject;
import avdta.project.DemandProject;
import avdta.project.Project;
import avdta.util.FileTransfer;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.EmergencyVehicle;
import avdta.vehicle.PersonalVehicle;
import avdta.vehicle.VOT;
import avdta.vehicle.Vehicle;
import avdta.vehicle.fuel.VehicleClass;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/**
 * The {@link DTASimulator} adds methods to find a DUE assignment. Currently it uses the method of successive averages.
 * @author Michael
 */
public class DTASimulator extends Simulator
{
    private Assignment currAssign;
    
    private int iteration;
    
    private boolean continueUntilExit = true;
    
    
    
    /**
     * Constructs this {@link DTASimulator} empty with the given project.
     * @param project the project
     */
    public DTASimulator(DTAProject project)
    {
        super(project);
        
        iteration = 1;
    }
    
    /**
     * Constructs this {@link DTASimulator} with the given project and with the given sets of {@link Node}s and {@link Link}s.
     * @param project the project
     * @param nodes the set of {@link Node}s
     * @param links the set of {@link Link}s
     */
    public DTASimulator(DTAProject project, Set<Node> nodes, Set<Link> links)
    {
        super(project, nodes, links);
        
        iteration = 1;
    }
    
    
    /**
     * Returns the project associated with this simulator
     * @return the project associated with this simulator
     */
    public DTAProject getProject()
    {
        return (DTAProject)super.getProject();
    }
    
    /**
     * Loads demand over multiple iterations to reduce the congestion at the start.
     * Each iteration, 1/iter vehicles are loaded onto the shortest path.
     * Shortest paths are updated based on congestion each iteration.
     * Vehicles already loaded are not affected.
     * 
     * @param iter the number of iterations
     * @return the simulator results at the end
     * @throws IOException if a file cannot be accessed
     */
    public DTAResults partial_demand(int iter) throws IOException
    {
        if(statusUpdate != null)
        {
            statusUpdate.update(0, 0, "Starting partial demand");
        }
        
        List<Vehicle> temp = new ArrayList<Vehicle>();
        
        for(Vehicle v : vehicles)
        {
            temp.add(v);
        }
        
        vehicles.clear();
        
        int count = 0;
        
        DTAResults output = null;
        
        if(print_status)
        {
            out.println(getProject().getName());
            out.println("Iter\tStep\tGap %\tAEC\tTSTT\tTrips\tNon-exiting\ttime");
        }
        
        
        for(int i = 0; i < iter; i++)
        {
            long time = System.nanoTime();
            
            Iterator<Vehicle> iterator = temp.iterator();
            
            while(iterator.hasNext())
            {
                if(rand.nextDouble() < 1.0 / (iter - i))
                {
                    vehicles.add(iterator.next());
                    iterator.remove();
                    count++;
                }
                else
                {
                    iterator.next();
                }
            }
            
            Collections.sort(vehicles);
            output = pathgen(0);
            
            time = System.nanoTime() - time;
            
            if(print_status)
            {
                out.println((i+1)+"\t"+String.format("%.4f", 0.0)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));
            }
            
            if(statusUpdate != null)
            {
                statusUpdate.update((i+1)/iter, 1.0/iter, "Assigned "+Math.round(100.0/iter)+"% demand");
            }
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

        Map<Node, Map<Node, Path[][]>> newpaths = new HashMap<Node, Map<Node, Path[][]>>();

        double tstt = 0;
        double min = 0;

        int count = 0;
        int moved_count = 0;

        for(Vehicle v : vehicles)
        {
            if(v.isTransit())
            {
                continue;
            }
            
            Node o = v.getOrigin();
            Node d = v.getDest();
            int ast = v.getAST();
            int dep_time = v.getDepTime();




            
            
            Map<Node, Path[][]> temp1;
            Path[][] temp2;

            if(newpaths.containsKey(o))
            {
                temp1 = newpaths.get(o);
            }
            else
            {
                newpaths.put(o, temp1 = new HashMap<Node, Path[][]>());
            }

            if(temp1.containsKey(d))
            {
                temp2 = temp1.get(d);
            }
            else
            {
                temp1.put(d, temp2 = new Path[Simulator.num_asts][DriverType.num_types]);
            }

            if(temp2[ast][v.getDriver().typeIndex()] == null)
            {
                temp2[ast][v.getDriver().typeIndex()] = findPath(o, d, getAvgDepTime(ast), v.getVOT(), v.getDriver(), costFunc);
            }
            
            

            if(v.getPath() != null)
            {
                tstt += v.getPath().getAvgCost(dep_time, v.getVOT(), costFunc);
                //tt += v.getTT();


                if(v.getTT() < 0)
                {
                    out.println("TT < 0 "+v.getDepTime()+" "+v.getExitTime());
                }
            }
            min += temp2[ast][v.getDriver().typeIndex()].getAvgCost(dep_time, v.getVOT(), costFunc);


            
            double r = rand.nextDouble();
            
            // move vehicle random chance
            if(v.getRouteChoice() == null || r <= stepsize)
            {


                try
                {
                    
                    
                    v.setPath(temp2[ast][v.getDriver().typeIndex()]);
                    
                    
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


        return new DTAResults(min, tstt, vehicles.size(), getNumExited());

    }
    
    /**
     * Returns the current iteration
     * @return the current iteration
     */
    public int getIteration()
    {
        return iteration;
    }
    
    /**
     * Saves the assignment in the given {@link Assignment} object.
     * This calls {@link Assignment#writeToFile(java.util.List, avdta.project.DTAProject)}.
     * @param assign the {@link Assignment} wrapper object
     * @throws IOException if a file cannot be accessed
     */
    public void saveAssignment(Assignment assign) throws IOException
    {        
        DTAProject project = (DTAProject)getProject();
        
        File folder = new File(project.getAssignmentsFolder()+"/"+assign.getName());
        folder.mkdirs();
        
        PathList paths = getPaths();
        
        
        paths.writeToFile(assign.getPathsFile());
        
        assign.writeToFile(vehicles, (DTAProject)getProject());
        
        FileTransfer.copy(project.getDemandFile(), assign.getDemandFile());
    }

    
    /**
     * Opens an {@link Assignment} from the given {@link Assignment} folder and assigns vehicles as specified.
     * This calls {@link Assignment#readFromFile(avdta.project.DTAProject, java.util.List, avdta.network.PathList)} and {@link DTASimulator#loadAssignment(avdta.dta.Assignment)}.
     * * To obtain data about the {@link Assignment}, call {@link DTASimulator#simulate()}.
     * @param file the assignment directory
     * @throws IOException if a file cannot be accessed
     */
    public void openAssignment(File file) throws IOException
    {
        loadAssignment(new Assignment(file));
    }
    
    /**
     * This reads the specified {@link Assignment} and assigns the vehicles in this simulator accordingly.
     * Vehicles and their ids must match the vehicles specified in the assignment. 
     * * To obtain data about the {@link Assignment}, call {@link DTASimulator#simulate()}.
     * @param assign the assignment to be loaded
     * @throws IOException if a file cannot be accessed
     */
    public void loadAssignment(Assignment assign) throws IOException
    {
        DTAProject project = getProject();
        PathList paths = new PathList(this, assign.getPathsFile());
        setPaths(paths);
        
        assign.readFromFile(project, getVehicles(), paths);
        
        currAssign = assign;
    }
        
    /**
     * Returns the current assignment
     * @return the current assignment
     */
    public Assignment getAssignment()
    {
        return currAssign;
    }
    
    
    
    
    /**
     * Writes a list of vehicle results, including travel times and mpg
     * @throws IOException if a file cannot be accessed
     */
    public void writeVehicleResults() throws IOException
    {
        DTAProject project = (DTAProject)getProject();
        PrintStream fileout = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/vehicles.txt")), true);
        
        fileout.println("ID\tOrigin\tDest\tDtime\tTT\tMPG\tTime waiting");
        for(Vehicle x : vehicles)
        {
            PersonalVehicle v = (PersonalVehicle)x;
            fileout.println(v.getId()+"\t"+v.getOrigin()+"\t"+v.getDest()+"\t"+v.getDepTime()+"\t"+v.getTT()+"\t"+v.getMPG()+"\t"+v.getTimeWaiting());
        }
        
        fileout.close();
    }
    
    /**
     * Calls {@link DTASimulator#msa(int, double)} with a gap of 0.
     * @param max_iter the maximum number of iterations
     * @return results from simulating the assignment
     * @throws IOException if a file is not found
     */
    public DTAResults msa(int max_iter) throws IOException
    {
        return msa(max_iter, -1);
    }
    
    /**
     * This creates the sim.vat ({@link Assignment#getAssignmentDirectory()}{@code /sim.vat}) and the VISTA vehicle_path ({@link Project#getResultsFolder()}{@code /vehicle_path.txt}) and vehicle_path_time ({@link Project#getResultsFolder()}{@code /vehicle_path_time.txt}) files.
     * @throws IOException if a file cannot be accessed
     */
    public void importResults() throws IOException
    {
        
        DTAProject project = (DTAProject)getProject();
        
        if(statusUpdate != null)
        {
            statusUpdate.update(0, 0.25, "Simulating");
        }
        
        
        simulate();
        
        
        if(statusUpdate != null)
        {
            statusUpdate.update(1.0/4, 0.25, "Creating sim.vat");
        }
        
        
        createSimVat(currAssign.getSimVatFile());
        
        
        if(statusUpdate != null)
        {
            statusUpdate.update(2.0/4, 0.25, "Postprocessing");
        }
        
        
        Scanner filein = new Scanner(new File(currAssign.getAssignmentDirectory()+"/sim.vat"));
        
        PrintStream path_out = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/vehicle_path.txt")), true);
        PrintStream veh_out = new PrintStream(new FileOutputStream(new File(project.getResultsFolder()+"/vehicle_path_time.txt")), true);
        
        Set<Integer> printed_paths = new HashSet<Integer>();
        
        // vehicle_path: id, origin, dest, hash, size, freeflowtt, length, issimpath, links
        // vehicle_path_time: id, type, dta_departure, sim_departure, sim_exittime, dta_path, sim_path, arrivaltime
        
        
        for(Vehicle x : vehicles)
        {
            
            int type = filein.nextInt();
            int id = filein.nextInt();
            int enter = (int)filein.nextDouble();
            int exit = (int)filein.nextDouble();
            
            int size = filein.nextInt();
            
            int[] arr_times = new int[size];
            
            for(int i = 0; i < size; i++)
            {
                filein.nextInt();
                arr_times[i] = (int)filein.nextDouble();
            }
            
            PersonalVehicle v = (PersonalVehicle)x;
            
            if(id != v.getId())
            {
                throw new RuntimeException("Vehicles out of order");
            }
            
            
            
            Path p = v.getPath();
            
            if(printed_paths.add(p.getId()))
            {
                path_out.print(p.getId()+"\t"+p.getOrigin()+"\t"+p.getDest()+"\t"+p.hashCode()+"\t"+p.size()+"\t"+p.getFFTime()+"\t"+p.getLength()+"\t2\t{");
                
                path_out.print(p.get(0));
                for(int i = 1; i < p.size(); i++)
                {
                    path_out.print(","+p.get(i));
                }
                path_out.println("}");
            }
            
            veh_out.print(v.getId()+"\t"+v.getType()+"\t"+v.getDepTime()+"\t"+v.getNetEnterTime()+"\t"+v.getExitTime()+"\t"+p.getId()+"\t"+p.getId()+"\t{");
            
            veh_out.print(arr_times[0]);
            for(int i = 1; i < arr_times.length; i++)
            {
                veh_out.print(","+arr_times[i]);
            }
            
            veh_out.print("}");
            veh_out.println();
        }
        
        filein.close();
        path_out.close();
        veh_out.close();
        
        if(statusUpdate != null)
        {
            statusUpdate.update(3.0/4, 0, "Link tdd");
        }
        
        printLinkTdd();
        
        if(statusUpdate != null)
        {
            statusUpdate.update(1, 0, "");
        }
    }
    
    /**
     * Calls {@link DTASimulator#msa_cont(int, int, double)} with a starting iteration of 1.
     * @param max_iter the maximum number of iterations
     * @param min_gap the minimum gap
     * @return results from simulating the assignment
     * @throws IOException if a file cannot be accessed
     */
    public DTAResults msa(int max_iter, double min_gap) throws IOException
    {
        return msa_cont(1, max_iter, min_gap);
    }
    
    /**
     * Calls {@link DTASimulator#msa_cont(int, int, double)} starting at the current iteration.
     * @param max_iter the maximum number of iterations
     * @param min_gap the minimum gap
     * @return results from simulating the assignment
     * @throws IOException if a file cannot be accessed
     */
    public DTAResults msa_cont(int max_iter, double min_gap) throws IOException
    {
        return msa_cont(iteration, max_iter, min_gap);
    }
    
    /**
     * Calls {@link DTASimulator#msa_cont(int, int, double)} with a minimum gap of 0.
     * @param start_iter the iteration to start at
     * @param max_iter the maximum iteration to be reached (not the maximum number of iterations)
     * @return results from simulating the assignment
     * @throws IOException if a file cannot be accessed
     */
    public DTAResults msa_cont(int start_iter, int max_iter) throws IOException
    {
        return msa_cont(start_iter, max_iter, -1);
    }
    
    public Assignment createAssignment(int start_iter)
    {
        return new MSAAssignment(getProject(), null, start_iter);
    }
    
    /**
     * Runs the method of successive averages starting at the specified iteration.
     * @param start_iter the starting iteration. Note that this should be the 1 more than the stopping iteration of the previous assignment.
     * @param max_iter the maximum iteration to be reached (not the maximum number of iterations)
     * @param min_gap the minimum gap
     * @return results from simulating the assignment
     * @throws IOException if a file cannot be accessed 
     */
    public DTAResults msa_cont(int start_iter, int max_iter, double min_gap) throws IOException
    {
        long actual_time = System.nanoTime();
        
        
        currAssign = createAssignment(start_iter);
        
        if(statusUpdate != null)
        {
            statusUpdate.update(0, 0, "Starting MSA");
        }
        
        
        PrintStream fileout = new PrintStream(new FileOutputStream(currAssign.getLogFile(), start_iter > 1), true);
        
        iteration = start_iter;
        DTAResults output = null;

        if(print_status)
        {
            out.println(getProject().getName());
            out.println("Iter\tStep\tGap %\tAEC\tTSTT\tTrips\tNon-exit\ttime");
        }
        
        fileout.println("Iter\tStep\tGap %\tAEC\tTSTT\tTrips\tNon-exit\ttime");
        
        int max_iter_contingency = max_iter *2;
        
        do
        {
            long time = System.nanoTime();

            double stepsize = 1.0/iteration;
            output = pathgen(stepsize);
            
            if(iteration == 1)
            {
                output.setMinTT(0);
            }


            time = System.nanoTime() - time;

            if(print_status)
            {
                out.println(iteration+"\t"+String.format("%.4f", stepsize)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));
            }
            fileout.println(iteration+"\t"+String.format("%.4f", stepsize)+"\t"+String.format("%.2f", output.getGapPercent())+"%\t"+
                        String.format("%.1f", output.getAEC())+"\t"+String.format("%.1f", output.getTSTT())+"\t"+output.getTrips()+"\t"+
                        output.getNonExiting()+"\t"+String.format("%.2f", time / 1.0e9));

            if(statusUpdate != null)
            {
                statusUpdate.update((double)(iteration - start_iter+1)/ (max_iter - start_iter + 1), 1.0 / (max_iter - start_iter + 1), "Iteration "+iteration);
            }
        }
        while( (iteration++ < max_iter && (iteration <= 2 || min_gap < output.getGapPercent())) || 
                (continueUntilExit && output.getNonExiting() > 0 && iteration < max_iter_contingency) );
        
        currAssign.setResults(output);
        ((MSAAssignment)currAssign).setIter(iteration);
        saveAssignment(currAssign);
        
        if(statusUpdate != null)
        {
            statusUpdate.update(1, 0, "");
        }
        
        actual_time = System.nanoTime() - actual_time;

        out.println(String.format("TSTT\t%.1f", getTSTT()/3600.0)+"\thr");
        out.println("Avg. TT\t"+String.format("%.2f", getTSTT() / 60 / vehicles.size())+"\tmin/veh");
        out.println("Exiting: "+getNumExited());
        out.println("Energy:\t"+getTotalEnergy());
        out.println("VMT:\t"+String.format("%.2f", getTotalVMT()));
        out.println("MPG:\t"+String.format("%.2f", getAvgMPG()));
        out.println();
        out.println("HV TT:\t"+String.format("%.2f", getAvgTT(DriverType.HV)/60)+"\tmin");
        out.println("AV TT:\t"+String.format("%.2f", getAvgTT(DriverType.AV)/60)+"\tmin");
        out.println();
        out.println("DA TT:\t"+String.format("%.2f", getAvgBusTT(false)/60)+"\tmin");
        out.println("Bus TT:\t"+String.format("%.2f", getAvgBusTT(true)/60)+"\tmin");
        out.println("Bus ratio:\t"+(calcAvgBusTimeRatio()));
        out.println("CPU time: "+String.format("%.1f", actual_time/1.0e9)+"s");
        
        fileout.println(String.format("TSTT\t%.1f", getTSTT()/3600.0)+"\thr\nAvg. TT\t"+String.format("%.2f", getTSTT() / 60 / vehicles.size())+"\tmin/veh");
        fileout.println();
        fileout.println("Energy:\t"+getTotalEnergy());
        fileout.println("VMT:\t"+String.format("%.2f", getTotalVMT()));
        fileout.println("MPG:\t"+String.format("%.2f", getAvgMPG()));
        fileout.println();
        fileout.println("HV TT:\t"+String.format("%.2f", getAvgTT(DriverType.HV)/60)+"\tmin");
        fileout.println("AV TT:\t"+String.format("%.2f", getAvgTT(DriverType.AV)/60)+"\tmin");
        fileout.println();
        fileout.println("DA TT:\t"+String.format("%.2f", getAvgBusTT(false)/60)+"\tmin");
        fileout.println("Bus TT:\t"+String.format("%.2f", getAvgBusTT(true)/60)+"\tmin");
        fileout.println("Bus TT ratio:\t"+calcAvgBusTimeRatio());
        fileout.println("CPU time: "+String.format("%.1f", actual_time/1.0e9)+"s");
        
        fileout.close();
        
        return output;
    }
    
    
    /**
     * Creates a subnetwork that includes the specified set of links for the specified project.
     * The project should be a new project (or an existing project that will be overwritten).
     * 
     * This method requires that a sim.vat file has been created (see {@link Simulator#createSimVat(java.io.File)}).
     * The sim.vat file is used to read vehicle trips.
     * Vehicle trip origins, destinations, and departure times will be adjusted based on when they enter and exit the subnetwork via the routes in sim.vat.
     * If necessary, vehicle trips will be split into multiple trips that enter and exit the subnetwork.
     * Additional centroids and centroid connectors will be created where vehicles enter and exit the subnetwork links.
     * 
     * Note that transit routes are not yet implemented.
     * 
     * @param newLinks the set of links that to be included
     * @param simvat the sim.vat file
     * @param rhs the new project
     * @throws IOException if a file cannot be accessed
     */
    public void createSubnetwork(Set<Link> newLinks, File simvat, DemandProject rhs) throws IOException
    {
        DTAProject project = getProject();
        
        Simulator newsim = rhs.getSimulator();
        
        Set<Node> newNodes = new HashSet<Node>();
        
        for(Link l : newLinks)
        {
            newNodes.add(l.getSource());
            newNodes.add(l.getDest());
        }
        
        /*
        for(Link l : getLinks())
        {
            if(l.isCentroidConnector())
            {
                CentroidConnector c = (CentroidConnector)l;
                
                if(newNodes.contains(c.getIntersection()))
                {
                    newNodes.add(c.getZone());
                    newLinks.add(c);
                }
            }
        }
        */
        Map<Integer, Link> linksmap = createLinkIdsMap();
        
        // this is a map of created centroids
        Map<Node, Node[]> centroids = new HashMap<Node, Node[]>();
        
        for(Node n : nodes)
        {
            if(n.isZone())
            {
                centroids.put(n, new Node[]{n, n});
            }
        }
        
        // add centroid connectors and centroids where necessary
        // create trips table
        
        int new_veh_id = 1;
        int new_centroid_id = 1;
        

        
        for(Vehicle v : getVehicles())
        {
            new_veh_id = (int)Math.max(new_veh_id, v.getId()+1);
        }
        
        for(Node n : getNodes())
        {
            if(n.isZone())
            {
                new_centroid_id = (int)Math.max(new_centroid_id, n.getId()+1);
            }
        }
        

        
        Random rand = rhs.getRandom();
        
        // I need vehicle paths and arrival times - use sim.vat
        Scanner filein = new Scanner(simvat);
        PrintStream fileout = new PrintStream(new FileOutputStream(rhs.getDemandFile()), true);
        fileout.println(ReadDemandNetwork.getDemandFileHeader());
        
        while(filein.hasNextInt())
        {
            int type = filein.nextInt();
            int id = filein.nextInt();
            int dtime = (int)filein.nextDouble();
            filein.nextDouble();
            int size = filein.nextInt();
            filein.nextLine();
            
            Scanner chopper = new Scanner(filein.nextLine());
            
            // skip transit vehicles
            if(type / 100 == ReadNetwork.BUS / 100)
            {
                continue;
            }
            
            Path path = new Path();
            ArrayList<Integer> arrTimes = new ArrayList<Integer>();
            
            while(chopper.hasNextInt())
            {
                path.add(linksmap.get(chopper.nextInt()));
                arrTimes.add((int)chopper.nextDouble());
            }
            
            // go through path, find where it meets subnetwork, create centroids if necessary
            // might require multiple vehicle trips
            
            Node origin = path.getOrigin();
            Node dest = path.getDest();
            
            if(!newNodes.contains(origin))
            {
                origin = null;
            }
            
            if(!newNodes.contains(dest))
            {
                dest = null;
            }
            
            for(int i = 0; i < path.size(); i++)
            {
                Link l = path.get(i);
                
                if(!newLinks.contains(l))
                {
                    if(newNodes.contains(l.getDest()))
                    {
                        origin = l.getDest();
                        createCentroid(origin, centroids, new_centroid_id, newNodes, newLinks);
                        new_centroid_id += 2;
                        
                        dtime = (int)Math.max(dtime, arrTimes.get(i+1));
                    }
                    else if(newNodes.contains(l.getSource()))
                    {
                        dest = l.getSource();
                        
                        createCentroid(dest, centroids, new_centroid_id, newNodes, newLinks);
                        new_centroid_id += 2;
                        
                        // create vehicle trip
                        if(dtime >= 0)
                        {
                            fileout.println((new_veh_id++)+"\t"+type+"\t"+centroids.get(origin)[0]+"\t"+centroids.get(dest)[1]+"\t"+dtime+"\t"+VOT.dagum_rand(rand));
                        }
                        origin = null;
                        dest = null;
                        
                    }
                }
            }
            
            if(origin != null && dest != null && dtime >= 0)
            {
                fileout.println((new_veh_id++)+"\t"+type+"\t"+centroids.get(origin)[0]+"\t"+centroids.get(dest)[1]+"\t"+dtime+"\t"+VOT.dagum_rand(rand));
            }
            
        }
        filein.close();
        
        
        
        // clear unused data
        
        linksmap = null;
        centroids = null;
        
        newsim.setNetwork(newNodes, newLinks);
        newsim.save(rhs);
        
        
        // copy demand profile
        FileTransfer.copy(project.getDemandProfileFile(), rhs.getDemandProfileFile());
        
        
        
        
        DemandProfile profile = new DemandProfile(rhs);
        
        DynamicODTable triptable = new DynamicODTable();
               
        // create dynamic od table
        filein = new Scanner(rhs.getDemandFile());
        
        filein.nextLine();
        while(filein.hasNextInt())
        {
            VehicleRecord trip = new VehicleRecord(filein.nextLine());
            
            triptable.addDemand(trip, profile);
        }
        filein.close();
        
        triptable.printDynamicOD(rhs);
        
        // create static od table
        triptable.printStaticOD(rhs);
    }
    
    private void createCentroid(Node node, Map<Node, Node[]> centroids, int id, Set<Node> newNodes, Set<Link> newLinks)
    {
        if(!centroids.containsKey(node))
        {
            Zone newO = new Zone(id, node);
            Zone newD = new Zone(id+1, node);
            
            centroids.put(node, new Node[]{newO, newD});
            
            newNodes.add(newO);
            newNodes.add(newD);
            
            newLinks.add(new CentroidConnector(id+10000, newO, node));
            newLinks.add(new CentroidConnector(id+20000, node, newD));
        }
    }
}
