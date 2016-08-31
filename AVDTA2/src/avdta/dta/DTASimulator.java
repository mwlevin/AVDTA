/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package avdta.dta;

import avdta.network.Path;
import avdta.network.PathList;
import avdta.network.Simulator;
import avdta.network.link.CentroidConnector;
import avdta.network.link.Link;
import avdta.network.node.Node;
import avdta.project.DTAProject;
import avdta.vehicle.Bus;
import avdta.vehicle.DriverType;
import avdta.vehicle.PersonalVehicle;
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
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author micha
 */
public class DTASimulator extends Simulator
{
    private Assignment currAssign;
    
    private int iteration;
    
    public DTASimulator(DTAProject project)
    {
        super(project);
        
        iteration = 1;
    }
    public DTASimulator(DTAProject project, Set<Node> nodes, Set<Link> links)
    {
        super(project, nodes, links);
        
        iteration = 1;
    }
    
    
    public DTAProject getProject()
    {
        return (DTAProject)super.getProject();
    }
    
    public DTAResults partial_demand(int iter) throws IOException
    {
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
            out.println("Iter\tStep\tGap %\tAEC\tTTT\tTrips\tNon-exiting\ttime");
        }
        
        
        for(int i = 0; i < iter; i++)
        {
            long time = System.nanoTime();
            
            Iterator<Vehicle> iterator = temp.iterator();
            
            while(iterator.hasNext())
            {
                if(Math.random() < 1.0 / (iter - i))
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
        }
        
        return output;
    }
    
    public DTAResults pathgen(double stepsize) throws IOException
    {
        int error_count = 0;

        Map<Node, Map<Node, Path[][]>> newpaths = new HashMap<Node, Map<Node, Path[][]>>();

        double tstt = 0;
        double min = 0;
        int exiting = 0;

        int count = 0;
        
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
                temp1.put(d, temp2 = new Path[Simulator.num_asts][4]);
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


            // move vehicle random chance
            if(v.getPath() == null || rand.nextDouble() < stepsize)
            {
                try
                {
                    v.setPath(temp2[ast][v.getDriver().typeIndex()]);
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
    
    public int getIteration()
    {
        return iteration;
    }
    
    public void saveAssignment(Assignment assign) throws IOException
    {        
        DTAProject project = (DTAProject)getProject();
        
        File folder = new File(project.getAssignmentsFolder()+"/"+assign.getName());
        folder.mkdirs();
        
        PathList paths = getPaths();
        paths.writeToFile(project.getPathsFile());
        
        assign.writeToFile(vehicles, (DTAProject)getProject());
    }
    
    public void openAssignment(File file) throws IOException
    {
        DTAProject project = (DTAProject)getProject();
        PathList paths = new PathList(this, project.getPathsFile());
        
        Assignment assign = new Assignment(file);
        assign.readFromFile(project, getVehicles(), paths);
        
        currAssign = assign;
    }
        
    public Assignment getAssignment()
    {
        return currAssign;
    }
    
    public Vehicle findVehicle(int id)
    {
        for(Vehicle v : vehicles)
        {
            if(v.getId() == id)
            {
                return v;
            }
        }
        return null;
    }
    
    public void addVehicles()
    {
        List<Vehicle> vehicles = getVehicles();
        
        while(veh_idx < vehicles.size())
        {
            PersonalVehicle v = (PersonalVehicle)vehicles.get(veh_idx);

            if(v.getPath() == null)
            {
                veh_idx++;
            }
            else if(v.getDepTime() <= Simulator.time)
            {
                v.entered();
                v.getNextLink().addVehicle(v);
                veh_idx++;
            }
            else
            {
                break;
            }
        }
    }
    
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
    
    public DTAResults msa(int max_iter) throws IOException
    {
        return msa(max_iter, -1);
    }
    
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
        
        
        createSimVat(new File(project.getAssignmentsFolder()+"/"+currAssign.getName()+"/sim.vat"));
        
        
        if(statusUpdate != null)
        {
            statusUpdate.update(2.0/4, 0.25, "Postprocessing");
        }
        
        
        Scanner filein = new Scanner(new File(project.getAssignmentsFolder()+"/"+currAssign.getName()+"/sim.vat"));
        
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
    
    public DTAResults msa(int max_iter, double min_gap) throws IOException
    {
        return msa_cont(1, max_iter, min_gap);
    }
    
    public DTAResults msa_cont(int max_iter, double min_gap) throws IOException
    {
        return msa_cont(iteration, max_iter, min_gap);
    }
    
    public DTAResults msa_cont(int start_iter, int max_iter) throws IOException
    {
        return msa_cont(start_iter, max_iter, -1);
    }
    
    public DTAResults msa_cont(int start_iter, int max_iter, double min_gap) throws IOException
    {
        currAssign = new MSAAssignment(getProject(), null, start_iter);
        
        if(statusUpdate != null)
        {
            statusUpdate.update(0, 0, "Starting MSA");
        }
        
        
        PrintStream fileout = new PrintStream(new FileOutputStream(
                new File(getProject().getAssignmentsFolder()+"/"+currAssign.getName()+"/log.txt")), true);
        
        iteration = start_iter;
        DTAResults output = null;

        if(print_status)
        {
            out.println("Iter\tStep\tGap %\tAEC\tTSTT\tTrips\tNon-exit\ttime");
        }
        
        fileout.println("Iter\tStep\tGap %\tAEC\tTTT\tTrips\tNon-exit\ttime");

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
        while(iteration++ < max_iter && (iteration == 2 || min_gap < output.getGapPercent()));
        
        currAssign.setResults(output);
        ((MSAAssignment)currAssign).setIter(iteration);
        saveAssignment(currAssign);
        
        if(statusUpdate != null)
        {
            statusUpdate.update(1, 0, "");
        }

        out.println(String.format("TSTT\t%.1f", getTSTT()/3600.0)+"\thr\nAvg. TT\t"+String.format("%.2f", getTSTT() / 60 / vehicles.size())+"\tmin/veh"+
                "\nExiting: "+getNumExited());
        out.println("Energy:\t"+getTotalEnergy());
        out.println("VMT:\t"+getTotalVMT());
        out.println("MPG:\t"+(getTotalVMT() / (getTotalEnergy() / VehicleClass.E_PER_GALLON)));
        out.println();
        out.println("HV TT:\t"+(getAvgTT(DriverType.HV)/60)+"\tmin");
        out.println("AV TT:\t"+(getAvgTT(DriverType.AV)/60)+"\tmin");
        out.println();
        out.println("DA TT:\t"+(getAvgBusTT(false)/60)+"\tmin");
        out.println("Bus TT:\t"+(getAvgBusTT(true)/60)+"\tmin");
        
        fileout.println(String.format("TSTT\t%.1f", getTSTT()/3600.0)+"\thr\nAvg. TT\t"+String.format("%.2f", getTSTT() / 60 / vehicles.size())+"\tmin/veh");
        fileout.println();
        fileout.println("Energy:\t"+getTotalEnergy());
        fileout.println("VMT:\t"+getTotalVMT());
        fileout.println("MPG:\t"+(getTotalVMT() / (getTotalEnergy() / VehicleClass.E_PER_GALLON)));
        fileout.println();
        fileout.println("HV TT:\t"+(getAvgTT(DriverType.HV)/60)+"\tmin");
        fileout.println("AV TT:\t"+(getAvgTT(DriverType.AV)/60)+"\tmin");
        fileout.println();
        fileout.println("DA TT:\t"+(getAvgBusTT(false)/60)+"\tmin");
        fileout.println("Bus TT:\t"+(getAvgBusTT(true)/60)+"\tmin");
        
        fileout.close();
        
        return output;
    }
    
    
}
